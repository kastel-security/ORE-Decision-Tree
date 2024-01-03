package decisiontree.main

import crypto.ore.UpdatableCiphertext
import crypto.ore.UpdatableSecretKey
import decisiontree.DecisionTreeHandler
import decisiontree.data.Data
import decisiontree.data.Dataset
import decisiontree.datasets.DatasetSource
import decisiontree.datasets.GenericData
import decisiontree.datasets.mnist.MNIST
import decisiontree.datasets.other.BostonHousing
import decisiontree.datasets.other.ComparableDatasetInstance
import decisiontree.datasets.other.Titanic
import decisiontree.evaluators.TotalHeuristicSplit
import decisiontree.evaluators.heuristic.EntropyEvaluator
import decisiontree.math.Curve25519Group
import decisiontree.math.Ed25519Group
import decisiontree.ore.prf.NPRPrf
import decisiontree.ore.prf.Prf
import decisiontree.ore.updatable.UpdatableParams
import decisiontree.tree.*
import decisiontree.util.AsyncNetworkHandler
import decisiontree.util.ParallelHandler
import decisiontree.util.WrappedByteArray
import decisiontree.util.timed
import org.bouncycastle.math.ec.ECPoint
import java.net.ServerSocket
import java.net.Socket

fun getOreScheme(bitLength: Int) = UpdatableParams(NPRPrf(NPRPrf.SHA256,
    Ed25519Group,
//    Curve25519Group,
//    ECGroup("curve25519"),
//    ZSafePrime.p2048Group,
    Prf.HMAC_SHA256), bitLength)
private typealias GroupElement = WrappedByteArray
//private typealias GroupElement = ECPoint
//private typealias GroupElement = BigInteger
val splitEvaluator = TotalHeuristicSplit(EntropyEvaluator)
val executionHandler = ParallelHandler

private typealias Label = Int
//val DATASET = GlassDataset
//private typealias Label = String


object Alice {
    val host = "localhost"
    val port = 10001

    fun run(nDatapoints: Int, datasetSource: DatasetSource<Label>) {
        val dataset = timed("Loading dataset") { datasetSource.loadDataset(nDatapoints) }
        val tree = runTraining(dataset, datasetSource.attributeBitSize)
        val testSet = datasetSource.testSet.loadDataset()
        var correct = 0
        for ((dataPoint, label) in testSet.allData) {
            if (tree.classify(dataPoint) == label) {
                correct++
            }
        }
        println("$correct correct out of ${testSet.dataSize}. (${correct.toDouble() / testSet.dataSize})")
        val nNodes = NodeCounter<Label>().visit(tree, Unit)
        val nLayers = LayerCounter<Label>().visit(tree, Unit)
        println("Tree has $nNodes nodes in $nLayers layers.")
    }

    fun runTraining(dataset: Dataset<Label>, nBits: Int): Node<Label> {
        val ORE_SCHEME = getOreScheme(nBits)
        val nAttributes = dataset.nAttributes
        val aliceDatapoints = dataset.dataSize
        val serverSocket = ServerSocket(port)
        println("Waiting for incoming connection.")
        val socket = serverSocket.accept()
        println("Connection established.")
        return timed("MPC Training (Alice)") {
            AsyncNetworkHandler(socket).use { network ->

                //KeyGen
                val keys: List<UpdatableSecretKey<GroupElement>> = timed("Key generation") {
                    val list = ArrayList<UpdatableSecretKey<GroupElement>>()
                    executionHandler.handle(nAttributes, {}, {
                        ORE_SCHEME.generateSecretKey()
                    }, list::add)
                    list
                }

                //Bob -> Alice: Enc(k_j, m_{i, j})
                //Alice -> Bob: Updated Datapoints and Alice's data
                timed("Updaing Bobs Data") {
                    val bobDatapoints = network.readObject<Int>()
                    executionHandler.handle(bobDatapoints * nAttributes, {
                        it to network.readObject<UpdatableCiphertext<GroupElement>>()
                    }, { (idx, ct) ->
                        keys[idx % nAttributes].update(ct)
                    }, network::writeObject)
                }
                timed("Encrypting and sending own datapoints") {
                    network.writeObject(aliceDatapoints)
                    executionHandler.handle(aliceDatapoints, { it }, { i ->
                        val (data, label) = dataset.allData[i]
                        val cts = Array(nAttributes) { j ->
                            keys[j].encrypt((data.getAttribute(j) as Int).toLong())
                        }
                        cts to label
                    }, { (cts, label) ->
                        for (ct in cts) {
                            network.writeObject(ct)
                        }
                        network.writeObject(label)
                    })
                }

                //trained tree
                val tree = timed("Receiving tree") {
                    network.readObject<Node<Label>>()
                }
                println("A total of ${network.bytesSent()} bytes were sent.")
                println("A total of ${network.bytesReceived()} bytes were received.")
                timed("Decrypting received tree") { getDecryptionVisitor(keys).visit(tree, Unit) }
            }
        }
    }


    fun getDecryptionVisitor(keys: List<UpdatableSecretKey<GroupElement>>) = object: TreeVisitor<Label, Unit, Node<Label>>() {

        override fun visitLeaf(node: LeafNode<Label>, state: Unit): Node<Label> = node

        override fun visitInner(node: InnerNode<Label>, state: Unit): Node<Label> {

            val condition = node.condition
            val attr = condition.attribute
            val decSplit: Long = keys[attr].decrypt(condition.split as UpdatableCiphertext<GroupElement>)
            val newCondition = Condition(attr, decSplit.toInt())
            return InnerNode(visit(node.leftNode, state), visit(node.rightNode, state), newCondition)
        }

        override fun visitForest(node: ForestNode<Label>, state: Unit): Node<Label> {
            val originalNodes = node.subNodes
            val ret = Array(originalNodes.size) { i ->
                visit(originalNodes[i], state)
            }
            return ForestNode(ret)
        }
    }

}

object Bob {

    fun run(nDatapoints: Int, datasetSource: DatasetSource<Label>) {
        val dataSize = datasetSource.dataSize
        val dataset = timed("Loading dataset") { datasetSource.loadDataset(dataSize - nDatapoints, dataSize) }
        runTraining(dataset, datasetSource.attributeBitSize)
    }

    fun runTraining(dataset: Dataset<Label>, nBits: Int) = timed("MPC training (Bob)") {
        val ORE_SCHEME = getOreScheme(nBits)
        val nAttributes = dataset.nAttributes
        val bobDatapoints = dataset.dataSize
        val socket = Socket(Alice.host, Alice.port)
        AsyncNetworkHandler(socket).use { network ->

            //KeyGen
            val keys: List<UpdatableSecretKey<GroupElement>> = timed("Key generation") {
                val keys = ArrayList<UpdatableSecretKey<GroupElement>>()
                executionHandler.handle(bobDatapoints * nAttributes, {}, {
                    ORE_SCHEME.generateSecretKey()
                }, keys::add)
                keys
            }

            //Bob -> Alice: Enc(k_j, m_{i, j})
            timed("Encrypting and sending own data points") {
                network.writeObject(bobDatapoints)
                executionHandler.handle(bobDatapoints * nAttributes, { it }, { idx ->
                        val i = idx / nAttributes
                        val j = idx % nAttributes
                        val (datapoint, _) = dataset.allData[i]
                        val value = datapoint.getAttribute(j)
                        keys[idx].encrypt((value as Int).toLong())
                    }, network::writeObject)
            }

            //Alice -> Bob: Updated Datapoints and Alice's data
            val commonData = ArrayList<Pair<Data, Label>>()
            timed("Receiving and reverting updates on own data") {
                val updatedCts = ArrayList<UpdatableCiphertext<GroupElement>>()
                executionHandler.handle(bobDatapoints * nAttributes, { idx ->
                    idx to network.readObject<UpdatableCiphertext<GroupElement>>()
                }, { (idx, ct) ->
                    keys[idx].updateRev(ct)
                }, updatedCts::add)
                for (i in 0 until bobDatapoints) {
                    val (_, label) = dataset.allData[i]
                    val datapoint = GenericData(Array(nAttributes) { j ->
                        updatedCts[i * nAttributes + j]
                    })
                    commonData += datapoint to label
                }
            }
            timed("Receiving Alice data") {
                val aliceDatapoints = network.readObject<Int>()
                for (i in 0 until aliceDatapoints) {
                    val datapoint = GenericData(Array(nAttributes) { j ->
                        network.readObject<UpdatableCiphertext<GroupElement>>()
                    })
                    val label = network.readObject<Label>()
                    commonData += datapoint to label
                }
            }

            //training
            val trainedTree = timed("Training tree") {
                val commonDataset = Dataset(commonData, nAttributes)
                DecisionTreeHandler.train(commonDataset, splitEvaluator)
            }
            timed("Sending trained tree") {
                network.writeObject(trainedTree)
            }
            println("A total of ${network.bytesSent()} bytes were sent.")
            println("A total of ${network.bytesReceived()} bytes were received.")
        }
        println("Finished training.")
    }
}

fun main(args: Array<String>) {
    var task: (Int, DatasetSource<Label>) -> Unit = { n, dataset ->
        println("Specify a side with \"Alice\" or \"Bob\"")
    }
    val datasets = mapOf<String, DatasetSource<Label>>(
        "mnist" to MNIST.MNIST,
        "boston-housing" to BostonHousing,
        "titanic" to Titanic,
        "comparable" to ComparableDatasetInstance
    )
    var dataset: DatasetSource<Label> = MNIST.MNIST
    var nDatapoints = 10
    for (arg in args) {
        if (arg.lowercase() == "alice") {
            task = Alice::run
        } else if (arg.lowercase() == "bob") {
            task = Bob::run
        } else if (arg.lowercase().startsWith("n=")) {
            nDatapoints = Integer.parseInt(arg.substring(2))
        } else if (arg.lowercase().startsWith("dataset=")) {
            dataset = datasets[arg.lowercase().substring("dataset=".length)]!!
        } else {
            throw IllegalArgumentException("Unknown argument $arg")
        }
    }
    println("Starting with $nDatapoints data points.")
    println("Dataset has ${dataset.loadDataset(0, 1).nAttributes} attributes.")
    println("Dataset  ${dataset.datasetName} has ${dataset.dataSize} entries")
    task(nDatapoints, dataset)
}
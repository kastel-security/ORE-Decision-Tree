package decisiontree.datasets.other

import decisiontree.data.Dataset
import decisiontree.datasets.DatasetSource
import decisiontree.datasets.GenericData
import java.lang.Float
import kotlin.random.Random

val GlassDataset = ICSUCIDataset("glass", 10)
val IonosphereDataset = ICSUCIDataset("ionosphere", 34)
val WineDataset = ICSUCIDataset("wine", 13)

class ICSUCIDataset(private val name: String, private val nAttributes: Int): DatasetSource<String> {

    override fun loadDataset(skip: Int, max: Int): Dataset<String> {
        val rand = Random(42)
        val datasetStream = DatasetSource.loadFileCached("https://archive.ics.uci.edu/ml/machine-learning-databases/$name/$name.data", "$name.data")
        return datasetStream.bufferedReader().useLines {
            val dataPoints = it.filterNot(String::isEmpty).map {
                val elements = it.split(',')
                val type = elements[nAttributes]
                val data = GenericData(Array(nAttributes) {
                    elements[it].parse()
                })
                data to type
            }.shuffled(rand).take(max).drop(skip).toList()
            Dataset(dataPoints, nAttributes)
        }
    }

    private fun String.parse(): Int {
        return Float.floatToIntBits(toFloat()) ushr 1
    }

    override fun getAttributeBitSize(): Int = 31

    override fun getDatasetName(): String = name
    override fun isTestSet(): Boolean = false

    override fun getOther(): DatasetSource<String> = this
}
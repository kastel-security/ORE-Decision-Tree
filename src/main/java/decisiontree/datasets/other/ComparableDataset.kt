package decisiontree.datasets.other

import decisiontree.data.Data
import decisiontree.data.Dataset
import decisiontree.datasets.DatasetSource
import decisiontree.datasets.GenericData
import java.util.*
import kotlin.collections.ArrayList

/**
 * Dataset created to be comparable to the one in https://petsymposium.org/popets/2023/popets-2023-0021.pdf
 * height h
 * samples n
 * attributes m
 */
class ComparableDataset(nElements: Int, private val nAttributes: Int, nLabels: Int): DatasetSource<Int> {

    private val samples = ArrayList<Pair<Data, Int>>(nElements)

    init {
        val rand = Random(42);
        for (i in 0 until nElements) {
            val data = GenericData(Array(nAttributes) {
                rand.nextInt(1 shl attributeBitSize)
            })
            samples.add(data to rand.nextInt(nLabels))
        }
    }

    override fun loadDataset(skip: Int, max: Int): Dataset<Int> {
        val adjustedMax: Int = if (max + skip < 0 || max + skip > samples.size) {
            samples.size
        } else {
            max + skip
        }
        return Dataset(samples.subList(skip, adjustedMax), nAttributes)

    }

    override fun getDatasetName(): String = "Comparable Dataset"
    override fun isTestSet(): Boolean = false
    override fun getOther(): DatasetSource<Int> = this
    override fun getAttributeBitSize(): Int = 8
}

val ComparableDatasetInstance = ComparableDataset(1 shl 13, 7, 3)
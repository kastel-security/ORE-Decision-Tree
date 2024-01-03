package decisiontree.datasets.other

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import decisiontree.data.Dataset
import decisiontree.datasets.DatasetSource
import decisiontree.datasets.GenericData
import java.lang.Float

abstract class CsvDataset<L>(private val columns: Array<String>, val source: String): DatasetSource<L> {
    override fun loadDataset(skip: Int, max: Int): Dataset<L> {
        val source = DatasetSource.loadFileCached(source, "$datasetName.csv")
        val data = csvReader().open(source.buffered()) {
            readAllWithHeaderAsSequence()
                .filterNot { line -> columns.any { line[it]!!.isEmpty() } }
                .take(max)
                .drop(skip)
                .map { line ->
                    GenericData(
                        columns.map{
                            line[it]!!.parse()
                        }.toTypedArray()
                    ) to (getLabel(line))
                }
                .toList()
        }
        return Dataset(data, columns.size)
    }

    private fun String.parse(): Int {
        return Float.floatToIntBits(toFloat()) ushr 1
    }

    override fun getAttributeBitSize(): Int = 31

    override fun isTestSet(): Boolean = false

    override fun getOther(): DatasetSource<L> = this

    abstract fun getLabel(csvData: Map<String, String>): L
}
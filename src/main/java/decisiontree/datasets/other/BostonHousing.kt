package decisiontree.datasets.other

object BostonHousing: CsvDataset<Int>(
    arrayOf("crim", "rm", "nox", "age", "dis", "b", "lstat"),
    "https://raw.githubusercontent.com/selva86/datasets/master/BostonHousing.csv"
) {
    override fun getLabel(csvData: Map<String, String>): Int {
        val medianValue = csvData["medv"]!!.toFloat().toInt()
        return medianValue / 10
    }

    override fun getDatasetName(): String = "boston-housing"

}
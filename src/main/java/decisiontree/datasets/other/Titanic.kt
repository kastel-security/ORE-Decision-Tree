package decisiontree.datasets.other

object Titanic: CsvDataset<Int>(
    arrayOf("Pclass", "Age", "SibSp", "Parch", "Fare"), //We only consider numeric attributes
    "https://github.com/datasciencedojo/datasets/raw/master/titanic.csv"
    ) {
    override fun getLabel(csvData: Map<String, String>): Int {
        return csvData["Survived"]!!.toInt()// != 0
    }

    override fun getDatasetName(): String = "titanic"
}
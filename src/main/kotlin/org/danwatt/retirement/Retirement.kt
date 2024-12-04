package org.danwatt.retirement

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.ColType
import org.jetbrains.kotlinx.dataframe.io.readCSV

class Retirement {

    fun loadMutualFundData(file: String) : DataFrame<Any?>{
        //Date	Open	High	Low	Close	Adj	Close
        val types = mapOf(
            "Date" to ColType.LocalDate,
            "Open" to ColType.Double,
            "High" to ColType.Double,
            "Low" to ColType.Double,
            "Close" to ColType.Double,
            "Adj CLose" to ColType.Double
        )
        return DataFrame.readCSV(
            fileOrUrl = Retirement::class.java.classLoader.getResource(file).file,
           colTypes = types,
            delimiter = '\t'
        )
            .sortBy("Date")
            .add("Year") { (it["Date"] as LocalDate).toJavaLocalDate().atTime(0, 0, 0, 0).withDayOfYear(1).year }
            .groupBy("Year")
            .aggregate {
                it.first()["Open"] as Double? into "Open"
                it.last()["Close"] as Double? into "Close"
            }
            .add("DeltaPercentage") {
                if (index() == 0) null else ((it["Close"] as Double) / (it["Open"] as Double))
            }
    }

    fun loadAndAggregateSAP(): DataFrame<Any?> {
        val annualDF = DataFrame.readCSV(
            fileOrUrl = Retirement::class.java.classLoader.getResource("sap500.csv").file,
            colTypes = mapOf(
                "Date" to ColType.LocalDate,
                "Open" to ColType.Double,
                "High" to ColType.Double,
                "Low" to ColType.Double,
                "Close" to ColType.Double,
                "Volume" to ColType.Long
            )
        ).sortBy("Date")
            .add("Year") { (it["Date"] as LocalDate).toJavaLocalDate().atTime(0, 0, 0, 0).withDayOfYear(1).year }
            .groupBy("Year")
            .aggregate {
                (if (it.first()["Open"] == 0.0)  it.first()["Close"] as Double else it.first()["Open"] as Double?) into "Open"
                it.last()["Close"] as Double? into "Close"
            }
            .add("DeltaPercentage") {
                ((it["Close"] as Double) / (it["Open"] as Double))
            }
        return annualDF
    }
}

fun main() {
    Retirement().loadAndAggregateSAP()
}
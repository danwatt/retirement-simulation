package org.danwatt.retirement

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage.withPercentage
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.math.median
import org.junit.jupiter.api.Test

private val Pair<Int, Pair<Double,Double>>.ben: Double
    get() =this.second.first

private val Pair<Int, Pair<Double,Double>>.joey: Double
    get() =this.second.second

class BenAndJoeyTests {

    /* First, test existing, known values from books and publications to make sure we are on the same page
    with the math
     */
    @Test
    // Financial Peace ~1995
    fun oldBenAuthor() {
        val expectedReturn = 1.10 // 10% every year
        val returns = (1..47).map { listOf(expectedReturn) } // Repeat 10% for 47 years
        val (ben,arthur) = BenAndJoey().simulate(
            benStartAge = 22,
            benYearsToContribute = 8,
            endAge = 65,
            annualContribution = 1000.0,
            yearsAndDeltas = returns
        )
        assertThat(ben).isCloseTo(388865.0, withPercentage(0.01))
        assertThat(arthur).isCloseTo(329039.0, withPercentage(0.01))
    }

    @Test
    // Newer versions of Financial Peace, FPU, Foundations, etc
    fun benArthur() {
        val expectedReturn = 1.12
        val returns = (1..47).map { listOf(expectedReturn) }
        val (ben,arthur) = BenAndJoey().simulate(
            benStartAge = 19,
            benYearsToContribute = 8,
            endAge = 65,
            annualContribution = 2000.0,
            yearsAndDeltas = returns
        )
        assertThat(ben).isCloseTo(2288996.0, withPercentage(0.01))
        assertThat(arthur).isCloseTo(1532166.0, withPercentage(0.01))
    }

    @Test
    //Website as of late 2024
    fun benJoey() {
        val expectedReturn = 1.11
        val returns = (1..48).map { listOf(expectedReturn) }
        val (ben,joey) = BenAndJoey().simulate(
            benStartAge = 21,
            benYearsToContribute = 9,
            endAge = 67,
            annualContribution = 2400.0,
            yearsAndDeltas = returns
        )
        assertThat(ben).isCloseTo(2105826.0, withPercentage(0.01))
        assertThat(joey).isCloseTo(1232318.0, withPercentage(0.01))
    }

    /* Now lets simulate some alternative scenarios */

    @Test
    fun benJoeyWithLoadedFunds() {
        val expectedReturn = 1.11
        val returns = (1..48).map { listOf(expectedReturn) }
        //In my own personal experience, customers sent to an investing professional are directed to purchase A-class
        // shares of American Funds, which have up-front loads.
        //https://www.capitalgroup.com/advisor/investments/share-class-information/share-class-pricing.html
        //Technically the loads decrease as (purchase+AUM) increases. This can be updated to support that
//        Less than $25,000	5.75%
//        $25,000 to $50,000	5.00%
//        $50,000 to $100,000	4.50%
//        $100,000 to $250,000	3.50%
//        $250,000 to $500,000	2.50%
//        $500,000 to $750,000	2.00%
//        $750,000 to $1 million	1.50%
//        $1 million and above	0.00%†
        val load = 0.0575

        val (ben,joey) = BenAndJoey().simulate(
            benStartAge = 21,
            benYearsToContribute = 9,
            endAge = 67,
            annualContribution = 2400.0,
            yearsAndDeltas = returns,
            load = load
        )
        assertThat(ben).isCloseTo(1876177.45, withPercentage(0.01)) // vs 2105826
        assertThat(joey).isCloseTo(1181367.33, withPercentage(0.01)) // vs 1232318

    }

    @Test
    fun simulateMultipleYearsSP500() {
        val sapDF = Retirement().loadAndAggregateSAP()
        val rates = sapDF.map { it["Year"] as Int to it["DeltaPercentage"] as Double? }.toList()

        val benToJoeyDelay = 9

        val allCompetitions = (1928..1978).map { year ->
            val start = year-1928
            val r= rates.subList(start,start+47)
            val returns = r.map { listOf(it.second!!) }.toList()
            val (ben,joey) = BenAndJoey().simulate(
                benStartAge = 21,
                benYearsToContribute = benToJoeyDelay,
                endAge = 67,
                annualContribution = 2400.0,
                yearsAndDeltas = returns,
                allocations = listOf(element = 1.0),
                load = 0.0
            )
            year to (ben to joey)
        }

        val benWinners = allCompetitions.filter { it.ben > it.joey }
        val joeyWinners = allCompetitions.filter { it.ben < it.joey }
        val benStartYear = allCompetitions.maxBy { it.ben }
        val joeyStartYear = allCompetitions.maxBy { it.joey }
        val benWorstYear = allCompetitions.minBy { it.ben }
        val joeyWorstYear = allCompetitions.minBy { it.joey }
        val biggestDifference = allCompetitions.maxBy { it.ben / it.joey }

        val benMedian = allCompetitions.map { it.ben }.median()
        val joeyMedian = allCompetitions.map { it.joey }.median()
        println("Medians. Ben: $benMedian, Joey: $joeyMedian")

        val benAvg = allCompetitions.map { it.ben }.average()
        val joeyAvg = allCompetitions.map { it.joey }.average()
        println("Averages. Ben: $benAvg, Joey: $joeyAvg")

        println("Ben won ${benWinners.size} times, Joey ${joeyWinners.size}")
        println("Ben's best year was if he started in ${benStartYear.first}, earning ${benStartYear.ben}")
        println("Joey's best year was if he started in ${joeyStartYear.first+benToJoeyDelay}, earning ${joeyStartYear.joey}")

        println("Ben's worst year was if he started in ${benWorstYear.first}, earning ${benWorstYear.ben}")
        println("Joey's worst year was if he started in ${joeyWorstYear.first+benToJoeyDelay}, earning ${joeyWorstYear.joey}")

        println("The biggest difference was if Ben started in ${biggestDifference.first}, with Ben earning ${biggestDifference.ben} and Joey, starting ${benToJoeyDelay} years later at ${biggestDifference.joey}")
    }

    //TODO: Run the same simulation, but instead of S&P500, use the 4-fund mix at American funds.
}

package org.danwatt.retirement

class BenAndJoey {
    fun simulate(
        benStartAge: Int,
        benYearsToContribute: Int,
        endAge: Int,
        annualContribution: Double, // For Ben/Joey, 2400. Older Ben Arthur, 2000
        yearsAndDeltas: List<List<Double>>, // each row (outer list) is one year, each inner list is deltas for each allocated fund
        // for a single fund, the inner lists will have size 1
        allocations: List<Double> = listOf(1.0),//100% allocated into 1 fund. But, when testing the American Funds scenario, this should be 0.25, 0.25, 0.25, 0.25
        load: Double = 0.0 // Front-loaded sales charge.
    ): Pair<Double, Double> {
        val benYears = benStartAge..(benStartAge + benYearsToContribute)
        val joeyYears = (benStartAge + benYearsToContribute..endAge)

        val benContributions = (benYears).map { annualContribution }.toList() + (joeyYears).map { 0.0 }
        val joeyContributions = (benYears).map { 0.0 }.toList() + (joeyYears).map { annualContribution }

        val ben = benContributions.compute(yearsAndDeltas, load)
        val joey = joeyContributions.compute(yearsAndDeltas, load)

        return ben to joey
    }
}

private fun List<Double>.compute(yearsAndDeltas: List<List<Double>>, load: Double): Double =
    this.reduceIndexed { index, acc, value ->
        val delta = yearsAndDeltas[index - 1]
        if (index == 1) {
            (value - value * load) * delta[0]
        } else {
            (acc + value - value * load) * delta[0]
        }
    }

package org.danwatt.retirement

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.print
import org.junit.jupiter.api.Test

class RetirementTests {

    @Test
    fun test() {
        val annualData = Retirement().loadAndAggregateSAP()
        val filtered = annualData.filter { "Year"<Int>() in 2020..2023 }
        filtered.print()

        assertThat(filtered[0]["Year"]).isEqualTo(2020)
        assertThat(filtered[0]["DeltaPercentage"] as Double).isCloseTo(1.157612, Percentage.withPercentage(0.01))
    }

    @Test
    fun aivsx() {
        val annualData = Retirement().loadMutualFundData("aivsx.txt")
        val filtered = annualData.filter { "Year"<Int>() in 2020..2023 }
        filtered.print()

        assertThat(filtered[0]["Year"]).isEqualTo(2020)
        assertThat(filtered[0]["DeltaPercentage"] as Double).isCloseTo(1.307141, Percentage.withPercentage(0.01))
    }
}
/*
https://www.macrotrends.net/2526/sp-500-historical-annual-returns
Year	Average
Closing Price	Year Open	Year High	Year Low	Year Close	Annual
% Change
2024	5,381.04	4,742.83	6,049.88	4,688.68	6,049.88	26.84%
2023	4,283.73	3,824.14	4,783.35	3,808.10	4,769.83	24.23%
2022	4,097.49	4,796.56	4,796.56	3,577.03	3,839.50	-19.44%
2021	4,273.41	3,700.65	4,793.06	3,700.65	4,766.18	26.89%
2020	3,217.86	3,257.85	3,756.07	2,237.40	3,756.07	16.26%

From this file:
   Year        Open       Close DeltaPercentage DeltaAsRelative
 0 2020 3244.669922 3756.070068        1.157612        0.157612
 1 2021 3764.610107 4766.180176        1.266049        0.266049
 2 2022 4778.140137 3839.500000        0.803555       -0.196445
 3 2023 3853.290039 4769.830078        1.237859        0.237859
 */
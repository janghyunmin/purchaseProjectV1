package run.piece.dev.widget.utils

import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.toDecimalComma

/**
 *packageName    : com.bsstandard.piece.widget.utils
 * fileName       : ConvertMoney
 * author         : piecejhm
 * date           : 2022/08/19
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/08/19        piecejhm       최초 생성
 */
class ConvertMoney {
    fun getNumKorString(value: Long): String {
        var unitWords = listOf("", "억 ", "만")
        var splitUnit = 10000
        var splitCount = unitWords.size
        var resultArray =  ArrayList<Int>();
        var resultString = ""

        for (i in 0 until splitCount) {
            var unitResult = (value % Math.pow(
                splitUnit.toDouble(),
                (i + 1).toDouble()
            )) / Math.pow(splitUnit.toDouble(), i.toDouble())
            unitResult = Math.floor(unitResult)

            if (unitResult > 0) {
                resultArray.add(unitResult.toInt())
            }
        }

        for (index in 0 until resultArray.size) {
            resultString = resultArray[index].toString() + unitWords[index] + resultString
        }
        return resultString
    }
    fun getMoneyUnitToString(value: Long, width: Int? = null): String {
        val splitUnit = 99999
        var resultString = ""

        if (value > splitUnit) { // > 99999
            when(value) {

                in value .. 999999 -> { // <= 99만
                    val data = "$value".subSequence(0, 2).toString()
                    return "${data.toInt().decimalComma()}만"
                }
                in value .. 9999999 -> { // ~ <= 999만
                    val data = "$value".subSequence(0, 3).toString()
                    return "${data.toInt().decimalComma()}만"
                }
                in value .. 99999999 -> { // ~ 9999만
                    val data = "$value".subSequence(0, 4).toString()
                    return "${data.toInt().decimalComma()}만"
                }
                in value .. 1000000000000 -> { //1억 ~ 1조
                    val rem = value.rem(100000000)
                    val div = value.div(100000000)

                    val divData = "$div".toDecimalComma() // 1,923
                    val remData = "$rem"

                    if (remData.length >= 2) {
                        width?.let {
                            return "$divData.${remData.substring(0, 1)}억"
                        }?: run {
                            return "$divData.${remData.substring(0, 1)}억"
                        }
                    }
                    return "${divData}억"
                }
            }
        } else resultString = value.decimalComma()
        return resultString
    }
}

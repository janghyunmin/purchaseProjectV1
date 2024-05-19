package run.piece.dev.refactoring.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

fun String?.default() = this ?: ""
fun Int?.default() = this ?: 0
fun Int?.idDefault() = this ?: -1
fun <T> List<T>?.default() = this ?: emptyList()
fun Boolean?.default() = this ?: false
fun Float?.default() = this ?: 0f
fun Double?.default() = this ?: 0.0
fun Long?.default() = this ?: 0L
fun Long?.isDefault() = this ?: -1
fun Date?.default() = this ?: Date()

fun String.toBaseDateFormat(): String {
    val input = SimpleDateFormat("yyyy-mm-dd", Locale.getDefault())
    val output = SimpleDateFormat("yyyy.mm.dd", Locale.getDefault())
    val parsed = input.parse(this)
    return output.format(parsed)
}

fun String.toKoreanDateFormat(): String {
    val input = SimpleDateFormat("yyyy-mm-dd", Locale.getDefault())
    val output = SimpleDateFormat("yyyy년 mm월 dd일", Locale.getDefault())
    val parsed = input.parse(this)
    return output.format(parsed)
}

fun String.toDateFormat(style: String? = null): String {
    val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    var output = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
    var parsed = input.parse(this)

    style?.let {
        when (it) {
            "year" -> {
                parsed = input.parse(this)
                return output.format(parsed)
            }

            "month" -> {
                output = SimpleDateFormat("M월 dd일", Locale.KOREA)
                parsed = input.parse(this)
                return output.format(parsed)
            }

            "day" -> {
                output = SimpleDateFormat("dd일", Locale.KOREA)
                parsed = input.parse(this)
                return output.format(parsed)
            }

            else -> {
                parsed = input.parse(this)
                return output.format(parsed)
            }
        }
    }

    return output.format(parsed)
}

fun String.toDotDateFormat(): String {
    val input = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val output = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    val parsed = input.parse(this)
    return output.format(parsed)
}

fun String.toBasicDateFormat(): String {
    val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s", Locale.US)
    val output = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    val parsed = input.parse(this)
    return output.format(parsed)
}


fun String.toDateTimeFormat(style: String? = null): String {
    val input = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    var output = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
    var parsed = input.parse(this)

    style?.let {
        when (it) {
            "year" -> {
                output = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault())
                parsed = input.parse(this)
                return output.format(parsed)
            }

            "month" -> {
                output = SimpleDateFormat("M월 d일 HH:mm", Locale.getDefault())
                parsed = input.parse(this)
                return output.format(parsed)
            }

            "day" -> {
                output = SimpleDateFormat("dd일 HH:mm", Locale.getDefault())
                parsed = input.parse(this)
                return output.format(parsed)
            }

            else -> {}
        }
    }
    return output.format(parsed)
}

fun Int.decimalComma(): String = DecimalFormat("#,###").format(this)
fun Long.decimalComma(): String = DecimalFormat("#,###").format(this)

fun String.toDecimalComma(): String {
    var data = this

    if (this.contains(".")) {
        val tempData = this.toDouble()
        data = tempData.roundToLong().toString()
    }

    val decimal = DecimalFormat("###,###")
    data = decimal.format(data.toIntOrNull() ?: 0)
    return data
}

fun String.toInt(): Int {
    if (this.isNotEmpty()) {
        return Integer.parseInt(this)
    }
    return 0
}

fun String.toDouble(): Double {
    if (this.isNotEmpty()) {
        return java.lang.Double.parseDouble(this)
    }
    return 0.0
}

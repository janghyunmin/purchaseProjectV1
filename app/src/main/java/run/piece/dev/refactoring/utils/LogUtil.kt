package run.piece.dev.refactoring.utils

import android.annotation.SuppressLint
import android.util.Log
import run.piece.dev.BuildConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LogUtil {
    companion object {
        private const val TAG = "PIECE"
        private const val PREFIX = BuildConfig.BUILD_TYPE

        @Synchronized
        fun v(text: String) {
            when(BuildConfig.FLAVOR) {
                "dev","stage" ->  Log.v(TAG, getDecoratedLog(text))
            }
        }

        @Synchronized
        fun d(text: String) {
            when(BuildConfig.FLAVOR) {
                "dev","stage" ->  Log.d(TAG, getDecoratedLog(text))
            }
        }

        @Synchronized
        fun i(text: String) {
            when(BuildConfig.FLAVOR) {
                "dev","stage" ->  Log.i(TAG, getDecoratedLog(text))
            }
        }

        @Synchronized
        fun w(text: String) {
            when(BuildConfig.FLAVOR) {
                "dev","stage" ->  Log.w(TAG, getDecoratedLog(text))
            }
        }

        @Synchronized
        fun e(text: String) {
            when(BuildConfig.FLAVOR) {
                "dev","stage" -> Log.e(TAG, getDecoratedLog(text))
            }
        }

        @SuppressLint("SimpleDateFormat")
        private fun getDecoratedLog(text: String): String {
            val sb = StringBuilder()
            if(BuildConfig.DEBUG) {
                val ste = Thread.currentThread().stackTrace[4]
                val lineNumber: Int = ste.lineNumber
                val currentTime = System.currentTimeMillis() // 현재 시간을 msec 단위로 얻음
                val todayDate = Date(currentTime) // 현재 시간 Date 변수에 저장
                val sDFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val strSDFormatDay = sDFormat.format(todayDate) // 'yyyy-MM-dd HH:mm' 형태로 포맷 변경

                sb.append("==================================================================================================================")
                sb.append("\n")
                sb.append("BuildType : [$PREFIX]")
                sb.append("\n")
                sb.append("LogText : [$text]")
                sb.append("\n")
                sb.append("Time : [${TimeUtil.getTimeAsString(strSDFormatDay)}]")
                sb.append("\n")
                sb.append("FileName : [${ste.fileName.replace(".java", "")}]")
                sb.append("\n")
                sb.append("LineNumber : [${lineNumber}]")
                sb.append("\n")
                sb.append("==================================================================================================================")
            }

            return sb.toString()
        }
    }
}

class TimeUtil {
    companion object {

        @Synchronized
        fun getTimeAsLong(): Long {
            val calendar = Calendar.getInstance()
            return calendar.timeInMillis
        }

        @Synchronized
        fun getTimeAsString(format: String): String {
            val date = Date(getTimeAsLong())
            val sdf = SimpleDateFormat(format, Locale.getDefault())

            return sdf.format(date)
        }

        @Synchronized
        fun getTimeAsLong(format: String, text: String): Long {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                val date = sdf.parse(text)

                return date.time

            } catch (e: Exception) {
                e.printStackTrace()
            }

            return -1
        }

        @Synchronized
        fun getTimeAsString(format: String, time: Long): String {
            val date = Date(time)
            val sdf = SimpleDateFormat(format, Locale.getDefault())

            return sdf.format(date)
        }
    }
}
package run.piece.dev.refactoring

import com.google.gson.Gson
import java.io.File

class JsonFileReader<T>(private val gson: Gson, private val clazz: Class<T>) {

    fun readJsonFileAndConvertToObject(filePath: String): T? {
        return try {
            // 파일 읽기
            val jsonContent = File(filePath).readText()

            // JSON을 Kotlin 객체로 변환
            return gson.fromJson(jsonContent, clazz)

        } catch (e: Exception) {
            e.printStackTrace() // 예외 처리
            null
        }
    }

    fun readJsonFile(filePath: String): String? {
        return try {
            return File(filePath).readText()

        } catch (e: Exception) {
            e.printStackTrace() // 예외 처리
            ""
        }
    }
}
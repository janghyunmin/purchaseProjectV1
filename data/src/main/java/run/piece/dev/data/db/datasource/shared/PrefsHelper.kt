package run.piece.dev.data.db.datasource.shared

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder

class PrefsHelper private constructor(context: Context) {

    companion object {
        const val PREFERENCE_NAME = "PIECE"
        private lateinit var prefs: SharedPreferences
        private lateinit var prefsEditor: SharedPreferences.Editor
        private var instance: PrefsHelper? = null

        @Synchronized
        fun init(context: Context): PrefsHelper {
            if (instance == null) {
                instance = PrefsHelper(context)
            }

            prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            prefsEditor = prefs.edit()

            return instance!!
        }

        @Synchronized
        fun read(key: String, defValue: String): String {
            return prefs.getString(key, defValue) ?: ""
        }

        @Synchronized
        fun write(key: String, value: String?) {
            prefsEditor.putString(key, value)
            prefsEditor.apply()
        }

        @Synchronized
        fun removeKey(key: String) {
            prefsEditor.remove(key)
            prefsEditor.apply()
        }

        @Synchronized
        fun removeAll() {
            prefsEditor.clear()
            prefsEditor.apply()
        }

        @Synchronized
        fun removeToken(key: String) {
            prefsEditor.remove(key)
            prefsEditor.apply()
        }

        // Integer Shared - jhm 2022/07/04
        @Synchronized
        fun read(key: String, defValue: Int): Int {
            return prefs.getInt(key, defValue)
        }

        @Synchronized
        fun write(key: String, value: Int) {
            prefsEditor.putInt(key, value).apply()
        }

        // boolean Shared - jhm 2022/07/04
        @Synchronized
        fun read(key: String, defValue: Boolean): Boolean {
            return prefs.getBoolean(key, defValue)
        }

        @Synchronized
        fun write(key: String, value: Boolean) {
            prefsEditor.putBoolean(key, value)
            prefsEditor.apply()
        }
    }
}
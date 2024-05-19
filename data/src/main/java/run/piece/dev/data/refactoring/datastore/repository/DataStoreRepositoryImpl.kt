package run.piece.dev.data.refactoring.datastore.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.datastore.DataStoreRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
private const val PREFERENCES_NAME = "PIECE"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME,
    scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)

//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
//    name = PREFERENCES_NAME,
//    corruptionHandler = ReplaceFileCorruptionHandler(
//        produceNewData = { emptyPreferences() }
//    ),
//    scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
//    produceMigrations = {
//            context -> listOf(SharedPreferencesMigration(context = context, PREFERENCES_NAME))
//    }
//)


class DataStoreRepositoryImpl @Inject constructor(
    private val context: Context
) : DataStoreRepository {

    // Shared 방식
    override suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun getString(key: String): String {
        return try {
            val preferencesKey = stringPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            preferences[preferencesKey].default()

        } catch (e: Exception){
            e.printStackTrace()
            ""
        }
    }

    override suspend fun getInt(key: String): Int {
        return try {
            val preferencesKey = intPreferencesKey(key)
            val preferences = context.dataStore.data.first()
            preferences[preferencesKey].default()
        } catch (e: Exception){
            e.printStackTrace()
            0
        }
    }



    // Flow 방식
    // 앱 첫 실행 여부 판별
    override suspend fun setFirstLaunch(key: Boolean) {
        context.dataStore.edit {
            it[firstLaunchKey] = key
        }
    }
    override fun getFirstLaunch(): Flow<Boolean> {
        return context.dataStore.data.catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            val firstLaunch = it[firstLaunchKey] ?: true
            firstLaunch
        }
    }

    override suspend fun setInvestFirstStatus(key: Boolean) {
        context.dataStore.edit {
            it[investFirstStatus] = key
        }
    }
    override fun getInvestFirstStatus(): Flow<Boolean> {
        return context.dataStore.data.catch { exception ->
            if(exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map {
            val investFirstStatus = it[investFirstStatus] ?: false
            investFirstStatus
        }
    }


    override suspend fun setFbToken(fbToken: String) {
        Result.runCatching {
            context.dataStore.edit { preferences ->
                preferences[fbTokenKey] = fbToken
            }
        }
    }

    override suspend fun getFbToken(): Result<String> {
        return Result.runCatching {
            val flow = context.dataStore.data.catch { exception ->
                if(exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
                .map { preferences ->
                    preferences[fbTokenKey]
                }
            val value = flow.firstOrNull() ?: ""
            value
        }
    }


    // DataStore KeySetting List
    companion object {
        val firstLaunchKey = booleanPreferencesKey(name = "firstLaunch")
        val fbTokenKey = stringPreferencesKey(name = "fbToken")
        val investFirstStatus = booleanPreferencesKey(name = "investFirstStatus")
    }

}
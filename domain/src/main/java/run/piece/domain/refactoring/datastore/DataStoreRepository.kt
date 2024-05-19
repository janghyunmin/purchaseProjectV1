package run.piece.domain.refactoring.datastore

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {

    // Shared 방식
    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun getString(key: String): String
    suspend fun getInt(key:String): Int

    // Flow 방식
    suspend fun setFirstLaunch(key: Boolean)
    fun getFirstLaunch(): Flow<Boolean>
    suspend fun setFbToken(fbToken: String)
    suspend fun getFbToken(): Result<String>

    suspend fun setInvestFirstStatus(key: Boolean)
    fun getInvestFirstStatus(): Flow<Boolean>
}
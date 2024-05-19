package run.piece.dev.data.repository

import android.app.Application
import run.piece.dev.data.api.NetworkInfo
import run.piece.dev.data.api.RetrofitService
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.AccountDTO




/**
 *packageName    : com.bsstandard.piece.data.repository
 * fileName       : AccountRepository
 * author         : piecejhm
 * date           : 2022/09/30
 * description    : 회원 계좌 정보 조회 요청 Repository
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/09/30        piecejhm       최초 생성
 */

class AccountRepository(application: Application) {
    val response = NetworkInfo.getRetrofit().create(RetrofitService::class.java)
    val accessToken: String = PrefsHelper.read("accessToken","")
    val deviceId: String = PrefsHelper.read("deviceId","")
    val memberId:String = PrefsHelper.read("memberId","")

    suspend fun getMemberAccount(accessToken: String, deviceId: String, memberId: String) : AccountDTO {
        return response.getMemberAccount(accessToken = accessToken , deviceId = deviceId, memberId = memberId)
    }

    companion object {
        private var instance: AccountRepository? = null
        fun getInstance(application: Application) : AccountRepository? {
            if(instance == null) instance = AccountRepository(application)
            return instance
        }
    }
}
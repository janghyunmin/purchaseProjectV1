package run.piece.dev.data.repository

import android.app.Application
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import run.piece.dev.data.api.NetworkInfo
import run.piece.dev.data.api.RetrofitService
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.MemberDTO

/**
 *packageName    : com.bsstandard.piece.data.repository
 * fileName       : GetMemberUpdateRepository
 * author         : piecejhm
 * date           : 2022/09/06
 * description    : 회원 정보 조회 Repository
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/09/06        piecejhm       최초 생성
 */
class GetUserRepository(application: Application) {
    val response = NetworkInfo.getRetrofit().create(RetrofitService::class.java)
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    fun getUser(): Observable<MemberDTO?>? = response
        .getMember("Bearer $accessToken", deviceId, memberId)
        .subscribeOn(Schedulers.io())
        .retry(1)
        .observeOn(AndroidSchedulers.mainThread())

    // singleton pattern - jhm 2022/08/15
    companion object {
        private var instance: GetUserRepository? = null

        fun getInstance(application : Application): GetUserRepository? {
            if (instance == null) instance = GetUserRepository(application)
            return instance
        }
    }
}
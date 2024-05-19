package run.piece.dev.data.api

import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.authentication.CallUserNameSsnAuthModel
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.authentication.CallUsernameAuthModel
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.document.MemberPortfolioDocument
import run.piece.dev.data.model.*
import java.util.*
interface RetrofitService {
    @GET("member")
    fun getMember(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
    ): Observable<MemberDTO>

    // 회원 포트폴리오 소유증서 신청 요청  - jhm 2022/10/31
    @POST("member/document")
    fun postDocument(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body memberPortfolioDocument: MemberPortfolioDocument?
    ) : Call<BaseDTO>

    // 회원 예치금 잔액 조회 요청 - jhm 2022/09/20
    @GET("deposit/balance")
    suspend fun getDepositBalance(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ): DepositDTO

    // 회원 분배금 예치금 전환 - jhm 2022/10/26
    @POST("deposit/balance")
    fun postDepositBalance(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ): Call<BaseDTO>


    // 회원 계좌 정보 조회 요청 - jhm 2022/09/30
    @GET("deposit/member/bank/account")
    suspend fun getMemberAccount(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ) : AccountDTO

    // 주민등록번호 실명인증 요청 - jhm 2022/10/26
    @POST("member/call_username_auth")
    fun postUserNameAuth(
        @Body callUsernameAuthModel: CallUsernameAuthModel
    ): Call<BaseDTO>

    // 실명인증 V2 - jhm 2023/02/07
    @POST("member/v2/call_username_auth")
    fun postUserNameSsnAuth(
        @Body callUserNameSsnAuthModel: CallUserNameSsnAuthModel
    ): Call<BaseDTO>
}
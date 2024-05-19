package run.piece.dev.data.refactoring.ui.deposit.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.RegisterNhBankDto
import run.piece.domain.refactoring.deposit.model.NhBankHistoryVo
import run.piece.domain.refactoring.deposit.model.WithDrawVo

interface NhApi {
    // 가상계좌 생성
    @POST("nhbank/vaccount/regist/member")
    suspend fun postNhAccount(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body registerNhBankModel: RegisterNhBankDto?
    ) : BaseDto

    // 연동계좌 변경
    @POST("nhbank/vaccount/change/member")
    suspend fun changeAccount(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body registerNhBankModel: RegisterNhBankDto?
    ): BaseDto

    // NH 출금 신청 요청
    @POST("nhbank/deposit/amount/return")
    suspend fun withDrawNH(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body withDrawVo: WithDrawVo
    ): BaseDto


      // NH 고객 입금 확인 조회
    @POST("nhbank/vaccount/transaction/history")
    suspend fun postNhHistory(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ) : Response<NhBankHistoryVo>
}
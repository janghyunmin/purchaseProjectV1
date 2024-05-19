package run.piece.dev.data.refactoring.ui.deposit.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.deposit.dto.AccountDto
import run.piece.dev.data.refactoring.ui.deposit.dto.DepositBalanceDto
import run.piece.dev.data.refactoring.ui.deposit.dto.HistoryDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDtoV2
import run.piece.dev.data.utils.WrappedResponse

interface DepositApi {
    // 회원 계좌 정보 조회 요청
    @GET("deposit/member/bank/account")
    suspend fun getMemberAccount(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ) : WrappedResponse<AccountDto>

    // 예치금 잔액 조회
    @GET("deposit/balance")
    suspend fun getDepositBalance(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ) : WrappedResponse<DepositBalanceDto>


    // 회원 거래 내역 목록 조회
    @GET("deposit/history")
    suspend fun getDepositHistory(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Header("apiVersion") apiVersion: String?,
        @Query("searchDvn") searchDvn: String,
        @Query("page") page: Int
    ) : WrappedResponse<HistoryDto>


    // 회원 구매 목록 조회 요청 ( v1 )
    @GET("deposit/purchase")
    suspend fun getDepositPurchaseV1(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Header("apiVersion") apiVersion: String?
    ) : WrappedResponse<List<PurchaseDto>>

    // 회원 구매 목록 조회 요청 ( v2 )
    @GET("deposit/purchase")
    suspend fun getDepositPurchaseV2(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Header("apiVersion") apiVersion: String?
    ) : WrappedResponse<List<PurchaseDtoV2>>

}
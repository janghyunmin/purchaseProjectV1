package run.piece.dev.data.refactoring.ui.deposit.repository.remote

import run.piece.dev.data.refactoring.ui.deposit.dto.AccountDto
import run.piece.dev.data.refactoring.ui.deposit.dto.DepositBalanceDto
import run.piece.dev.data.refactoring.ui.deposit.dto.HistoryDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDtoV2
import run.piece.dev.data.utils.WrappedResponse

interface DepositRemoteDataSource {
    // 회원 계좌 정보 조회
    suspend fun getMemberAccount(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) : WrappedResponse<AccountDto>

    // 예치금 잔액 조회
    suspend fun getDepositBalance(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) : WrappedResponse<DepositBalanceDto>

    // 거래내역 조회
    suspend fun getDepositHistory(
        accessToken: String,
        deviceId: String,
        memberId: String,
        apiVersion: String,
        searchDvn: String,
        page: Int
    ) : WrappedResponse<HistoryDto>

    // 내지갑 - 소유조각 상세 API
    suspend fun getDepositPurchaseV1(
        accessToken: String,
        deviceId: String,
        memberId: String,
        apiVersion: String
    ) : WrappedResponse<List<PurchaseDto>>

    // 내지갑 - 소유조각 목록 API
    suspend fun getDepositPurchaseV2(
        accessToken: String,
        deviceId: String,
        memberId: String,
        apiVersion: String
    ) : WrappedResponse<List<PurchaseDtoV2>>
}
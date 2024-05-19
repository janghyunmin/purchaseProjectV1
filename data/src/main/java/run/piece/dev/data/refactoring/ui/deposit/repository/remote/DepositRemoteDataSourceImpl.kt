package run.piece.dev.data.refactoring.ui.deposit.repository.remote

import run.piece.dev.data.refactoring.ui.deposit.api.DepositApi
import run.piece.dev.data.refactoring.ui.deposit.dto.AccountDto
import run.piece.dev.data.refactoring.ui.deposit.dto.DepositBalanceDto
import run.piece.dev.data.refactoring.ui.deposit.dto.HistoryDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDtoV2
import run.piece.dev.data.utils.WrappedResponse

class DepositRemoteDataSourceImpl(private val api: DepositApi): DepositRemoteDataSource {
    override suspend fun getMemberAccount(accessToken: String, deviceId: String, memberId: String): WrappedResponse<AccountDto> =
        api.getMemberAccount(accessToken = accessToken, deviceId = deviceId , memberId = memberId)

    override suspend fun getDepositBalance(accessToken: String, deviceId: String, memberId: String): WrappedResponse<DepositBalanceDto> =
        api.getDepositBalance(accessToken = accessToken , deviceId = deviceId , memberId = memberId)

    override suspend fun getDepositHistory(accessToken: String, deviceId: String, memberId: String, apiVersion: String, searchDvn: String, page: Int): WrappedResponse<HistoryDto> =
        api.getDepositHistory(accessToken = accessToken, deviceId = deviceId, memberId = memberId, apiVersion = apiVersion, searchDvn = searchDvn ,page = page)


    override suspend fun getDepositPurchaseV1(accessToken: String, deviceId: String, memberId: String, apiVersion: String): WrappedResponse<List<PurchaseDto>> =
        api.getDepositPurchaseV1(accessToken = accessToken , deviceId = deviceId, memberId = memberId, apiVersion = apiVersion)

    override suspend fun getDepositPurchaseV2(accessToken: String, deviceId: String, memberId: String, apiVersion: String): WrappedResponse<List<PurchaseDtoV2>> =
        api.getDepositPurchaseV2(accessToken = accessToken, deviceId = deviceId , memberId = memberId , apiVersion = apiVersion)
}
package run.piece.domain.refactoring.deposit.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.deposit.model.AccountVo
import run.piece.domain.refactoring.deposit.model.DepositBalanceVo
import run.piece.domain.refactoring.deposit.model.HistoryItemVo
import run.piece.domain.refactoring.deposit.model.HistoryVo
import run.piece.domain.refactoring.deposit.model.PurchaseVo
import run.piece.domain.refactoring.deposit.model.PurchaseVoV2

interface DepositRepository {
    fun getMemberAccount(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) : Flow<AccountVo>

    fun getDepositBalance(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) : Flow<DepositBalanceVo>


    fun getDepositHistory(
        accessToken: String,
        deviceId: String,
        memberId: String,
        apiVersion: String,
        searchDvn: String,
        page: Int
    ) : Flow<List<HistoryItemVo>>

    fun getDepositPurchaseV1(
        accessToken: String,
        deviceId: String,
        memberId: String,
        apiVersion: String
    ) : Flow<List<PurchaseVo>>

    fun getDepositPurchaseV2(
        accessToken: String,
        deviceId: String,
        memberId: String,
        apiVersion: String
    ) : Flow<List<PurchaseVoV2>>
}
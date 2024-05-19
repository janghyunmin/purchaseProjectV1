package run.piece.dev.data.refactoring.ui.deposit.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToAccountVo
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToDepositBalanceVo
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToHistoryItemVo
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToPurchaseVo
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToPurchaseVoV2
import run.piece.dev.data.refactoring.ui.deposit.repository.local.DepositLocalDataSource
import run.piece.dev.data.refactoring.ui.deposit.repository.remote.DepositRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.deposit.model.AccountVo
import run.piece.domain.refactoring.deposit.model.DepositBalanceVo
import run.piece.domain.refactoring.deposit.model.HistoryItemVo
import run.piece.domain.refactoring.deposit.model.PurchaseVo
import run.piece.domain.refactoring.deposit.model.PurchaseVoV2
import run.piece.domain.refactoring.deposit.repository.DepositRepository

class DepositRepositoryImpl(
    private val localDataSource: DepositLocalDataSource,
    private val remoteDataSource: DepositRemoteDataSource
) : DepositRepository {


    override fun getMemberAccount(accessToken: String, deviceId: String, memberId: String): Flow<AccountVo> =
        flow {
            emit(
                remoteDataSource.getMemberAccount(
                    accessToken = accessToken,
                    deviceId = deviceId,
                    memberId = memberId
                ).data.mapperToAccountVo()
            )
        }

    override fun getDepositBalance(accessToken: String, deviceId: String, memberId: String): Flow<DepositBalanceVo> =
        flow {
            emit(
                remoteDataSource.getDepositBalance(
                    accessToken = accessToken,
                    deviceId = deviceId,
                    memberId = memberId
                ).data.mapperToDepositBalanceVo()
            )
        }

    override fun getDepositHistory(accessToken: String, deviceId: String, memberId: String, apiVersion: String, searchDvn: String, page: Int): Flow<List<HistoryItemVo>> =
        flow {
            emit(
                remoteDataSource.getDepositHistory(
                    accessToken = accessToken,
                    deviceId = deviceId,
                    memberId = memberId,
                    apiVersion = apiVersion,
                    searchDvn = searchDvn,
                    page = page
                ).data.result.default().mapperToHistoryItemVo()
            )
        }


    override fun getDepositPurchaseV1(accessToken: String, deviceId: String, memberId: String, apiVersion: String): Flow<List<PurchaseVo>> =
       flow {
           emit(
               remoteDataSource.getDepositPurchaseV1(
                   accessToken = accessToken,
                   deviceId = deviceId,
                   memberId = memberId,
                   apiVersion = apiVersion
               ).data.mapperToPurchaseVo()
           )
       }

    override fun getDepositPurchaseV2(accessToken: String, deviceId: String, memberId: String, apiVersion: String): Flow<List<PurchaseVoV2>> =
        flow {
            emit(
                remoteDataSource.getDepositPurchaseV2(
                    accessToken = accessToken,
                    deviceId = deviceId,
                    memberId = memberId,
                    apiVersion = apiVersion
                ).data.mapperToPurchaseVoV2()
            )
        }


}
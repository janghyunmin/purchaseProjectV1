package run.piece.dev.data.refactoring.ui.purchase.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.purchase.mapper.mapperToPurchaseInfoVo
import run.piece.dev.data.refactoring.ui.purchase.mapper.mapperToPurchaseVo
import run.piece.dev.data.refactoring.ui.purchase.repository.local.PurchaseLocalDataSource
import run.piece.dev.data.refactoring.ui.purchase.repository.remote.PurchaseRemoteDataSource
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.model.PurchaseDefaultVo
import run.piece.domain.refactoring.purchase.model.PurchaseInfoVo
import run.piece.domain.refactoring.purchase.model.PurchaseModel
import run.piece.domain.refactoring.purchase.repository.PurchaseRepository

class PurchaseRepositoryImpl(
    private val purchaseLocalDataSource: PurchaseLocalDataSource,
    private val purchaseRemoteDataSource: PurchaseRemoteDataSource
) : PurchaseRepository {
    override fun getPurchaseInfo(
        accessToken: String,
        deviceId: String,
        memberId: String,
        portfolioId: String
    ): Flow<PurchaseInfoVo> = flow {
            emit(purchaseRemoteDataSource.getPurchaseInfo(accessToken, deviceId, memberId, portfolioId).data.mapperToPurchaseInfoVo())
    }

    override fun purchaseOffer(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseModel: PurchaseModel
    ): Flow<PurchaseDefaultVo> = flow {
        emit(purchaseRemoteDataSource.purchaseOffer(accessToken, deviceId, memberId, purchaseModel).data.mapperToPurchaseVo())
    }

    override fun purchaseCancel(
        accessToken: String,
        deviceId: String,
        memberId: String,
        model: PurchaseCancelModel
    ): Flow<BaseVo> = flow {
        emit(purchaseRemoteDataSource.purchaseCancel(accessToken, deviceId, memberId, model).mapperToPurchaseVo())
    }
}
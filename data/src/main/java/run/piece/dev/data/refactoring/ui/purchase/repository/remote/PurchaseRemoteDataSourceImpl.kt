package run.piece.dev.data.refactoring.ui.purchase.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.purchase.api.PurchaseApi
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseInfoDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.model.PurchaseModel
class PurchaseRemoteDataSourceImpl(private val api: PurchaseApi) : PurchaseRemoteDataSource {
    override suspend fun purchaseOffer(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseModel: PurchaseModel
    ): WrappedResponse<PurchaseDto?> = api.postPurchaseOffer(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        purchaseModel = purchaseModel
    )

    override suspend fun purchaseCancel(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseCancelModel: PurchaseCancelModel
    ): BaseDto = api.cancelPurchase(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        purchaseCancelModel = purchaseCancelModel
    )

    override suspend fun getPurchaseInfo(accessToken: String,
                                         deviceId: String,
                                         memberId: String,
                                         portfolioId: String
    ): WrappedResponse<PurchaseInfoDto> = api.getPurchaseInfo(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        portfolioId = portfolioId
    )
}
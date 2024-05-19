package run.piece.dev.data.refactoring.ui.purchase.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseInfoDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.model.PurchaseModel

interface PurchaseRemoteDataSource {
    // 청약 신청
    suspend fun purchaseOffer(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseModel: PurchaseModel
    ) : WrappedResponse<PurchaseDto?>

    // 청약 취소
    suspend fun purchaseCancel(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseCancelModel: PurchaseCancelModel
    ) : BaseDto

    suspend fun getPurchaseInfo(
        accessToken: String,
        deviceId: String,
        memberId: String,
        portfolioId: String
    ): WrappedResponse<PurchaseInfoDto>
}
package run.piece.domain.refactoring.purchase.usecase

import run.piece.domain.refactoring.purchase.model.PurchaseModel
import run.piece.domain.refactoring.purchase.repository.PurchaseRepository

// 청약 신청 UseCase
class PurchaseOfferUseCase(private val repository: PurchaseRepository) {
    operator fun invoke(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseModel: PurchaseModel
    ) = repository.purchaseOffer(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        purchaseModel = purchaseModel
    )
}
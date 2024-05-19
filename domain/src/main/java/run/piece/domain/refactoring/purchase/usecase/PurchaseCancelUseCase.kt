package run.piece.domain.refactoring.purchase.usecase

import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.repository.PurchaseRepository

class PurchaseCancelUseCase(private val repository: PurchaseRepository) {
    fun deletePurchaseUseCase(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseCancelModel: PurchaseCancelModel
    ) = repository.purchaseCancel(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        model = purchaseCancelModel
    )
}
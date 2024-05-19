package run.piece.domain.refactoring.purchase.usecase

import run.piece.domain.refactoring.purchase.repository.PurchaseRepository
class PurchaseInfoUseCase(private val repository: PurchaseRepository) {
    operator fun invoke(
        accessToken: String,
        deviceId: String,
        memberId: String,
        portfolioId: String
    ) = repository.getPurchaseInfo(accessToken, deviceId, memberId, portfolioId)
}
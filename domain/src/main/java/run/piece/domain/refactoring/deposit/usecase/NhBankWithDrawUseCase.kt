package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.model.WithDrawVo
import run.piece.domain.refactoring.deposit.repository.NhBankRepository

class NhBankWithDrawUseCase(private val repository: NhBankRepository) {
    fun withDrawAmountReturn(
        accessToken: String,
        deviceId: String,
        memberId: String,
        withDrawModel: WithDrawVo
    ) = repository.nhBankWithDraw(accessToken, deviceId, memberId, withDrawModel)
}
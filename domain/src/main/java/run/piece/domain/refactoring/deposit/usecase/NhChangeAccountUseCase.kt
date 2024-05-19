package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.model.NhBankRegisterVo
import run.piece.domain.refactoring.deposit.repository.NhBankRepository

class NhChangeAccountUseCase(private val repository: NhBankRepository) {
    fun changeNhAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBank: NhBankRegisterVo
    ) = repository.changeAccount(accessToken = accessToken, deviceId = deviceId, memberId = memberId, registerNhBankModel = registerNhBank)

}
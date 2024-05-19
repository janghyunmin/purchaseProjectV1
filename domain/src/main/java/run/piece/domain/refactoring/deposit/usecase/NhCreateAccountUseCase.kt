package run.piece.domain.refactoring.deposit.usecase

import kotlinx.coroutines.flow.flow
import run.piece.domain.refactoring.deposit.model.NhBankRegisterVo
import run.piece.domain.refactoring.deposit.repository.NhBankRepository

class NhCreateAccountUseCase(private val repository: NhBankRepository) {
    fun postNhCreateAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBank: NhBankRegisterVo
    ) = repository.createAccount(accessToken = accessToken, deviceId = deviceId, memberId = memberId, registerNhBankModel = registerNhBank)
}
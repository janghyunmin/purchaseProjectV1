package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.repository.DepositRepository

class GetMemberAccountUseCase(private val repository: DepositRepository) {
    fun getMemberAccount(accessToken: String, deviceId: String, memberId: String) =
        repository.getMemberAccount(accessToken = accessToken , deviceId = deviceId , memberId = memberId)
}
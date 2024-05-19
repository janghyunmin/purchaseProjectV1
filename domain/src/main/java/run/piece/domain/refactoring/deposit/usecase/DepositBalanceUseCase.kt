package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.repository.DepositRepository

class DepositBalanceUseCase (private val repository: DepositRepository) {
    fun getDepositBalance(accessToken: String, deviceId: String, memberId: String) =
        repository.getDepositBalance(accessToken = accessToken, deviceId = deviceId , memberId = memberId)
}
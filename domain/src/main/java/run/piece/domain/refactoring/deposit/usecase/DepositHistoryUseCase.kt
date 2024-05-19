package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.repository.DepositRepository

class DepositHistoryUseCase(private val repository: DepositRepository) {
    fun getDepositHistory(accessToken: String, deviceId: String, memberId: String, apiVersion: String, searchDvn: String, page: Int) =
        repository.getDepositHistory(accessToken = accessToken, deviceId = deviceId, memberId = memberId, apiVersion = apiVersion, searchDvn = searchDvn, page = page)
}
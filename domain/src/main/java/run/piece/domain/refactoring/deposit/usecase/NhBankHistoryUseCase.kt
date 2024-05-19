package run.piece.domain.refactoring.deposit.usecase

import run.piece.domain.refactoring.deposit.repository.NhBankRepository

class NhBankHistoryUseCase(private val repository: NhBankRepository) {
    suspend operator fun invoke(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) = repository.nhHistory(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId
    )
}
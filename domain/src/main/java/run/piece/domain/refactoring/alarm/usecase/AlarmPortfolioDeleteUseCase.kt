package run.piece.domain.refactoring.alarm.usecase

import run.piece.domain.refactoring.alarm.repository.AlarmRepository

class AlarmPortfolioDeleteUseCase(private val repository: AlarmRepository) {
    operator fun invoke(accessToken: String,
                        deviceId: String,
                        memberId: String,
                        portfolioId: String) = repository.deletePortfolioAlarm(accessToken, deviceId, memberId, portfolioId)
}

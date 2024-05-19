package run.piece.domain.refactoring.alarm.usecase

import run.piece.domain.refactoring.alarm.repository.AlarmRepository

class AlarmPortfolioGetUseCase(private val repository: AlarmRepository) {
    operator fun invoke(accessToken: String,
                        deviceId: String,
                        memberId: String,
                        portfolioId: String) = repository.getPortfolioAlarm(accessToken, deviceId, memberId, portfolioId)
}

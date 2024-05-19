package run.piece.domain.refactoring.alarm.usecase

import run.piece.domain.refactoring.alarm.repository.AlarmRepository

class AlarmPutUseCase(private val repository: AlarmRepository) {
    operator fun invoke(accessToken: String,
                        deviceId: String,
                        memberId: String) = repository.putAlarm(accessToken, deviceId, memberId)
}

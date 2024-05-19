package run.piece.domain.refactoring.alarm.usecase

import run.piece.domain.refactoring.alarm.repository.AlarmRepository

class AlarmListGetUseCase(private val repository: AlarmRepository) {
    operator fun invoke(
        accessToken: String,
        deviceId: String,
        memberId: String,
        type: String
    ) = repository.getAlarm(accessToken, deviceId, memberId, type)
}
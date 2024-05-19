package run.piece.dev.data.refactoring.ui.alarm.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmBaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmDto
import run.piece.dev.data.refactoring.ui.alarm.dto.PortfolioGetAlarmDto
import run.piece.dev.data.utils.WrappedResponse

interface AlarmRemoteDataSource {
    suspend fun getAlarm(
        accessToken: String,
        deviceId: String,
        memberId: String,
        type: String
    ): WrappedResponse<AlarmDto>

    suspend fun putAlarm(accessToken: String, deviceId: String, memberId: String): BaseDto
    suspend fun sendPortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String): AlarmBaseDto
    suspend fun deletePortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String): AlarmBaseDto
    suspend fun getPortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String): WrappedResponse<PortfolioGetAlarmDto>
}
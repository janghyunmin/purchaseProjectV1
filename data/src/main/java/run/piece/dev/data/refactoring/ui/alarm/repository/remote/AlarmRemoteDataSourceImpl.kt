package run.piece.dev.data.refactoring.ui.alarm.repository.remote

import run.piece.dev.data.refactoring.ui.alarm.api.AlarmApi
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmBaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmDto
import run.piece.dev.data.refactoring.ui.alarm.dto.PortfolioGetAlarmDto
import run.piece.dev.data.utils.WrappedResponse

class AlarmRemoteDataSourceImpl(private val api: AlarmApi) : AlarmRemoteDataSource {
    override suspend fun getAlarm(
        accessToken: String,
        deviceId: String,
        memberId: String,
        type: String
    ): WrappedResponse<AlarmDto> = api.getAlarm("Bearer $accessToken", deviceId, memberId, 500, type)
    override suspend fun putAlarm(accessToken: String, deviceId: String, memberId: String) = api.putAlarm("Bearer $accessToken", deviceId, memberId)
    override suspend fun sendPortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String) = api.sendPortfolioAlarm("Bearer $accessToken", deviceId, memberId, portfolioId)
    override suspend fun deletePortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String): AlarmBaseDto = api.deletePortfolioAlarm("Bearer $accessToken", deviceId, memberId, portfolioId)
    override suspend fun getPortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String): WrappedResponse<PortfolioGetAlarmDto> = api.getPortfolioAlarm("Bearer $accessToken", deviceId, memberId, portfolioId)
}
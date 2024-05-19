package run.piece.dev.data.refactoring.ui.alarm.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.alarm.mapper.mapperToAlarmBaseVo
import run.piece.dev.data.refactoring.ui.alarm.mapper.mapperToAlarmItemVo
import run.piece.dev.data.refactoring.ui.alarm.mapper.mapperToPortfolioGetAlarmVo
import run.piece.dev.data.refactoring.ui.alarm.repository.local.AlarmLocalDataSource
import run.piece.dev.data.refactoring.ui.alarm.repository.remote.AlarmRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.alarm.model.AlarmBaseVo
import run.piece.domain.refactoring.alarm.model.AlarmItemVo
import run.piece.domain.refactoring.alarm.model.DeletePortfolioAlarmVo
import run.piece.domain.refactoring.alarm.model.PortfolioGetAlarmVo
import run.piece.domain.refactoring.alarm.repository.AlarmRepository

class AlarmRepositoryImpl(private val alarmRemoteDataSource: AlarmRemoteDataSource,
                          private val alarmLocalDataSource: AlarmLocalDataSource): AlarmRepository {
    override fun getAlarm(
        accessToken: String,
        deviceId: String,
        memberId: String,
        type: String
    ): Flow<List<AlarmItemVo>> = flow {
        emit(alarmRemoteDataSource.getAlarm(accessToken, deviceId, memberId, type).data.alarms.default().mapperToAlarmItemVo())
    }

    override fun putAlarm(accessToken: String,
                          deviceId: String,
                          memberId: String): Flow<BaseVo> = flow {
        emit(alarmRemoteDataSource.putAlarm(accessToken, deviceId, memberId).mapperToAlarmBaseVo())
    }

    override fun sendPortfolioAlarm(accessToken: String,
                          deviceId: String,
                          memberId: String,
                          portfolioId: String): Flow<AlarmBaseVo> = flow {
        emit(alarmRemoteDataSource.sendPortfolioAlarm(accessToken, deviceId, memberId, portfolioId).mapperToAlarmBaseVo())
    }

    override fun deletePortfolioAlarm(accessToken: String,
                                      deviceId: String,
                                      memberId: String,
                                      portfolioId: String): Flow<AlarmBaseVo> = flow {
        emit(alarmRemoteDataSource.deletePortfolioAlarm(accessToken, deviceId, memberId, portfolioId).mapperToAlarmBaseVo())
    }

    override fun getPortfolioAlarm(accessToken: String, deviceId: String, memberId: String, portfolioId: String): Flow<PortfolioGetAlarmVo> = flow {
        emit(alarmRemoteDataSource.getPortfolioAlarm(accessToken, deviceId, memberId, portfolioId).data.mapperToPortfolioGetAlarmVo())
    }
}
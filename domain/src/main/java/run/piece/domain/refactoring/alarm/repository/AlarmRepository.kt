package run.piece.domain.refactoring.alarm.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.alarm.model.AlarmBaseVo
import run.piece.domain.refactoring.alarm.model.AlarmItemVo
import run.piece.domain.refactoring.alarm.model.DeletePortfolioAlarmVo
import run.piece.domain.refactoring.alarm.model.PortfolioGetAlarmVo

interface AlarmRepository {
    fun getAlarm(accessToken: String,
                 deviceId: String,
                 memberId: String,
                 type: String): Flow<List<AlarmItemVo>>

    fun putAlarm(accessToken: String,
                 deviceId: String,
                 memberId: String): Flow<BaseVo>

    fun sendPortfolioAlarm(accessToken: String,
                           deviceId: String,
                           memberId: String,
                           portfolioId: String): Flow<AlarmBaseVo>

    fun deletePortfolioAlarm(accessToken: String,
                             deviceId: String,
                             memberId: String,
                             portfolioId: String): Flow<AlarmBaseVo>
    fun getPortfolioAlarm(accessToken: String,
                          deviceId: String,
                          memberId: String,
                          portfolioId: String): Flow<PortfolioGetAlarmVo>
}
package run.piece.domain.refactoring.event.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.event.model.EventDetailVo
import run.piece.domain.refactoring.event.model.EventItemVo

interface EventRepository {
    suspend fun getEventList(): Flow<PagingData<EventItemVo>>
    suspend fun getEventDetail(eventId: String): Flow<EventDetailVo>
}
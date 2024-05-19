package run.piece.dev.data.refactoring.ui.event.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.event.EventPagingSource
import run.piece.dev.data.refactoring.ui.event.mapper.mapperToEventDetailVo
import run.piece.dev.data.refactoring.ui.event.repository.local.EventLocalDataSource
import run.piece.dev.data.refactoring.ui.event.repository.remote.EventRemoteDataSource
import run.piece.domain.refactoring.event.model.EventDetailVo
import run.piece.domain.refactoring.event.model.EventItemVo
import run.piece.domain.refactoring.event.repository.EventRepository

class EventRepositoryImpl(private val eventRemoteDataSource: EventRemoteDataSource,
                          private val eventLocalDataSource: EventLocalDataSource): EventRepository {

    override suspend fun getEventList(): Flow<PagingData<EventItemVo>> {
        val pagingData = EventPagingSource(remoteDataSource = eventRemoteDataSource)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { pagingData }
        ).flow
    }

    override suspend fun getEventDetail(eventId: String): Flow<EventDetailVo> = flow {
        emit(eventRemoteDataSource.getEventDetail(eventId).data.mapperToEventDetailVo())
    }
}
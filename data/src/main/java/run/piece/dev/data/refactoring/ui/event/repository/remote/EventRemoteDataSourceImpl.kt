package run.piece.dev.data.refactoring.ui.event.repository.remote

import run.piece.dev.data.refactoring.ui.event.api.EventApi
import run.piece.dev.data.refactoring.ui.event.dto.EventDetailDto
import run.piece.dev.data.refactoring.ui.event.dto.EventDto
import run.piece.dev.data.utils.WrappedResponse

class EventRemoteDataSourceImpl(private val api: EventApi): EventRemoteDataSource {
    override suspend fun getEventList(page: Int): WrappedResponse<EventDto> = api.getEventList(page)
    override suspend fun getEventDetail(eventId: String): WrappedResponse<EventDetailDto> = api.getEventDetail(eventId)
}
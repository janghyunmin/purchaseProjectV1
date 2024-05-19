package run.piece.dev.data.refactoring.ui.event.repository.remote

import run.piece.dev.data.refactoring.ui.event.dto.EventDetailDto
import run.piece.dev.data.refactoring.ui.event.dto.EventDto
import run.piece.dev.data.utils.WrappedResponse
interface EventRemoteDataSource {
    suspend fun getEventList(page: Int): WrappedResponse<EventDto>
    suspend fun getEventDetail(eventId: String): WrappedResponse<EventDetailDto>
}
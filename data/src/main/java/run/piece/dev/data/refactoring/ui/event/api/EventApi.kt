package run.piece.dev.data.refactoring.ui.event.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.event.dto.EventDetailDto
import run.piece.dev.data.refactoring.ui.event.dto.EventDto
import run.piece.dev.data.utils.WrappedResponse

interface EventApi {
    @GET("board/event")
    suspend fun getEventList(@Query("page") page: Int): WrappedResponse<EventDto>

    @GET("board/event/{eventId}")
    suspend fun getEventDetail(@Path("eventId") eventId: String): WrappedResponse<EventDetailDto>
}
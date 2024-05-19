package run.piece.dev.data.refactoring.ui.event.dto

import com.google.gson.annotations.SerializedName
data class EventDto(
    @SerializedName("events") var events: List<EventItemDto>?,
    @SerializedName("totalCount") var totalCount: Int?,
    @SerializedName("page") var page: Int?,
    @SerializedName("length") var length: Int?
)

data class EventItemDto(
    @SerializedName("contents") var contents: String?,
    @SerializedName("createdAt") var createdAt: String?,
    @SerializedName("eventBeginDate") var eventBeginDate: String?,
    @SerializedName("eventButtons") var eventButtons: List<EventItemButtonDto>?,
    @SerializedName("eventEndDate") var eventEndDate: String?,
    @SerializedName("eventId") var eventId: String?,
    @SerializedName("isEnd") var isEnd: String?,
    @SerializedName("representThumbnailPath") var representThumbnailPath: String?,
    @SerializedName("shareUrl") var shareUrl: String?,
    @SerializedName("title") var title: String?
)

data class EventItemButtonDto(
    @SerializedName("eventId") var eventId: String?,
    @SerializedName("seq") var seq: Int?,
    @SerializedName("btnTitle") var btnTitle: String?,
    @SerializedName("btnType") var btnType: String?,
    @SerializedName("btnEndpoint") var btnEndPoint: String?,
    @SerializedName("btnEndpointAuth") var btnEndPointAuth: String?,
    @SerializedName("createdAt") var createdAt: String?)
package run.piece.domain.refactoring.event.model
data class EventVo(
    val events: List<EventItemVo>,
    val totalCount: Int,
    val page: Int,
    val length: Int
)

data class EventItemVo(
    val contents: String,
    val createdAt: String,
    val eventBeginDate: String,
    val eventButtons: List<EventItemButtonVo>,
    val eventEndDate: String,
    val eventId: String,
    val isEnd: String,
    val representThumbnailPath: String,
    val shareUrl: String,
    val title: String
)

data class EventItemButtonVo(
    val eventId: String,
    val seq: Int,
    val btnTitle: String,
    val btnType: String,
    val btnEndPoint: String,
    val btnEndPointAuth: String,
    val createdAt: String)
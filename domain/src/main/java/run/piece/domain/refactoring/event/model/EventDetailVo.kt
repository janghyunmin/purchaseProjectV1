package run.piece.domain.refactoring.event.model
data class EventDetailVo(val eventId: String,
                         val title: String,
                         val contents: String,
                         val eventBeginDate: String,
                         val eventEndDate: String,
                         val representThumbnailPath: String,
                         val createdAt: String,
                         val shareUrl: String)
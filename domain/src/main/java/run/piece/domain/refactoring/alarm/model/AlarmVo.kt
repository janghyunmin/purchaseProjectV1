package run.piece.domain.refactoring.alarm.model

data class AlarmVo(
    val alarms: List<AlarmItemVo>?,
    val totalCount: Int?,
    val page: Int?,
    val length: Int?
)

data class AlarmItemVo(
    val viewType: Int,
    val notificationId: String,
    val memberId: String,
    val title: String,
    val message: String,
    val notificationType: String,
    val notificationTypeName: Any,
    val referralTarget: Any,
    val isRead: String,
    val createdAt: String
)
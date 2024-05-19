package run.piece.dev.data.refactoring.ui.alarm.dto

import com.google.gson.annotations.SerializedName

data class AlarmDto(@SerializedName("alarms") var alarms: List<AlarmItemDto>?,
                    @SerializedName("totalCount") var totalCount: Int?,
                    @SerializedName("page") var page: Int?,
                    @SerializedName("length") var length: Int?)

data class AlarmItemDto(var viewType: Int?,
                        var notificationId: String?,
                        var memberId: String?,
                        var title: String?,
                        var message: String?,
                        var notificationType: String?,
                        var notificationTypeName: Any?,
                        var referralTarget: Any?,
                        var isRead: String?,
                        var createdAt: String?)
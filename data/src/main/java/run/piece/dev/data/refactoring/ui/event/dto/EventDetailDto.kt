package run.piece.dev.data.refactoring.ui.event.dto

import com.google.gson.annotations.SerializedName

data class EventDetailDto(@SerializedName("eventId") var eventId: String?,
                          @SerializedName("title") var title: String?,
                          @SerializedName("contents") var contents: String?,
                          @SerializedName("eventBeginDate") var eventBeginDate: String?,
                          @SerializedName("eventEndDate") var eventEndDate: String?,
                          @SerializedName("representThumbnailPath") var representThumbnailPath: String?,
                          @SerializedName("createdAt") var createdAt: String?,
                          @SerializedName("shareUrl") var shareUrl: String?)
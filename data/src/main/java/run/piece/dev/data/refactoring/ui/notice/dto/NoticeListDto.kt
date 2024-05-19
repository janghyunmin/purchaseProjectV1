package run.piece.dev.data.refactoring.ui.notice.dto

import com.google.gson.annotations.SerializedName

data class NoticeListDto(@SerializedName("boards") var boards: List<NoticeItemDto>)

data class NoticeItemDto(@SerializedName("boardId") var boardId: String?,
                         @SerializedName("boardType") var boardType: String?,
                         @SerializedName("boardCategory") var boardCategory: String?,
                         @SerializedName("title") var title: String?,
                         @SerializedName("press") var press: Any?,
                         @SerializedName("contents") var contents: String?,
                         @SerializedName("boadrThumbnailPath") var boardThumbnailPath: String?,
                         @SerializedName("createdAt") var createdAt: String?)
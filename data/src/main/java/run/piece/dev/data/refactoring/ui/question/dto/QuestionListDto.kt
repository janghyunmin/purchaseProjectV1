package run.piece.dev.data.refactoring.ui.question.dto

import com.google.gson.annotations.SerializedName

data class QuestionListDto(@SerializedName("boards") var boards: List<QuestionItemDto>)

data class QuestionItemDto(@SerializedName("boardId") var boardId: String?,
                           @SerializedName("boardType") var boardType: String?,
                           @SerializedName("boardCategory") var boardCategory: String?,
                           @SerializedName("title") var title: String?,
                           @SerializedName("press") var press: Any?,
                           @SerializedName("contents") var contents: String?,
                           @SerializedName("boadrThumbnailPath") var boardThumbnailPath: String?,
                           @SerializedName("createdAt") var createdAt: String?)
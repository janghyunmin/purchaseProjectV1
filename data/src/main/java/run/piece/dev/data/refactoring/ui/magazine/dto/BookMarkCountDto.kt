package run.piece.dev.data.refactoring.ui.magazine.dto

import com.google.gson.annotations.SerializedName

data class BookMarkCountDto (
    @SerializedName("status") val status: String?,
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BookMarkCountItemDto,
)

data class BookMarkCountItemDto (
    @SerializedName("bookmarkCount") var bookmarkCount: Int?
)
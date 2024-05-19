package run.piece.dev.data.refactoring.ui.magazine.dto

import com.google.gson.annotations.SerializedName

data class BookMarkDto (
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("magazineId") var magazineId: String?,
    @SerializedName("magazineType") var magazineType: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("midTitle") var midTitle: String?,
    @SerializedName("smallTitle") var smallTitle: String?,
    @SerializedName("author") var author: String?,
    @SerializedName("representThumbnailPath") var representThumbnailPath: String?,
    @SerializedName("representImagePath") var representImagePath: String?,
    @SerializedName("contents") var contents: String?,
    @SerializedName("isDelete") var isDelete: String?,
    @SerializedName("createdAt") var createdAt: String?,
    @SerializedName("isFavorite") var isFavorite: String
)
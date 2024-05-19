package run.piece.dev.data.refactoring.ui.magazine.dto

import com.google.gson.annotations.SerializedName

data class MagazineDto(
    @SerializedName("magazines") var magazines: List<MagazineItemDto>?,
    @SerializedName("totalCount") var totalCount: Int?,
    @SerializedName("bookmarkCount") var bookmarkCount: Int?,
    @SerializedName("page") var page: Int?,
    @SerializedName("length") var length: Int?
)

data class MagazineItemDto(
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
    @SerializedName("shareUrl") var shareUrl: String?,
    @SerializedName("isFavorite") var isFavorite: String?
)

data class MagazineDetailDto(
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
    @SerializedName("shareUrl") var shareUrl: String?
)
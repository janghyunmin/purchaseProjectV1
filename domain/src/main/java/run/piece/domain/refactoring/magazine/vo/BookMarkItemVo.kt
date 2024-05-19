package run.piece.domain.refactoring.magazine.vo

class BookMarkItemVo(
    val memberId: String,
    val magazineId: String,
    val magazineType: String,
    val title: String,
    val midTitle: String,
    val smallTitle: String,
    val author: String,
    val representThumbnailPath: String,
    val representImagePath: String,
    val contents: String,
    val isDelete: String,
    val createdAt: String,
    val isFavorite: String
)
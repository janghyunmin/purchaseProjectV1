package run.piece.domain.refactoring.magazine.vo

class MagazineVo(
    val magazines: List<MagazineItemVo>?
)
class MagazineItemVo(
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
    val shareUrl: String,
    val isFavorite: String
)

class MagazineDetailVo(
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
    val shareUrl: String
)

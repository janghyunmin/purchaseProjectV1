package run.piece.domain.refactoring.magazine.vo

class BookMarkCountVo (
    val status: String,
    val statusCode: Int,
    val message: String,
    val data: BookMarkCountItemVo
)

class BookMarkCountItemVo (
    val bookmarkCount: Int
)
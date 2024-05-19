package run.piece.domain.refactoring.notice.model
data class NoticeListVo(val boards: List<NoticeItemVo>)

data class NoticeItemVo(val boardId: String,
                        val boardType: String,
                        val boardCategory: String,
                        val title: String,
                        val press: Any,
                        val contents: String,
                        val boardThumbnailPath: String,
                        val createdAt: String,
                        var expandable: Boolean = false)
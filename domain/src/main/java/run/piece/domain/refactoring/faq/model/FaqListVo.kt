package run.piece.domain.refactoring.faq.model
data class FaqListVo(val boards: List<FaqItemVo>)
data class FaqItemVo(val boardId: String,
                     val boardType: String,
                     val boardCategory: String,
                     val title: String,
                     val press: Any,
                     val contents: String,
                     val boardThumbnailPath: String,
                     val createdAt: String,
                     var expandable: Boolean = false
)
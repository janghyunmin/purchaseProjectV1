package run.piece.domain.refactoring.question.model
data class QuestionListVo(val boards: List<QuestionItemVo>)

data class QuestionItemVo(val boardId: String,
                          val boardType: String,
                          val boardCategory: String,
                          val title: String,
                          val press: Any,
                          val contents: String,
                          val boardThumbnailPath: String,
                          val createdAt: String)
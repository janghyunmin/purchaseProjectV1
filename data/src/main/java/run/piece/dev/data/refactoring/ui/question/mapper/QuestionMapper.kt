package run.piece.dev.data.refactoring.ui.question.mapper

import run.piece.dev.data.refactoring.ui.question.dto.QuestionItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.question.model.QuestionItemVo

fun List<QuestionItemDto>?.mapperToQuestionListVo(): List<QuestionItemVo> {
    val list = arrayListOf<QuestionItemVo>()
    this?.forEach {
        list.add(it.mapperToQuestionItemVo())
    }
    return list
}
fun QuestionItemDto?.mapperToQuestionItemVo(): QuestionItemVo = QuestionItemVo(
    boardId = this?.boardId.default(),
    boardType = this?.boardType.default(),
    boardCategory = this?.boardCategory.default(),
    title = this?.title.default(),
    press = this?.press.default(),
    contents = this?.contents.default(),
    boardThumbnailPath = this?.boardThumbnailPath.default(),
    createdAt = this?.createdAt.default()
)
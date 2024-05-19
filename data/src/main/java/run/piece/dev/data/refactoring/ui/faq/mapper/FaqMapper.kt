package run.piece.dev.data.refactoring.ui.faq.mapper

import run.piece.dev.data.refactoring.ui.faq.dto.FaqItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.faq.model.FaqItemVo

fun List<FaqItemDto>?.mapperToFaqListVo(): List<FaqItemVo> {
    val list = arrayListOf<FaqItemVo>()
    this?.forEach {
        list.add(it.mapperToFaqItemVo())
    }
    return list
}
fun FaqItemDto?.mapperToFaqItemVo(): FaqItemVo = FaqItemVo(
    boardId = this?.boardId.default(),
    boardType = this?.boardType.default(),
    boardCategory = this?.boardCategory.default(),
    title = this?.title.default(),
    press = this?.press.default(),
    contents = this?.contents.default(),
    boardThumbnailPath = this?.boardThumbnailPath.default(),
    createdAt = this?.createdAt.default()
)

package run.piece.dev.data.refactoring.ui.notice.mapper

import run.piece.dev.data.refactoring.ui.notice.dto.NoticeItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.notice.model.NoticeItemVo

fun List<NoticeItemDto>?.mapperToNoticeListVo(): List<NoticeItemVo> {
    val list = arrayListOf<NoticeItemVo>()
    this?.forEach {
        list.add(it.mapperToNoticeItemVo())
    }
    return list
}
fun NoticeItemDto?.mapperToNoticeItemVo(): NoticeItemVo
    = NoticeItemVo(
    boardId = this?.boardId.default(),
    boardType = this?.boardType.default(),
    boardCategory = this?.boardCategory.default(),
    title = this?.title.default(),
    press = this?.press.default(),
    contents = this?.contents.default(),
    boardThumbnailPath = this?.boardThumbnailPath.default(),
    createdAt = this?.createdAt.default()
)


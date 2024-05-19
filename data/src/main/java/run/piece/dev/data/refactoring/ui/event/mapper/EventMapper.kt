package run.piece.dev.data.refactoring.ui.event.mapper

import run.piece.dev.data.refactoring.ui.event.dto.EventDetailDto
import run.piece.dev.data.refactoring.ui.event.dto.EventItemButtonDto
import run.piece.dev.data.refactoring.ui.event.dto.EventItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.event.model.EventDetailVo
import run.piece.domain.refactoring.event.model.EventItemButtonVo
import run.piece.domain.refactoring.event.model.EventItemVo

fun List<EventItemDto>?.mapperToEventItemListVo(): List<EventItemVo> {
    val list = arrayListOf<EventItemVo>()
    this?.forEach {
        list.add(it.mapperToEventItemListVo())
    }
    return list
}
fun EventItemDto?.mapperToEventItemListVo(): EventItemVo {
    return EventItemVo(
        contents = this?.contents.default(),
        createdAt = this?.createdAt.default(),
        eventBeginDate = this?.eventBeginDate.default(),
        eventButtons = this?.eventButtons.mapperToEventItemButtonListVo(),
        eventEndDate = this?.eventEndDate.default(),
        eventId = this?.eventId.default(),
        isEnd = this?.isEnd.default(),
        representThumbnailPath = this?.representThumbnailPath.default(),
        shareUrl = this?.shareUrl.default(),
        title = this?.title.default())
}


fun List<EventItemButtonDto>?.mapperToEventItemButtonListVo(): List<EventItemButtonVo> {
    val list = arrayListOf<EventItemButtonVo>()
    this?.forEach {
        list.add(it.mapperToEventItemButtonVo())
    }
    return list
}
fun EventItemButtonDto?.mapperToEventItemButtonVo(): EventItemButtonVo {
    return EventItemButtonVo(
        eventId = this?.eventId.default(),
        seq = this?.seq.default(),
        btnTitle = this?.btnTitle.default(),
        btnType = this?.btnType.default(),
        btnEndPoint = this?.btnEndPoint.default(),
        btnEndPointAuth = this?.btnEndPointAuth.default(),
        createdAt = this?.createdAt.default()
    )
}

fun EventDetailDto?.mapperToEventDetailVo(): EventDetailVo {
    return EventDetailVo(
        eventId = this?.eventId.default(),
        title = this?.title.default(),
        contents = this?.contents.default(),
        eventBeginDate = this?.eventBeginDate.default(),
        eventEndDate = this?.eventEndDate.default(),
        representThumbnailPath = this?.representThumbnailPath.default(),
        createdAt = this?.createdAt.default(),
        shareUrl = this?.shareUrl.default()
    )
}
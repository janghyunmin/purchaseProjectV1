package run.piece.dev.data.refactoring.ui.alarm.mapper

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmBaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.alarm.model.AlarmBaseVo
import run.piece.domain.refactoring.alarm.model.AlarmItemVo

fun List<AlarmItemDto>.mapperToAlarmItemVo(): List<AlarmItemVo> {
    val list = arrayListOf<AlarmItemVo>()
    forEach {
        list.add(
            AlarmItemVo(
                it.viewType.default(),
                it.notificationId.default(),
                it.memberId.default(),
                it.title.default(),
                it.message.default(),
                it.notificationType.default(),
                it.notificationTypeName.default(),
                it.referralTarget.default(),
                it.isRead.default(),
                it.createdAt.default()
            )
        )
    }
    return list
}

fun BaseDto.mapperToAlarmBaseVo(): BaseVo = BaseVo(status, statusCode, message, subMessage, data)

fun AlarmBaseDto.mapperToAlarmBaseVo(): AlarmBaseVo = AlarmBaseVo(status.default(), statusCode.default(), message.default(), data.default())
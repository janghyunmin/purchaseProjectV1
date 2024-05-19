package run.piece.dev.data.refactoring.base

import run.piece.domain.refactoring.BaseVo

fun BaseDto.mapperToBaseVo(): BaseVo = BaseVo(status, statusCode, message, subMessage, data)
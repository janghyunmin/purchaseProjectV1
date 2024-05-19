package run.piece.dev.data.refactoring.ui.alarm.mapper

import run.piece.dev.data.refactoring.ui.alarm.dto.DeletePortfolioAlarmDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.alarm.model.DeletePortfolioAlarmVo

fun DeletePortfolioAlarmDto.mapperToPortfolioGetAlarmVo(): DeletePortfolioAlarmVo = DeletePortfolioAlarmVo(status.default(), statusCode.default(), message.default(), data.default())
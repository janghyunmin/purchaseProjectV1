package run.piece.dev.data.refactoring.ui.alarm.mapper

import run.piece.dev.data.refactoring.ui.alarm.dto.PortfolioGetAlarmDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.alarm.model.PortfolioGetAlarmVo

fun PortfolioGetAlarmDto.mapperToPortfolioGetAlarmVo(): PortfolioGetAlarmVo = PortfolioGetAlarmVo(notificationYn.default())
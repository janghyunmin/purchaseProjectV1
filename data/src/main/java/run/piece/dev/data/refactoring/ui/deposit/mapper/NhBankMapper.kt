package run.piece.dev.data.refactoring.ui.deposit.mapper

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.NhBankHistoryDto
import run.piece.dev.data.refactoring.ui.deposit.dto.RegisterNhBankDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deposit.model.NhBankHistoryVo
import run.piece.domain.refactoring.deposit.model.NhBankRegisterVo


fun BaseDto.mapperToNhBankVo(): BaseVo = BaseVo(status, statusCode, message, subMessage, data)
fun NhBankRegisterVo.mapperToNhAccount(): RegisterNhBankDto =
    RegisterNhBankDto(memberName, bankCode, bankAccount)

fun NhBankHistoryDto.mapperToNhHistoryVo(): NhBankHistoryVo =
    NhBankHistoryVo(
        memberId.default(),
        memberName.default(),
        vractUsg.default(),
        vran.default(),
        invstBrwNm.default(),
        dwmAcnoFx.default(),
        dwmBncd.default(),
        dwmAcno.default(),
        userVranDsnc.default(),
        varnState.default(),
        invstDepsBal.default(),
        rtnAblAmt.default(),
        isncYmd.default()
    )








package run.piece.dev.data.refactoring.ui.common.mapper

import run.piece.dev.data.refactoring.ui.common.dto.AddressDefaultDto
import run.piece.dev.data.refactoring.ui.common.dto.AddressItemDto
import run.piece.dev.data.refactoring.ui.common.dto.CommonDto
import run.piece.dev.data.refactoring.ui.common.dto.CommonFaqDto
import run.piece.dev.data.refactoring.ui.common.dto.JusoDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.common.model.AddressDefaultVo
import run.piece.domain.refactoring.common.model.AddressItemVo
import run.piece.domain.refactoring.common.model.CommonVo
import run.piece.domain.refactoring.common.model.JusoVo
import run.piece.domain.refactoring.common.vo.CommonFaqVo

fun AddressDefaultDto.mapperToAddressVo(): AddressDefaultVo = AddressDefaultVo(
    results = results.mapperToAddressItemVo()
)

fun AddressItemDto.mapperToAddressItemVo(): AddressItemVo = AddressItemVo(
    commonVo = commonDto.mapperToCommonVo(),
    jusoVo = jusoDto.mapperToJusoVo()
)

fun CommonDto?.mapperToCommonVo(): CommonVo = CommonVo(
    errorMessage = this?.errorMessage.default(),
    countPerPage = this?.countPerPage.default(),
    errorCode = this?.errorCode.default(),
    totalCount = this?.totalCount.default(),
    currentPage = this?.currentPage.default()
)

fun List<JusoDto>?.mapperToJusoVo(): List<JusoVo> {
    val list = arrayListOf<JusoVo>()
    this?.forEach {
        list.add(
            JusoVo(
                it.siNm.default(),
                it.lnbrMnnm.default(),
                it.bdKdcd.default(),
                it.jibunAddr.default(),
                it.buldSlno.default(),
                it.bdMgtSn.default(),
                it.zipNo.default(),
                it.admCd.default(),
                it.roadAddr.default(),
                it.liNm.default(),
                it.bdNm.default(),
                it.mtYn.default(),
                it.rnMgtSn.default(),
                it.roadAddrPart2.default(),
                it.sggNm.default(),
                it.buldMnnm.default(),
                it.roadAddrPart1.default(),
                it.emdNm.default(),
                it.lnbrSlno.default(),
                it.engAddr.default(),
                it.udrtYn.default(),
                it.rn.default(),
                it.detBdNmList.default(),
                it.emdNo.default(),
            )
        )
    }
    return list
}



fun List<CommonFaqDto>.mapperToCommonFaqVo(): List<CommonFaqVo> {
    val list = arrayListOf<CommonFaqVo>()
    forEach {
        list.add(
            CommonFaqVo(
                codeId = it.codeId.default(),
                upperCodeId = it.upperCodeId.default(),
                codeName = it.codeName.default(),
                displayOrder = it.displayOrder.default()
            )
        )
    }
    return list
}



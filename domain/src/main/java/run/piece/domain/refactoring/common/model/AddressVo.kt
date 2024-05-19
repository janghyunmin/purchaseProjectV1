package run.piece.domain.refactoring.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class AddressDefaultVo(
    val results: AddressItemVo
) : Parcelable

@Parcelize
class AddressItemVo (
    val commonVo: CommonVo,
    val jusoVo: List<JusoVo>
) : Parcelable

@Parcelize
class CommonVo(
    val errorMessage: String,
    val countPerPage: String,
    val errorCode: String,
    val totalCount: String,
    val currentPage: String
) : Parcelable


@Parcelize
class JusoVo(
    val siNm: String,
    val lnbrMnnm: String,
    val bdKdcd: String,
    val jibunAddr: String,
    val buldSlno: String,
    val bdMgtSn: String,
    val zipNo: String,
    val admCd: String,
    val roadAddr: String,
    val liNm: String,
    val bdNm: String,
    val mtYn: String,
    val rnMgtSn: String,
    val roadAddrPart2: String,
    val sggNm: String,
    val buldMnnm: String,
    val roadAddrPart1: String,
    val emdNm: String,
    val lnbrSlno: String,
    val engAddr: String,
    val udrtYn: String,
    val rn: String,
    val detBdNmList: String,
    val emdNo: String,
) : Parcelable
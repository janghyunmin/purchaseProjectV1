package run.piece.dev.data.refactoring.ui.common.dto

import com.google.gson.annotations.SerializedName

class AddressDefaultDto (
    @SerializedName("results") var results: AddressItemDto,
)

class AddressItemDto (
    @SerializedName("common") var commonDto: CommonDto,
    @SerializedName("juso") var jusoDto: List<JusoDto>?
)

class CommonDto (
    @SerializedName("errorMessage") var errorMessage: String?,
    @SerializedName("countPerPage") var countPerPage: String?,
    @SerializedName("errorCode") var errorCode: String?,
    @SerializedName("totalCount") var totalCount: String?,
    @SerializedName("currentPage") var currentPage: String?,
)

class JusoDto (
    @SerializedName("siNm") var siNm: String?,
    @SerializedName("lnbrMnnm") var lnbrMnnm: String?,
    @SerializedName("bdKdcd") var bdKdcd: String?,
    @SerializedName("jibunAddr") var jibunAddr: String?,
    @SerializedName("buldSlno") var buldSlno: String?,
    @SerializedName("bdMgtSn") var bdMgtSn: String?,
    @SerializedName("zipNo") var zipNo: String?,
    @SerializedName("admCd") var admCd: String?,
    @SerializedName("roadAddr") var roadAddr: String?,
    @SerializedName("liNm") var liNm: String?,
    @SerializedName("bdNm") var bdNm: String?,
    @SerializedName("mtYn") var mtYn: String?,
    @SerializedName("rnMgtSn") var rnMgtSn: String?,
    @SerializedName("roadAddrPart2") var roadAddrPart2: String?,
    @SerializedName("sggNm") var sggNm: String?,
    @SerializedName("buldMnnm") var buldMnnm: String?,
    @SerializedName("roadAddrPart1") var roadAddrPart1: String?,
    @SerializedName("emdNm") var emdNm: String?,
    @SerializedName("lnbrSlno") var lnbrSlno: String?,
    @SerializedName("engAddr") var engAddr: String?,
    @SerializedName("udrtYn") var udrtYn: String?,
    @SerializedName("rn") var rn: String?,
    @SerializedName("detBdNmList") var detBdNmList: String?,
    @SerializedName("emdNo") var emdNo: String?
)
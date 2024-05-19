package run.piece.dev.data.refactoring.ui.deposit.dto

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


data class NhBankHistoryDto(
    @SerializedName("memberId")
    @Expose
    var memberId: String?,

    @SerializedName("memberName")
    @Expose
    var memberName: Any?,

    @SerializedName("vractUsg")
    @Expose
    var vractUsg: Any?,

    @SerializedName("vran")
    @Expose
    var vran: Any?,

    @SerializedName("invstBrwNm")
    @Expose
    var invstBrwNm: Any?,

    @SerializedName("dwmAcnoFx")
    @Expose
    var dwmAcnoFx: Any?,

    @SerializedName("dwmBncd")
    @Expose
    var dwmBncd: Any?,

    @SerializedName("dwmAcno")
    @Expose
    var dwmAcno: Any?,

    @SerializedName("userVranDsnc")
    @Expose
    var userVranDsnc: Any?,

    @SerializedName("varnState")
    @Expose
    var varnState: Any?,

    @SerializedName("invstDepsBal")
    @Expose
    var invstDepsBal: Int?,

    @SerializedName("rtnAblAmt")
    @Expose
    var rtnAblAmt: Int?,

    @SerializedName("isncYmd")
    @Expose
    var isncYmd: Any?

)

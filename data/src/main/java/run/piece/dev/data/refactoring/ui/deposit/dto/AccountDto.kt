package run.piece.dev.data.refactoring.ui.deposit.dto

import com.google.gson.annotations.SerializedName

class AccountDto (
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("bankCode") var bankCode: String?,
    @SerializedName("accountNo") var accountNo: String?,
    @SerializedName("bankName") var bankName: String?
)
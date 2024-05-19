package run.piece.dev.data.refactoring.ui.deposit.dto

import com.google.gson.annotations.SerializedName

class DepositBalanceDto (
    @SerializedName("depositBalance") var depositBalance: Long?,
    @SerializedName("memberId") var memberId: String?,
)
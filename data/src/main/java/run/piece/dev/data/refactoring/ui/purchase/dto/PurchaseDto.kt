package run.piece.dev.data.refactoring.ui.purchase.dto

import com.google.gson.annotations.SerializedName

data class PurchaseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("statusCode") val statusCode: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("subMessage") val subMessage: String?,
    @SerializedName("data") val data: String?
)

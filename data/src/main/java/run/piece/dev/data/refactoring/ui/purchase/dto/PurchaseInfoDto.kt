package run.piece.dev.data.refactoring.ui.purchase.dto

import com.google.gson.annotations.SerializedName

data class PurchaseInfoDto (
    @SerializedName("purchaseAt") val purchaseAt: String,
    @SerializedName("offerPieceVolume") val offerPieceVolume: String,
    @SerializedName("offerTotalAmount") val offerTotalAmount: String,
    @SerializedName("minPurchaseAmount") val minPurchaseAmount: String,
    @SerializedName("purchaseId") val purchaseId: String
)
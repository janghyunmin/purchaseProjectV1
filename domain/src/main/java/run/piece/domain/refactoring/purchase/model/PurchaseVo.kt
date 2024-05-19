package run.piece.domain.refactoring.purchase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PurchaseVo(var response: PurchaseDefaultVo) : Parcelable

@Parcelize
data class PurchaseDefaultVo(
    var status: String?,
    var statusCode: Int?,
    var message: String?,
    var subMessage: String?,
    var data: String?
) : Parcelable

data class PurchaseModel(
    val portfolioId: String?,
    val offerVolume: Int?,
    val electronicDocumentConsentDate: String?
)

data class PurchaseCancelModel(
    val offerId: String?
)
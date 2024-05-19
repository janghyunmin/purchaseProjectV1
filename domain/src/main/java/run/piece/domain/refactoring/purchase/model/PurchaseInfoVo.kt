package run.piece.domain.refactoring.purchase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchaseInfoVo(val purchaseAt: String,
                          val offerPieceVolume: String,
                          val offerTotalAmount: String,
                          val minPurchaseAmount: String,
                          val purchaseId: String) : Parcelable
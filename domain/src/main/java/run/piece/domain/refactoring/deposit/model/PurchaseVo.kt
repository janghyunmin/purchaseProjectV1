package run.piece.domain.refactoring.deposit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchaseVo (
    val purchaseId: String?,
    val memberId: String?,
    val portfolioId: String?,
    val purchaseState: String?,
    val purchaseAt: String?,
    val purchasePieceVolume: Int?,
    val purchasePieceAmount: Int?,
    val purchaseTotalAmount: Int?,
    val isCoupon: String?,
    val isConfirm: String?,
    val portfolio: PortfolioVo?,
    val document: DocumentsVo?,
    val portfolioAttachFile: PortfolioAttachFileVo?
): Parcelable

@Parcelize
data class PortfolioVo(
    val portfolioId: String?,
    val title: String?,
    val representThumbnailImagePath: String?,
    val representImagePath: String?,
    val recruitmentAmount: String?,
    val recruitmentBeginDate: String?,
    val minPurchaseAmount: String?,
    val maxPurchaseAmount: String?,
    val dividendsExpectationDate: String?,
    val products: List<ProductVo?>?
): Parcelable

@Parcelize
data class DocumentsVo(
    val purchaseId: String?,
    val memberId: String?,
    val portfolioId: String?,
    val documentCode: String,
    val documentContents: String
): Parcelable

@Parcelize
data class PortfolioAttachFileVo(
    val title: String?,
    val attachFilePath: String?,
    val codeName: String?
): Parcelable

@Parcelize
data class ProductVo(
    val portfolioId: String?,
    val productId: String?,
    val title: String?,
    val productCondition: String?,
    val productPackageCondition: String?,
    val representThumbnailImagePath: String?,
    val representImagePath: String?,
    val productionYear: String?,
    val author: String?,
    val productMaterial: String?,
    val productDetailInfo: String?,
    val documents: List<DocumentsItemVo>?
): Parcelable

@Parcelize
data class DocumentsItemVo(
    val productId: String?,
    val documentId: String?,
    val documentName: String?,
    val documentImagePath: String?,
    val address: String?,
    val addressUrl: String?,
    val documentIconPath: String?,
): Parcelable


// ApiVersion v0.0.2 Vo
@Parcelize
data class PurchaseVoV2(
    val purchaseId: String?,
    val memberId: String?,
    val portfolioId: String?,
    val purchasePieceVolume: Int?,
    val purchasePieceAmount: Int?,
    val purchaseTotalAmount: Int?,
    val isCoupon: String?,
    val isConfirm: String?,
    val purchaseState: String?,
    val purchaseAt: String?,
    val title: String?,
    val subTitle: String?,
    val attachFilePath: String?,
    val representThumbnailImagePath: String?
) : Parcelable

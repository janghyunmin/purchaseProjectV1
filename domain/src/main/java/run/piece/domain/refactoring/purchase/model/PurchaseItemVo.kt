package run.piece.domain.refactoring.purchase.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchaseItemVo(
    val status: String?,
    val message: String?,
    val data: List<Data>?
) : Parcelable

@Parcelize
data class Data(
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
    val document: DocumentVo?,
    val portfolioAttachFile: PortfolioAttachFileVo?,
) : Parcelable


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
    val dividendsExpecatationDate: String?,
    val products: List<ProductVo>?,
) : Parcelable

@Parcelize
data class DocumentVo (
    val purchaseId: String?,
    val memberId: String?,
    val portfolioId: String?,
    val documentCode: String?,
    val documentContents: String?
) : Parcelable

@Parcelize
data class PortfolioAttachFileVo(
    val title: String?,
    val attachFilePath: String?,
    val codeName: String?
) : Parcelable


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
    val documents: List<DocumentItemVo>?
) : Parcelable

@Parcelize
data class DocumentItemVo(
    val productId: String?,
    val documentId: String?,
    val documentName: String?,
    val documentImagePath: String?
) : Parcelable



package run.piece.dev.data.refactoring.ui.deposit.dto

import com.google.gson.annotations.SerializedName


class PurchaseDto(
    @SerializedName("purchaseId") var purchaseId: String?,
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("purchaseState") var purchaseState: String?,
    @SerializedName("purchaseAt") var purchaseAt: String?,
    @SerializedName("purchasePieceVolume") var purchasePieceVolume: Int?,
    @SerializedName("purchasePieceAmount") var purchasePieceAmount: Int?,
    @SerializedName("purchaseTotalAmount") var purchaseTotalAmount: Int?,
    @SerializedName("isCoupon") var isCoupon: String?,
    @SerializedName("isConfirm") var isConfirm: String?,
    @SerializedName("portfolio") var portfolio: PortfolioDto?,
    @SerializedName("document") var document: DocumentsDto?,
    @SerializedName("portfolioAttachFile") var portfolioAttachFile: PortfolioAttachFileDto?
)

class PortfolioDto(
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("representThumbnailImagePath") var representThumbnailImagePath: String?,
    @SerializedName("representImagePath") var representImagePath: String?,
    @SerializedName("recruitmentAmount") var recruitmentAmount: String?,
    @SerializedName("recruitmentBeginDate") var recruitmentBeginDate: String?,
    @SerializedName("minPurchaseAmount") var minPurchaseAmount: String?,
    @SerializedName("maxPurchaseAmount") var maxPurchaseAmount: String?,
    @SerializedName("dividendsExpecatationDate") var dividendsExpecatationDate: String?,
    @SerializedName("products") var products: List<ProductDto?>?,
)

class DocumentsDto(
    @SerializedName("purchaseId") var purchaseId: String?,
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("portfolioId")  var portfolioId: String?,
    @SerializedName("documentCode") var documentCode: String,
    @SerializedName("documentContents") var documentContents: String,
)

class PortfolioAttachFileDto(
    @SerializedName("title") var title: String?,
    @SerializedName("attachFilePath") var attachFilePath: String?,
    @SerializedName("codeName") var codeName: String?
)


class ProductDto(
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("productId") var productId: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("productCondition") var productCondition: String?,
    @SerializedName("productPackageCondition") var productPackageCondition: String?,
    @SerializedName("representThumbnailImagePath") var representThumbnailImagePath: String?,
    @SerializedName("representImagePath") var representImagePath: String?,
    @SerializedName("productionYear") var productionYear: String?,
    @SerializedName("author") var author: String?,
    @SerializedName("productMaterial") var productMaterial: String?,
    @SerializedName("productDetailInfo") var productDetailInfo: String?,
    @SerializedName("documents") var documents: List<DocumentsItemDto>?
)

class DocumentsItemDto(
    @SerializedName("productId") var productId: String?,
    @SerializedName("documentId") var documentId: String?,
    @SerializedName("documentName") var documentName: String?,
    @SerializedName("documentImagePath") var documentImagePath: String?,
    @SerializedName("address") var address: String?,
    @SerializedName("addressUrl") var addressUrl: String?,
    @SerializedName("documentIconPath") var documentIconPath: String?,
)


// ApiVersion v0.0.2 Dto
class PurchaseDtoV2(
    @SerializedName("purchaseId") var purchaseId: String?,
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("purchasePieceVolume") var purchasePieceVolume: Int?,
    @SerializedName("purchasePieceAmount") var purchasePieceAmount: Int?,
    @SerializedName("purchaseTotalAmount") var purchaseTotalAmount: Int?,
    @SerializedName("isCoupon") var isCoupon: String?,
    @SerializedName("isConfirm") var isConfirm: String?,
    @SerializedName("purchaseState") var purchaseState: String?,
    @SerializedName("purchaseAt") var purchaseAt: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("subTitle") var subTitle: String?,
    @SerializedName("attachFilePath") var attachFilePath: String?,
    @SerializedName("representThumbnailImagePath") var representThumbnailImagePath: String?,
)

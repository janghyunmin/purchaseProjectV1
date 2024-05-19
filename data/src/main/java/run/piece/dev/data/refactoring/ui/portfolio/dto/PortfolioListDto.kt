package run.piece.dev.data.refactoring.ui.portfolio.dto

import com.google.gson.annotations.SerializedName

data class PortfolioListDto(
    @SerializedName("portfolios") var portfolioDtos: List<PortfoliosDto>,
    @SerializedName("achieveInfos") var achieveInfos: List<AchieveInfosDto>,
)

data class PortfoliosDto(
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("subTitle") var subTitle: String?,
    @SerializedName("representThumbnailImagePath") var representThumbnailImagePath: String?,
    @SerializedName("recruitmentState") var recruitmentState: String?,
    @SerializedName("recruitmentBeginDate") var recruitmentBeginDate: String?,
    @SerializedName("dividendsExpecatationDate") var dividendsExpecatationDate: String?,
    @SerializedName("achievementRate") var achievementRate: String?
)

data class AchieveInfosDto(
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("subTitle") var subTitle: String?,
    @SerializedName("achieveProfitRate") var achieveProfitRate: String?,
)

data class ProductDto(
    @SerializedName("productId") var productId: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("representThumbnailImagePath") var representThumbnailImagePath: String?,
    @SerializedName("productionYear") var productionYear: String?,
    @SerializedName("productMaterial") var productMaterial: String?,
    @SerializedName("productSize") var productSize: String?,
    @SerializedName("productDetailInfo") var productDetailInfo: Any?,
    @SerializedName("author") var author: String?,
    @SerializedName("productDocuments") var productDocumentDtos: List<ProductDocumentDto>?
)

data class ProductDocumentDto(
    @SerializedName("documentId") var documentId: String?,
    @SerializedName("documentName") var documentName: String?,
    @SerializedName("documentIconPath") var documentIconPath: String?,
    @SerializedName("documentImagePath") var documentImagePath: String?
)

data class PurchaseGuideDto(
    @SerializedName("guideId") var guideId: String?,
    @SerializedName("guideName") var guideName: String?,
    @SerializedName("description") var description: Any?,
    @SerializedName("guideIconPath") var guideIconPath: String?
)

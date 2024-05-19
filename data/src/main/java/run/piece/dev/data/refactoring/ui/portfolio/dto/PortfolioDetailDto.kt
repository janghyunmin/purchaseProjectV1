package run.piece.dev.data.refactoring.ui.portfolio.dto

import com.google.gson.annotations.SerializedName

data class PortfolioDetailDto(
    @SerializedName("portfolioDetailDefault") var detailDefault: PortfolioDetailDefaultDto,
    @SerializedName("portfolioProductComposition") var productCompositionList: List<ProductCompositionItemDto>,
    @SerializedName("portfolioStory") var story: PortfolioStoryDto,
    @SerializedName("portfolioJoinBiz") var joinBizList: List<PortfolioJoinBizItemDto>,
    @SerializedName("purchaseGuides") var purchaseGuides: List<PurchaseGuideItemDto>,
    @SerializedName("attachFile") var attachFile: List<AttachFileItemDto>,
    @SerializedName("portfolioStock") var portfolioStock: PortfolioStockItemDto,
    @SerializedName("portfolioMarketInfos") var portfolioMarketInfos: List<PortfolioMarketInfoDto>,
//    @SerializedName("boards") var boards: List<PortfolioBoardDto>,
    @SerializedName("offerId") var offerId: String?
)

data class PortfolioDetailDefaultDto(
    var portfolioId: String?,
    var title: String?,
    var subTitle: String?,
    var representThumbnailImagePath: String?,
    var recruitmentState: String?,
    var recruitmentBeginDate: String?,
    var recruitmentEndDate: String?,
    var dividendsExpecatationDate: String?,
    var achievementRate: String?,
    var prizeAt: String?,
    var offerMemberCount: String?,
    var achieveProfitRate: String?,
    var shareUrl: String?,
    var quantityLeft: String?,
    var maxPurchaseAmount: String?,
    var notificationCount: String?
)

data class ProductCompositionItemDto(
    var productId: String?,
    var owner: String?,
    var title: String?,
    var rate: String?
)

data class PortfolioStoryDto(
    var storyId: String?,
    var sectionName: String?,
    var title: String?,
    var subTitle: String?,
    var contents: String?,
    var storyImagePath: String?
)

data class PortfolioJoinBizItemDto(
    var bizName: String?,
    var bizThumbnailPath: String?,
    var bizId: String?,
    var portfolioJoinBizDetails: List<PortfolioJoinBizDetailsItemDto?>,
)

data class PortfolioJoinBizDetailsItemDto(
    var seq: String?,
    var title: String?,
    var description: String?
)

data class PortfolioStockItemDto(
    var faceValue: String?,
    var recruitmentAmount: String?,
    var isStockPublish: String?,
    var totalPiece: Double?,
    var recruitmentBeginDate: String?,
    var recruitmentEndDate: String?,
    var stockOperatePeriod: String?,
    var stockDvn: String?,
    var stockDvnName: String?,
    var recruitmentMethod: String?
)

data class PurchaseGuideItemDto(
    var guideId: String?,
    var guideName: String?,
    var description: String?,
    var guideIconPath: String?
)

data class AttachFileItemDto(
    var attachFileCode: String?,
    var codeName: String?,
    var attachFilePath: String?,
    var displayOrder: Int?
)


data class PortfolioMarketInfoDto(
    var priceInfoId: String?,
    var priceTitle: String?,
    var displayOrder: Int?,
    var marketPrices: List<PortfolioMarketInfoPriceDto>,
)

data class PortfolioMarketInfoPriceDto(
    var priceId: String?,
    var pricePeriod: String?,
    var price: String?,
    var displayOrder: Int?
)

data class PortfolioBoardDto(
    var title: String?,
    var createdAt: String?,
    var codeName: String?,
    var tabDvn: String?,
    var boardId: String?
)

data class PortfolioOfferIdItemDto(
    var offerId: String?
)
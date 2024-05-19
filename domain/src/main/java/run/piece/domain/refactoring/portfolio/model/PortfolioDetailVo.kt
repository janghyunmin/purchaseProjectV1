package run.piece.domain.refactoring.portfolio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PortfolioDetailVo(
    val detailDefault: PortfolioDetailDefaultVo,
    val productCompositionList: List<ProductCompositionItemVo>,
    val story: PortfolioStoryVo,
    val joinBizList: List<PortfolioJoinBizItemVo>,
    val purchaseGuides: List<PurchaseGuideItemVo>,
    val attachFile: List<AttachFileItemVo>,
    val portfolioStock: PortfolioStockItemVo,
    val portfolioMarketInfos: List<PortfolioMarketInfoVo>,
//    val boards: List<PortfolioBoardVo>,
    val offerId: String
): Parcelable

@Parcelize
data class PortfolioDetailDefaultVo(
    val portfolioId: String,
    val title: String,
    val subTitle: String,
    val representThumbnailImagePath: String,
    val recruitmentState: String,
    val recruitmentBeginDate: String,
    val recruitmentEndDate: String,
    val dividendsExpecatationDate: String,
    val achievementRate: String,
    val prizeAt: String,
    val offerMemberCount: String,
    val achieveProfitRate: String,
    val shareUrl: String,
    val quantityLeft: String,
    val maxPurchaseAmount: String,
    val notificationCount: String
): Parcelable

@Parcelize
data class ProductCompositionItemVo(
    val productId: String,
    val owner: String,
    val title: String,
    val rate: String,
    var colorId: Int
): Parcelable

@Parcelize
data class PortfolioStoryVo(
    val storyId: String,
    val sectionName: String,
    val title: String,
    val subTitle: String,
    val contents: String,
    val storyImagePath: String
): Parcelable

@Parcelize
data class PortfolioJoinBizItemVo(
    val bizName: String,
    val bizThumbnailPath: String,
    val bizId: String,
    val portfolioJoinBizDetails: List<PortfolioJoinBizDetailsItemVo?>,
): Parcelable

@Parcelize
data class PortfolioJoinBizDetailsItemVo(
    val seq: String,
    val title: String,
    val description: String
): Parcelable

@Parcelize
data class PortfolioStockItemVo(
    val faceValue: String,
    val recruitmentAmount: String,
    val isStockPublish: String,
    val totalPiece: Double,
    val recruitmentBeginDate: String,
    val recruitmentEndDate: String,
    val stockOperatePeriod: String,
    val stockDvn: String,
    val stockDvnName: String,
    val recruitmentMethod: String

) : Parcelable

@Parcelize
data class PurchaseGuideItemVo(
    val guideId: String,
    val guideName: String,
    val description: String,
    val guideIconPath: String
): Parcelable

@Parcelize
data class AttachFileItemVo(
    val attachFileCode: String,
    val codeName: String,
    val attachFilePath: String,
    val displayOrder: Int
) : Parcelable

@Parcelize
data class PortfolioMarketInfoVo(
    val priceInfoId: String,
    val priceTitle: String,
    val displayOrder: Int,
    val marketPrices: List<PortfolioMarketInfoPriceVo>,
    var isClicked: Boolean
) : Parcelable

@Parcelize
data class PortfolioMarketInfoPriceVo(
    val priceId: String,
    val pricePeriod: String,
    val price: String,
    val displayOrder: Int
) : Parcelable

@Parcelize
data class PortfolioBoardVo(
    val title: String,
    val createdAt: String,
    val codeName: String,
    val tabDvn: String,
    val boardId: String
) : Parcelable
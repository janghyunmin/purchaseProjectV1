package run.piece.domain.refactoring.portfolio.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PortfolioListVo(
    val portfolios: List<PortfoliosVo>?,
    val achieveInfos: List<AchieveInfosVo>?,
): Parcelable

@Parcelize
data class PortfoliosVo(
    val portfolioId: String?,
    val title: String?,
    val subTitle: String?,
    val representThumbnailImagePath: String?,
    val recruitmentState: String?,
    val recruitmentBeginDate: String?,
    val dividendsExpecatationDate: String?,
    val achievementRate: String?
): Parcelable

@Parcelize
data class AchieveInfosVo(
    val portfolioId: String?,
    val subTitle: String?,
    val achieveProfitRate: String?,
): Parcelable

data class ProductVo(
    val productId: String?,
    val title: String?,
    val representThumbnailImagePath: String?,
    val productionYear: String?,
    val productMaterial: String?,
    val productSize: String?,
    val productDetailInfo: Any?,
    val author: String?,
    val productDocuments: List<ProductDocumentVo>?
)

data class ProductDocumentVo(
    val documentId: String?,
    val documentName: String?,
    val documentIconPath: String?,
    val documentImagePath: String?
)

data class PurchaseGuideVo(
    val guideId: String?,
    val guideName: String?,
    val description: Any?,
    val guideIconPath: String?
)
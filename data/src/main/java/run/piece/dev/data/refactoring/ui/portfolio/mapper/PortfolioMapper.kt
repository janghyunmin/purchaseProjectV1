package run.piece.dev.data.refactoring.ui.portfolio.mapper

import run.piece.dev.data.refactoring.ui.portfolio.dto.AchieveInfosDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioListDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfoliosDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.ProductDocumentDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.ProductDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PurchaseGuideDto
import run.piece.domain.refactoring.portfolio.model.AchieveInfosVo
import run.piece.domain.refactoring.portfolio.model.PortfolioListVo
import run.piece.domain.refactoring.portfolio.model.PortfoliosVo
import run.piece.domain.refactoring.portfolio.model.ProductDocumentVo
import run.piece.domain.refactoring.portfolio.model.ProductVo
import run.piece.domain.refactoring.portfolio.model.PurchaseGuideVo

fun PortfolioListDto.mapperToPortfolioVo() : PortfolioListVo {
    return PortfolioListVo(
        portfolios = portfolioDtos.mapperToPortfolioVo(),
        achieveInfos = achieveInfos.mapperToAchieveInfosVo(),
    )
}
fun List<PortfoliosDto>.mapperToPortfolioVo(): List<PortfoliosVo> {
    val list = arrayListOf<PortfoliosVo>()

    forEach {
        list.add(
            PortfoliosVo(
                it.portfolioId,
                it.title,
                it.subTitle,
                it.representThumbnailImagePath,
                it.recruitmentState,
                it.recruitmentBeginDate,
                it.dividendsExpecatationDate,
                it.achievementRate
            )
        )
    }

    return list
}

fun List<AchieveInfosDto>.mapperToAchieveInfosVo(): List<AchieveInfosVo> {
    val list = arrayListOf<AchieveInfosVo>()
    forEach {
        list.add(
            AchieveInfosVo(
                it.portfolioId,
                it.subTitle,
                it.achieveProfitRate
            )
        )
    }
    return list
}

fun List<PurchaseGuideDto>.mapperToPurchaseGuideVo(): List<PurchaseGuideVo> {
    val list = arrayListOf<PurchaseGuideVo>()
    forEach {
        list.add(PurchaseGuideVo(it.guideId, it.guideName, it.description, it.guideIconPath))
    }
    return list
}

fun List<ProductDto>.mapperToProductVo(): List<ProductVo> {
    val list = arrayListOf<ProductVo>()
    forEach {
        list.add(
            ProductVo(
                it.productId,
                it.title,
                it.representThumbnailImagePath,
                it.productionYear,
                it.productMaterial,
                it.productSize,
                it.productDetailInfo,
                it.author,
                it.productDocumentDtos?.mapperToProductDocumentVo()
            )
        )
    }
    return list
}

fun List<ProductDocumentDto>.mapperToProductDocumentVo(): List<ProductDocumentVo> {
    val list = arrayListOf<ProductDocumentVo>()
    forEach {
        list.add(
            ProductDocumentVo(
                it.documentId,
                it.documentName,
                it.documentIconPath,
                it.documentImagePath
            )
        )
    }
    return list
}
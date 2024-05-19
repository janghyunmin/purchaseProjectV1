package run.piece.dev.data.refactoring.ui.portfolio.mapper


import run.piece.dev.data.refactoring.ui.portfolio.dto.AttachFileItemDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioBoardDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioDetailDefaultDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioDetailDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioJoinBizDetailsItemDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioJoinBizItemDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioMarketInfoDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioMarketInfoPriceDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioStockItemDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioStoryDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.ProductCompositionItemDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PurchaseGuideItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioBoardVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import run.piece.domain.refactoring.portfolio.model.PortfolioJoinBizDetailsItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioJoinBizItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioMarketInfoPriceVo
import run.piece.domain.refactoring.portfolio.model.PortfolioMarketInfoVo
import run.piece.domain.refactoring.portfolio.model.PortfolioStockItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioStoryVo
import run.piece.domain.refactoring.portfolio.model.ProductCompositionItemVo
import run.piece.domain.refactoring.portfolio.model.PurchaseGuideItemVo

//PortfolioDetailDto
fun PortfolioDetailDto.mapperToPortfolioDetailVo(): PortfolioDetailVo {
    return PortfolioDetailVo(
        detailDefault.mapperToPortfolioDetailDefaultVo(),
        productCompositionList.mapperToProductCompositionItemListVo(),
        story.mapperToPortfolioStoryVo(),
        joinBizList.mapperToPortfolioJoinBizItemVo(),
        purchaseGuides.mapperToPurchaseGuideItemVo(),
        attachFile.mapperToAttachFileItemVo(),
        portfolioStock.mapperToPortfolioStockItemVo(),
        portfolioMarketInfos.mapperToPortfolioMarketInfoListVo(),
//        boards.mapperToPortfolioBoardListVo(),
        offerId.toString()
    )
}

fun List<PortfolioJoinBizItemDto>.mapperToPortfolioJoinBizItemVo(): List<PortfolioJoinBizItemVo> {
    val list = arrayListOf<PortfolioJoinBizItemVo>()
    forEach {
        it.let {
            list.add(it.mapperToPortfolioJoinBizItemVo())
        }
    }
    return list
}

fun List<ProductCompositionItemDto>.mapperToProductCompositionItemListVo(): List<ProductCompositionItemVo> {
    val list = arrayListOf<ProductCompositionItemVo>()
    forEach {
        it.let {
            list.add(it.mapperToProductCompositionItemVo())
        }
    }
    return list
}
fun List<PortfolioJoinBizDetailsItemDto?>.mapperToPortfolioJoinBizDetailsItemListVo(): List<PortfolioJoinBizDetailsItemVo> {
    val list = arrayListOf<PortfolioJoinBizDetailsItemVo>()

    forEach {
        list.add(it.mapperToPortfolioJoinBizDetailsItemVo())
    }

    return list
}

fun List<PurchaseGuideItemDto>.mapperToPurchaseGuideItemVo(): List<PurchaseGuideItemVo> {
    val list = arrayListOf<PurchaseGuideItemVo>()
    forEach {
        it.let {
            list.add(it.mapperToPurchaseGuideItemVo())
        }
    }
    return list
}

fun List<AttachFileItemDto>.mapperToAttachFileItemVo(): List<AttachFileItemVo> {
    val list = arrayListOf<AttachFileItemVo>()
    forEach {
        it.let {
            list.add(it.mapperToAttachFileItemVo())
        }
    }
    return list
}

fun List<PortfolioMarketInfoPriceDto?>.mapperToPortfolioMarketInfoPriceListVo(): List<PortfolioMarketInfoPriceVo> {
    val list = arrayListOf<PortfolioMarketInfoPriceVo>()

    this?.let {
        it.forEach { dto ->
            list.add(dto.mapperToPortfolioMarketInfoPriceVo())
        }
    }
    return list
}

fun List<PortfolioMarketInfoDto>?.mapperToPortfolioMarketInfoListVo(): List<PortfolioMarketInfoVo> {
    val list = arrayListOf<PortfolioMarketInfoVo>()

    this?.let {
        it.forEach { dto ->
            list.add(dto.mapperToPortfolioMarketInfoVo())
        }
    }
    return list
}

fun List<PortfolioBoardDto>?.mapperToPortfolioBoardListVo(): List<PortfolioBoardVo> {
    val list = arrayListOf<PortfolioBoardVo>()
    this?.let {
        forEach {
            list.add(it.mapperToPortfolioBoardVo())
        }
    }

    return list
}


//PortfolioMarketInfoPriceDto
fun PortfolioJoinBizDetailsItemDto?.mapperToPortfolioJoinBizDetailsItemVo(): PortfolioJoinBizDetailsItemVo = PortfolioJoinBizDetailsItemVo(this?.seq?: "0", this?.title.default(), this?.description.default())
fun PortfolioJoinBizItemDto?.mapperToPortfolioJoinBizItemVo(): PortfolioJoinBizItemVo = PortfolioJoinBizItemVo(this?.bizName.default(), this?.bizThumbnailPath.default(), this?.bizId.default(), this?.portfolioJoinBizDetails?.mapperToPortfolioJoinBizDetailsItemListVo().default())
fun PortfolioStoryDto?.mapperToPortfolioStoryVo(): PortfolioStoryVo = PortfolioStoryVo(this?.storyId.default(), this?.sectionName.default(), this?.title.default(), this?.subTitle.default(), this?.contents.default(), this?.storyImagePath.default())
fun ProductCompositionItemDto?.mapperToProductCompositionItemVo(): ProductCompositionItemVo = ProductCompositionItemVo(this?.productId.default(), this?.owner.default(), this?.title.default(), this?.rate?: "0", 0)
fun PortfolioDetailDefaultDto?.mapperToPortfolioDetailDefaultVo(): PortfolioDetailDefaultVo = PortfolioDetailDefaultVo(this?.portfolioId.default(), this?.title.default(), this?.subTitle.default(), this?.representThumbnailImagePath.default(), this?.recruitmentState.default(),
    this?.recruitmentBeginDate.default(), this?.recruitmentEndDate.default(), this?.dividendsExpecatationDate.default(), this?.achievementRate?: "0", this?.prizeAt.default(), this?.offerMemberCount?: "0", this?.achieveProfitRate?: "0", this?.shareUrl.default() , this?.quantityLeft?: "0", this?.maxPurchaseAmount?: "0", this?.notificationCount?: "0")

fun PurchaseGuideItemDto?.mapperToPurchaseGuideItemVo(): PurchaseGuideItemVo = PurchaseGuideItemVo(this?.guideId.default(), this?.guideName.default(), this?.description.default(), this?.guideIconPath.default())
fun AttachFileItemDto?.mapperToAttachFileItemVo(): AttachFileItemVo = AttachFileItemVo(this?.attachFileCode.default(), this?.codeName.default(), this?.attachFilePath.default(), this?.displayOrder.default())
fun PortfolioStockItemDto?.mapperToPortfolioStockItemVo(): PortfolioStockItemVo = PortfolioStockItemVo(this?.faceValue?: "0", this?.recruitmentAmount?: "0", this?.isStockPublish.default(), this?.totalPiece.default(), this?.recruitmentBeginDate.default(), this?.recruitmentEndDate.default(), this?.stockOperatePeriod.default(), this?.stockDvn.default(), this?.stockDvnName.default(), this?.recruitmentMethod.default())
fun PortfolioMarketInfoPriceDto?.mapperToPortfolioMarketInfoPriceVo(): PortfolioMarketInfoPriceVo = PortfolioMarketInfoPriceVo(this?.priceId.default(), this?.pricePeriod.default(), this?.price?: "0", this?.displayOrder.default())
fun PortfolioMarketInfoDto?.mapperToPortfolioMarketInfoVo(): PortfolioMarketInfoVo = PortfolioMarketInfoVo(this?.priceInfoId.default(), this?.priceTitle.default(), this?.displayOrder.default(), this?.marketPrices?.mapperToPortfolioMarketInfoPriceListVo().default(), false)
fun PortfolioBoardDto?.mapperToPortfolioBoardVo(): PortfolioBoardVo = PortfolioBoardVo(this?.title.default(), this?.createdAt.default(), this?.codeName.default(), this?.tabDvn.default(), this?.boardId.default())

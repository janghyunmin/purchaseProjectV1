package run.piece.dev.data.refactoring.ui.portfolio.mapper

import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioMarketInfoDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioProductDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.ProductAttachFileItemDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.ProductJoinBizDetailDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.ProductJoinBizInfoDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.portfolio.model.PortfolioMarketInfoVo
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo
import run.piece.domain.refactoring.portfolio.model.ProductAttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.ProductJoinBizDetailVo
import run.piece.domain.refactoring.portfolio.model.ProductJoinBizInfoVo

fun List<ProductJoinBizDetailDto>?.mapperToProductJoinBizDetailListVo(): List<ProductJoinBizDetailVo> {
    val list = arrayListOf<ProductJoinBizDetailVo>()

    this?.let {
        forEach {
            list.add(ProductJoinBizDetailVo(it.seq.default(), it.title.default(), it.description.default()))
        }
    }
    return list
}

fun List<ProductAttachFileItemDto>?.mapperToProductAttachFileItemListVo(): List<ProductAttachFileItemVo> {
    val list = arrayListOf<ProductAttachFileItemVo>()

    this?.let {
        forEach {
            list.add(it.mapperToProductAttachFileItemVo())
        }
    }

    return list
}

fun List<PortfolioProductDto>?.mapperToPortfolioProductListVo(): List<PortfolioProductVo> {
    val list = arrayListOf<PortfolioProductVo>()

    this?.let {
        forEach {
            list.add(it.mapperToPortfolioProductVo())
        }
    }
    return list
}

fun ProductJoinBizInfoDto?.mapperToProductJoinBizInfoVo(): ProductJoinBizInfoVo = ProductJoinBizInfoVo(this?.bizId.default(), this?.bizName.default(), this?.bizSubName.default(), this?.bizThumbnailPath.default(), this?.productJoinBizDetails.mapperToProductJoinBizDetailListVo())

fun ProductAttachFileItemDto?.mapperToProductAttachFileItemVo(): ProductAttachFileItemVo = ProductAttachFileItemVo(this?.attachFilePath.default(), this?.attachFileCode.default(), this?.attachFileCodeName.default())

fun PortfolioProductDto?.mapperToPortfolioProductVo(): PortfolioProductVo = PortfolioProductVo(
    this?.productId.default(),
    this?.title.default(),
    this?.representThumbnailImagePath.default(),
    this?.recruitmentAmount.default(),
    this?.representImagePath.default(),
    this?.owner.default(),
    this?.productDate.default(),
    this?.productScale.default(),
    this?.productOther.default(),
    this?.categoryId.default(),
    this?.categoryName.default(),
    this?.storageLocation.default(),
    this?.storageCompany.default(),
    this?.productAttachFiles.mapperToProductAttachFileItemListVo(),
    this?.productJoinBizInfo.mapperToProductJoinBizInfoVo(),
    this?.xcoordinates?: "0",
    this?.ycoordinates?: "0",
    isClicked = false)


package run.piece.dev.data.refactoring.ui.deposit.mapper

import run.piece.dev.data.refactoring.ui.deposit.dto.AccountDto
import run.piece.dev.data.refactoring.ui.deposit.dto.DepositBalanceDto
import run.piece.dev.data.refactoring.ui.deposit.dto.DocumentsDto
import run.piece.dev.data.refactoring.ui.deposit.dto.DocumentsItemDto
import run.piece.dev.data.refactoring.ui.deposit.dto.HistoryDto
import run.piece.dev.data.refactoring.ui.deposit.dto.HistoryItemDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PortfolioAttachFileDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PortfolioDto
import run.piece.dev.data.refactoring.ui.deposit.dto.ProductDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.PurchaseDtoV2
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.deposit.model.AccountVo
import run.piece.domain.refactoring.deposit.model.DepositBalanceVo
import run.piece.domain.refactoring.deposit.model.DocumentsItemVo
import run.piece.domain.refactoring.deposit.model.DocumentsVo
import run.piece.domain.refactoring.deposit.model.HistoryItemVo
import run.piece.domain.refactoring.deposit.model.HistoryVo
import run.piece.domain.refactoring.deposit.model.PortfolioAttachFileVo
import run.piece.domain.refactoring.deposit.model.PortfolioVo
import run.piece.domain.refactoring.deposit.model.ProductVo
import run.piece.domain.refactoring.deposit.model.PurchaseVo
import run.piece.domain.refactoring.deposit.model.PurchaseVoV2


// 회원 계좌 정보 조회
fun AccountDto.mapperToAccountVo(): AccountVo = AccountVo(
    memberId = this.memberId.default(),
    bankCode = this.bankCode.default(),
    accountNo = this.accountNo.default(),
    bankName = this.bankName.default()
)

// 예치금 잔액 조회
fun DepositBalanceDto.mapperToDepositBalanceVo() : DepositBalanceVo = DepositBalanceVo(
    depositBalance = this.depositBalance.default(),
    memberId = this.memberId.default()
)



/**
 * 회원 구매 목록 조회는 ApiVersion v0.0.1 , v0.0.2 로 나누어져있다.
 * 하여, 아키텍쳐 2가지 모두 개발
 * */

// apiVersion : v0.0.1 Dto , Vo
fun HistoryDto.mapperToHistoryVo(): HistoryVo = HistoryVo(
    result = result.mapperToHistoryItemVo(),
    totalCount = this.totalCount.default(),
    page = this.page.default(),
    length = this.length.default()
)

fun List<HistoryItemDto>?.mapperToHistoryItemVo(): List<HistoryItemVo> {
    val list = arrayListOf<HistoryItemVo>()
    this?.forEach {
        list.add(
            HistoryItemVo(
                it.memberId.default(),
                it.seq.default(),
                it.changeAmount.default(),
                it.remainAmount.default(),
                it.changeReason.default(),
                it.changeReasonName.default(),
                it.changeReasonDetail.default(),
                it.createdAt.default()
            )
        )
    }
    return list
}
fun HistoryItemDto.mapperToHistoryItemVo(): HistoryItemVo = HistoryItemVo(
    memberId = this.memberId.default(),
    seq = this.seq.default(),
    changeAmount = this.changeAmount.default(),
    remainAmount = this.remainAmount.default(),
    changeReason = this.changeReason.default(),
    changeReasonName = this.changeReasonName.default(),
    changeReasonDetail = this.changeReasonDetail.default(),
    createdAt = this.createdAt.default()
)





// 회원 거래 내역 조회 apiVersion : v0.0.1 Dto , Vo
fun List<PurchaseDto>.mapperToPurchaseVo() : List<PurchaseVo> {
    val list = arrayListOf<PurchaseVo>()
    this.forEach {
        list.add(
            PurchaseVo(
                it.purchaseId.default(),
                it.memberId.default(),
                it.portfolioId.default(),
                it.purchaseState.default(),
                it.purchaseAt.default(),
                it.purchasePieceVolume.default(),
                it.purchasePieceAmount.default(),
                it.purchaseTotalAmount.default(),
                it.isCoupon.default(),
                it.isConfirm.default(),
                portfolio = it.portfolio?.mapperToPortfolioVo(),
                document = it.document?.mapperToDocumentsVo(),
                portfolioAttachFile = it.portfolioAttachFile?.mapperToPortfolioAttachFileVo()
            )
        )
    }

    return list
}

fun PortfolioDto.mapperToPortfolioVo() : PortfolioVo = PortfolioVo(
    portfolioId = this.portfolioId.default(),
    title = this.title.default(),
    representThumbnailImagePath = this.representThumbnailImagePath.default(),
    representImagePath = this.representImagePath.default(),
    recruitmentAmount = this.recruitmentAmount.default(),
    recruitmentBeginDate = this.recruitmentBeginDate.default(),
    minPurchaseAmount = this.minPurchaseAmount.default(),
    maxPurchaseAmount = this.maxPurchaseAmount.default(),
    dividendsExpectationDate = this.dividendsExpecatationDate.default(),
    products = products.mapperToProductVo()
)

fun DocumentsDto.mapperToDocumentsVo(): DocumentsVo = DocumentsVo(
    purchaseId = this.purchaseId.default(),
    memberId = this.memberId.default(),
    portfolioId = this.portfolioId.default(),
    documentCode = this.documentCode.default(),
    documentContents = this.documentContents.default()
)

fun PortfolioAttachFileDto.mapperToPortfolioAttachFileVo() : PortfolioAttachFileVo = PortfolioAttachFileVo(
    title = this.title.default(),
    attachFilePath = this.attachFilePath.default(),
    codeName = this.codeName.default()
)

fun List<ProductDto?>?.mapperToProductVo() : List<ProductVo?> {
    val list = arrayListOf<ProductVo>()
    this?.forEach {
        list.add(
            ProductVo(
                it?.portfolioId.default(),
                it?.productId.default(),
                it?.title.default(),
                it?.productCondition.default(),
                it?.productPackageCondition.default(),
                it?.representThumbnailImagePath.default(),
                it?.representImagePath.default(),
                it?.productionYear.default(),
                it?.author.default(),
                it?.productMaterial.default(),
                it?.productDetailInfo.default(),
                documents = it?.documents.mapperToDocumentsItemVo()
            )
        )
    }
    return list
}

fun List<DocumentsItemDto>?.mapperToDocumentsItemVo() : List<DocumentsItemVo> {
    val list = arrayListOf<DocumentsItemVo>()
    this?.forEach {
        list.add(
            DocumentsItemVo(
                it.productId.default(),
                it.documentId.default(),
                it.documentName.default(),
                it.documentImagePath.default(),
                it.address.default(),
                it.addressUrl.default(),
                it.documentIconPath.default()
            )
        )
    }
    return list
}







// 회원 거래 내역 조회 apiVersion : v0.0.2 Dto , Vo
fun List<PurchaseDtoV2>?.mapperToPurchaseVoV2(): List<PurchaseVoV2> {
    val list = arrayListOf<PurchaseVoV2>()
    this?.forEach {
        list.add(
            PurchaseVoV2(
                it.purchaseId.default(),
                it.memberId.default(),
                it.portfolioId.default(),
                it.purchasePieceVolume.default(),
                it.purchasePieceAmount.default(),
                it.purchaseTotalAmount.default(),
                it.isCoupon.default(),
                it.isConfirm.default(),
                it.purchaseState.default(),
                it.purchaseAt.default(),
                it.title.default(),
                it.subTitle.default(),
                it.attachFilePath.default(),
                it.representThumbnailImagePath.default()
            )
        )
    }

    return list
}

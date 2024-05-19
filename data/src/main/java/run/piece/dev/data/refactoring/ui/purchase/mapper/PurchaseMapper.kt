package run.piece.dev.data.refactoring.ui.purchase.mapper

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseInfoDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.purchase.model.PurchaseDefaultVo
import run.piece.domain.refactoring.purchase.model.PurchaseInfoVo
import run.piece.domain.refactoring.purchase.model.PurchaseVo


fun BaseDto.mapperToPurchaseVo() : BaseVo = BaseVo(this.status.default(), this.statusCode.default(), this.message.default(), this.subMessage.default(), this.data.default())
fun PurchaseDto?.mapperToPurchaseVo() : PurchaseDefaultVo = PurchaseDefaultVo(this?.status.default(), this?.statusCode.default(), this?.message.default(),this?.subMessage.default(), this?.data.default())

fun PurchaseInfoDto?.mapperToPurchaseInfoVo(): PurchaseInfoVo = PurchaseInfoVo(
    purchaseAt = this?.purchaseAt.default(),
    offerPieceVolume = this?.offerPieceVolume.default(),
    offerTotalAmount = this?.offerTotalAmount.default(),
    minPurchaseAmount = this?.minPurchaseAmount.default(),
    purchaseId = this?.purchaseId.default()
)
package run.piece.domain.refactoring.purchase.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.model.PurchaseDefaultVo
import run.piece.domain.refactoring.purchase.model.PurchaseInfoVo
import run.piece.domain.refactoring.purchase.model.PurchaseModel

interface PurchaseRepository {
    fun getPurchaseInfo(
        accessToken: String,
        deviceId: String,
        memberId: String,
        portfolioId: String
    ) : Flow<PurchaseInfoVo>

    fun purchaseOffer(
        accessToken: String,
        deviceId: String,
        memberId: String,
        purchaseModel: PurchaseModel
    ) : Flow<PurchaseDefaultVo>

    fun purchaseCancel(
        accessToken: String,
        deviceId: String,
        memberId: String,
        model: PurchaseCancelModel
    ) : Flow<BaseVo>
}
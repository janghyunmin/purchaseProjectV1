package run.piece.dev.data.refactoring.ui.purchase.model

import run.piece.domain.refactoring.BaseVo

sealed class PurchaseState {
    object Loading: PurchaseState()
    object Empty : PurchaseState()
    data class Success ( val isSuccess: BaseVo) : PurchaseState()
    data class Failure ( val message: String ) : PurchaseState()
}
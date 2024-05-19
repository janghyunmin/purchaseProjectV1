package run.piece.dev.data.refactoring.ui.deposit.model

import run.piece.domain.refactoring.BaseVo

sealed class NhBankCreateState {
    object Init : NhBankCreateState()
    data class IsLoading(val isLoading: Boolean) : NhBankCreateState()
    data class Success(val isSuccess: BaseVo) : NhBankCreateState()
    data class Failure(val message: String) : NhBankCreateState()
}
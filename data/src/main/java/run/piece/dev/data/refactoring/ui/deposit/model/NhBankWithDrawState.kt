package run.piece.dev.data.refactoring.ui.deposit.model

sealed class NhBankWithDrawState {
    object Init: NhBankWithDrawState()
    data class IsLoading(val isLoading: Boolean) : NhBankWithDrawState()
    data class Success(val isSuccess: Any?) : NhBankWithDrawState()
    data class Failure(val message: String) : NhBankWithDrawState()
}
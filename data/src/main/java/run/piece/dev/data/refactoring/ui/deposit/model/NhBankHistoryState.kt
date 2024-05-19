package run.piece.dev.data.refactoring.ui.deposit.model

sealed class NhBankHistoryState {
    object Init: NhBankHistoryState()
    data class IsLoading(val isLoading: Boolean) : NhBankHistoryState()
    data class Success(val isSuccess: Any?) : NhBankHistoryState()
    data class Failure(val message: String) : NhBankHistoryState()
    data class Empty(val code: String) : NhBankHistoryState()
}
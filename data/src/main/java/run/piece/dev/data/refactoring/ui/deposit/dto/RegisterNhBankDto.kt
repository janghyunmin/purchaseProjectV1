package run.piece.dev.data.refactoring.ui.deposit.dto

data class RegisterNhBankDto (
    val memberName: String,
    val bankCode: String?,
    val bankAccount: String
)
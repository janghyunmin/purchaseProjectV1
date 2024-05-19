package run.piece.domain.refactoring.deposit.model

// 가상계좌 등록 VO
data class NhBankRegisterVo(
    val memberName: String,
    val bankCode: String?,
    val bankAccount: String
)
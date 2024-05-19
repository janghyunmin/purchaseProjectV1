package run.piece.domain.refactoring.member.model

data class AuthPinErrorVo(
    val status: String,
    val statusCode: Int,
    val message: String,
    val data: ErrorData
)

data class ErrorData(
    val memberId: String,
    val pinNumber: String,
    val passwordUpdatedAt: String,
    val passwordIncorrectCount: String,
    val isExistMember: String
)
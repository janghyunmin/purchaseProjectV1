package run.piece.domain.refactoring.db.user

//imutable data class 값은 non null
data class UserVo(
    val name: String,
    val email: String,
    val birthDay: String,
    val cellPhoneNo: String,
    val gender: String,
    val pinNumber: String,
    val joinDay: String,
    val isFido: String
)
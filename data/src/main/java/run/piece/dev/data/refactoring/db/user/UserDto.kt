package run.piece.dev.data.refactoring.db.user

//mutable data class 값은 nullable
data class UserDto(
    var name: String?,
    var email: String?,
    var birthDay: String?,
    var cellPhoneNo: String?,
    var gender: String?,
    var pinNumber: String?,
    var joinDay: String?,
    var isFido: String?
)
package run.piece.domain.refactoring.db.user

import androidx.room.Entity
import androidx.room.PrimaryKey

//imutable data class 값은 nullable
@Entity(tableName = "user_info")
data class UserEntity(
    val name: String?,
    val email: String?,
    val birthDay: String?,
    val cellPhoneNo: String?,
    val gender: String?,
    val pinNumber: String?,
    val joinDay: String?,
    val isFido: String?
) {
    @PrimaryKey var id: Int = 0
}
package run.piece.dev.data.refactoring.db.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import run.piece.domain.refactoring.db.user.UserEntity

@Dao
interface UserDao {
    /* OnConflictStrategy.REPLACE 데이터베이스에 데이터를 추가할 때 중복되는
    PRIMARY KEY가 발생할 경우, 새로운 데이터로 기존 데이터를 대체하도록 지시하는 역할을 합니다.
    중복된 PRIMARY KEY 에러를 방지하면서 데이터를 갱신할 수 있습니다. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: UserEntity)

    @Query("SELECT * FROM `user_info` WHERE id = 0") // User id가 0인 레코드 조회
    fun getUser(): UserEntity
}
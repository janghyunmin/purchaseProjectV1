package run.piece.dev.data.refactoring.db

import androidx.room.Database
import androidx.room.RoomDatabase
import run.piece.dev.data.refactoring.db.user.UserDao
import run.piece.domain.refactoring.db.user.UserEntity

//Room Database
@Database(entities = [UserEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}
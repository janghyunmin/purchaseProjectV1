package run.piece.dev.data.refactoring.ui.more.repository.local

import run.piece.dev.data.refactoring.db.user.UserDao
import run.piece.domain.refactoring.db.user.UserEntity

class MoreLocalDataSourceImpl(private val userDao: UserDao): MoreLocalDataSource {
    override fun getUserEntity(): UserEntity = userDao.getUser()
    override fun setUserEntity(userEntity: UserEntity) = userDao.insert(userEntity)
}
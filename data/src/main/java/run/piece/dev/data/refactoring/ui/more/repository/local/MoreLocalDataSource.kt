package run.piece.dev.data.refactoring.ui.more.repository.local

import run.piece.domain.refactoring.db.user.UserEntity

interface MoreLocalDataSource {
    fun getUserEntity(): UserEntity
    fun setUserEntity(userEntity: UserEntity)
}
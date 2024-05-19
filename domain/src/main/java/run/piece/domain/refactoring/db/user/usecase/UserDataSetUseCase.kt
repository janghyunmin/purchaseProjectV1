package run.piece.domain.refactoring.db.user.usecase

import run.piece.domain.refactoring.more.repository.MoreRepository
import run.piece.domain.refactoring.db.user.UserVo

class UserDataSetUseCase(private val repository: MoreRepository) {
    operator fun invoke(userVo: UserVo) = repository.setUserVo(userVo = userVo)
}
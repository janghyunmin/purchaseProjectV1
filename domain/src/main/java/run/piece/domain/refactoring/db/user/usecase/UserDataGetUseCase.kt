package run.piece.domain.refactoring.db.user.usecase

import run.piece.domain.refactoring.more.repository.MoreRepository

class UserDataGetUseCase(private val repository: MoreRepository) {
    operator fun invoke() = repository.getUserVo()
}
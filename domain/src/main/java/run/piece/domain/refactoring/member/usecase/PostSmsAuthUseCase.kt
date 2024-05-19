package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class PostSmsAuthUseCase(private val repository: MemberRepository) {
    operator fun invoke(model: PostSmsAuthModel) = repository.postSmsAuth(model = model)
}
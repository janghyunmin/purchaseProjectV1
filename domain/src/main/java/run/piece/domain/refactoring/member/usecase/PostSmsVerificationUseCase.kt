package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.request.PostSmsVerificationModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class PostSmsVerificationUseCase(private val repository: MemberRepository) {
    operator fun invoke(model: PostSmsVerificationModel) = repository.postSmsVerification(model = model)
}

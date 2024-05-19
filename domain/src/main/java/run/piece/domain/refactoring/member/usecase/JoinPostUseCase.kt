package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.JoinBodyVo
import run.piece.domain.refactoring.member.repository.MemberRepository

class JoinPostUseCase(private val repository: MemberRepository) {
    operator fun invoke(joinModel: JoinBodyVo) = repository.postJoin(joinModel = joinModel)
}
package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.MemberPinModel
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class PutMemberUseCase(private val repository: MemberRepository) {
    operator fun invoke(headers: HashMap<String, String>, model: MemberModifyModel) = repository.putMember(headers = headers, model = model)
}
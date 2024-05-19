package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.request.MemberDeleteModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class MemberDeleteUseCase(private val repository: MemberRepository) {
    operator fun invoke(accessToken: String,
                        deviceId: String,
                        memberId: String,
                        memberDeleteModel: MemberDeleteModel)
    = repository.deleteMember(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberDeleteModel = memberDeleteModel)
}
package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.MemberPinModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class PutAuthPinUseCase(private val repository: MemberRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId:String, memberPinModel: MemberPinModel) = repository.putAuthPin(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberPinModel = memberPinModel)
}
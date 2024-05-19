package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.repository.MemberRepository

class GetSsnYnUseCase(private val repository: MemberRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId: String) = repository.getSsnCheck(accessToken = accessToken, deviceId = deviceId, memberId = memberId)
}
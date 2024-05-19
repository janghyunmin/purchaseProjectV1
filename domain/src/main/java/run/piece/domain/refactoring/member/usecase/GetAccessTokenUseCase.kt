package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.repository.MemberRepository

class GetAccessTokenUseCase(private val repository: MemberRepository) {
    operator fun invoke(accessToken: String, deviceId: String, grantType: String, memberId: String) = repository.getAccessToken(accessToken = accessToken , deviceId = deviceId, grantType = grantType, memberId = memberId)
}
package run.piece.domain.refactoring.consent.usecase

import run.piece.domain.refactoring.consent.repository.ConsentRepository

class ConsentMemberListGetUseCase(private val repository: ConsentRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId: String) = repository.getConsentMemberList(accessToken = accessToken, deviceId = deviceId, memberId = memberId)
}
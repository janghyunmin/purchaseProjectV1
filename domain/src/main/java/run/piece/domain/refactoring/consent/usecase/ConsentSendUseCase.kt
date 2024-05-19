package run.piece.domain.refactoring.consent.usecase

import run.piece.domain.refactoring.consent.model.request.ConsentSendModel
import run.piece.domain.refactoring.consent.repository.ConsentRepository
class ConsentSendUseCase(private val repository: ConsentRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId: String, model: ConsentSendModel) =
        repository.sendConsent(accessToken = accessToken, deviceId = deviceId, memberId = memberId, model = model)
}
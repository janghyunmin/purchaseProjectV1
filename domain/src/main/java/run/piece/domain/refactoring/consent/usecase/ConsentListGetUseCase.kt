package run.piece.domain.refactoring.consent.usecase

import run.piece.domain.refactoring.consent.repository.ConsentRepository

class ConsentListGetUseCase(private val repository: ConsentRepository) {
    operator fun invoke(consentDvn: String, apiVersion: String) = repository.getConsentList(consentDvn = consentDvn, apiVersion = apiVersion)
}
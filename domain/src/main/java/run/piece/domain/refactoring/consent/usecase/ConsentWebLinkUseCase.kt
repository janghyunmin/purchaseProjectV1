package run.piece.domain.refactoring.consent.usecase

import run.piece.domain.refactoring.consent.repository.ConsentRepository

class ConsentWebLinkUseCase(private val repository: ConsentRepository) {
    operator fun invoke() = repository.getConsentWebLink()
}
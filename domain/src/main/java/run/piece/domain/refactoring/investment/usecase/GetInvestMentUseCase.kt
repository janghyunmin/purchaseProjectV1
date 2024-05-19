package run.piece.domain.refactoring.investment.usecase

import run.piece.domain.refactoring.investment.repository.InvestMentRepository

class GetInvestMentUseCase(private val repository: InvestMentRepository) {
    operator fun invoke() = repository.getInvestMent()
}
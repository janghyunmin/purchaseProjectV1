package run.piece.domain.refactoring.portfolio.usecase

import run.piece.domain.refactoring.portfolio.repository.PortfolioRepository

class PortfolioDetailGetUseCase(private val repository: PortfolioRepository) {
    operator fun invoke(memberId: String, apiVersion: String, id: String) = repository.getPortfolioDetail(memberId,apiVersion, id)
}
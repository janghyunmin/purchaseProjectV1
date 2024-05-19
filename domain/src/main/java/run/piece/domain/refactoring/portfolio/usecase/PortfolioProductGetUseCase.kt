package run.piece.domain.refactoring.portfolio.usecase

import run.piece.domain.refactoring.portfolio.repository.PortfolioRepository

class PortfolioProductGetUseCase(private val repository: PortfolioRepository) {
    operator fun invoke(id: String, apiVersion: String) = repository.getPortfolioProduct(id, apiVersion)
}
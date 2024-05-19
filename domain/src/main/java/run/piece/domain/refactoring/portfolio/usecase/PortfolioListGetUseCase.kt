package run.piece.domain.refactoring.portfolio.usecase

import run.piece.domain.refactoring.portfolio.repository.PortfolioRepository

class PortfolioListGetUseCase(private val repository: PortfolioRepository) {
    fun getPortfolioList(apiVersion: String, length: Int) = repository.getPortfolio(apiVersion, length)
}
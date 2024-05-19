package run.piece.domain.refactoring.board.usecase

import run.piece.domain.refactoring.board.repository.BoardRepository

class BoardListGetUseCase(private val repository: BoardRepository) {
    fun getDisclosureSearchList(portfolioId: String, keyword: String, investmentPage: Int, managementPage: Int) =
        repository.getDisclosureSearchList(portfolioId = portfolioId, keyword = keyword, investmentPage = investmentPage, managementPage = managementPage)
}
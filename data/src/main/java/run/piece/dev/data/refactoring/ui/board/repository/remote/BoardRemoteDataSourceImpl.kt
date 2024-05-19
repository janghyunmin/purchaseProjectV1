package run.piece.dev.data.refactoring.ui.board.repository.remote

import run.piece.dev.data.refactoring.ui.board.api.BoardApi
import run.piece.dev.data.refactoring.ui.board.dto.BoardDto
import run.piece.dev.data.utils.WrappedResponse

class BoardRemoteDataSourceImpl(private val api: BoardApi) : BoardRemoteDataSource {
    override suspend fun getDisclosureSearchList(portfolioId: String, keyword: String, investmentPage: Int, managementPage: Int): WrappedResponse<BoardDto> =
        api.getPortfolioDisclosureSearch(portfolioId = portfolioId, keyword = keyword, investmentPage = investmentPage , managementPage = managementPage)
}
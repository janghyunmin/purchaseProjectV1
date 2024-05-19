package run.piece.dev.data.refactoring.ui.board.repository.remote

import run.piece.dev.data.refactoring.ui.board.dto.BoardDto
import run.piece.dev.data.utils.WrappedResponse

interface BoardRemoteDataSource {
    suspend fun getDisclosureSearchList(portfolioId: String, keyword:String, investmentPage: Int, managementPage:Int) : WrappedResponse<BoardDto>
}
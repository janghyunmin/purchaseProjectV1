package run.piece.domain.refactoring.board.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.board.model.BoardVo

interface BoardRepository {
    fun getDisclosureSearchList(portfolioId: String, keyword: String, investmentPage: Int, managementPage: Int): Flow<BoardVo>
}
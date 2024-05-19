package run.piece.dev.data.refactoring.ui.board.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.board.mapper.mapperToBoardVo
import run.piece.dev.data.refactoring.ui.board.repository.local.BoardLocalDataSource
import run.piece.dev.data.refactoring.ui.board.repository.remote.BoardRemoteDataSource
import run.piece.domain.refactoring.board.model.BoardVo
import run.piece.domain.refactoring.board.repository.BoardRepository

class BoardRepositoryImpl(
    private val localDataSource: BoardLocalDataSource,
    private val remoteDataSource: BoardRemoteDataSource
    ) : BoardRepository {

    override fun getDisclosureSearchList(portfolioId: String, keyword: String, investmentPage: Int, managementPage: Int): Flow<BoardVo> = flow {
        emit(remoteDataSource.getDisclosureSearchList(portfolioId, keyword, investmentPage, managementPage).data.mapperToBoardVo())
    }
}
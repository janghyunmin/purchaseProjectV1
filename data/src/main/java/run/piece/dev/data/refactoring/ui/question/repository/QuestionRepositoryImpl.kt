package run.piece.dev.data.refactoring.ui.question.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.dev.data.refactoring.ui.faq.FaqPagingSource
import run.piece.dev.data.refactoring.ui.question.QuestionPagingSource
import run.piece.dev.data.refactoring.ui.question.repository.local.QuestionLocalDataSource
import run.piece.dev.data.refactoring.ui.question.repository.remote.QuestionRemoteDataSource
import run.piece.domain.refactoring.faq.model.FaqItemVo
import run.piece.domain.refactoring.question.model.QuestionItemVo
import run.piece.domain.refactoring.question.repository.QuestionRepository

class QuestionRepositoryImpl(private val remoteDataSource: QuestionRemoteDataSource, private val localDataSource: QuestionLocalDataSource): QuestionRepository {
    override suspend fun getQuestionList(boardType: String, boardCategory: String, apiVersion: String): Flow<PagingData<QuestionItemVo>> {
        val pagingData = QuestionPagingSource(remoteDataSource = remoteDataSource, boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { pagingData }
        ).flow
    }

}
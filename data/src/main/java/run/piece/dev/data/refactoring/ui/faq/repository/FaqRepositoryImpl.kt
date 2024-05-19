package run.piece.dev.data.refactoring.ui.faq.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.faq.FaqPagingSource
import run.piece.dev.data.refactoring.ui.faq.repository.local.FaqLocalDataSource
import run.piece.dev.data.refactoring.ui.faq.repository.remote.FaqRemoteDataSource
import run.piece.dev.data.refactoring.ui.notice.NoticePagingSource
import run.piece.dev.data.refactoring.ui.notice.mapper.mapperToNoticeItemVo
import run.piece.domain.refactoring.faq.model.FaqItemVo
import run.piece.domain.refactoring.faq.repository.FaqRepository
import run.piece.domain.refactoring.notice.model.NoticeItemVo

class FaqRepositoryImpl(private val remoteDataSource: FaqRemoteDataSource, private val localDataSource: FaqLocalDataSource): FaqRepository {
    override suspend fun getFaqList(boardType: String, boardCategory: String, apiVersion: String): Flow<PagingData<FaqItemVo>> {
        val pagingData = FaqPagingSource(remoteDataSource = remoteDataSource, boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { pagingData }
        ).flow
    }
}
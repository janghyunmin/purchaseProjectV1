package run.piece.dev.data.refactoring.ui.notice.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.notice.NoticePagingSource
import run.piece.dev.data.refactoring.ui.notice.mapper.mapperToNoticeItemVo
import run.piece.dev.data.refactoring.ui.notice.repository.local.NoticeLocalDataSource
import run.piece.dev.data.refactoring.ui.notice.repository.remote.NoticeRemoteDataSource
import run.piece.domain.refactoring.notice.model.NoticeItemVo
import run.piece.domain.refactoring.notice.repository.NoticeRepository

class NoticeRepositoryImpl(private val remoteDataSource: NoticeRemoteDataSource,
                           private val localDataSource: NoticeLocalDataSource): NoticeRepository {

    override suspend fun getNoticeList(boardType: String): Flow<PagingData<NoticeItemVo>> {
        val pagingData = NoticePagingSource(remoteDataSource = remoteDataSource, boardType = boardType)
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { pagingData }
        ).flow
    }

    override suspend fun getNoticeDetail(boardId: String, apiVersion: String): Flow<NoticeItemVo> = flow {
        emit(remoteDataSource.getNoticeDetail(boardId = boardId,apiVersion = apiVersion).data.mapperToNoticeItemVo())
    }
}
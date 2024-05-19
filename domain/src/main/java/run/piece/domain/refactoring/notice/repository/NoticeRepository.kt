package run.piece.domain.refactoring.notice.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.notice.model.NoticeItemVo

interface NoticeRepository {
    suspend fun getNoticeList(boardType: String): Flow<PagingData<NoticeItemVo>>
    suspend fun getNoticeDetail(boardId: String, apiVersion: String): Flow<NoticeItemVo>
}
package run.piece.domain.refactoring.faq.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.faq.model.FaqItemVo
import run.piece.domain.refactoring.notice.model.NoticeItemVo

interface FaqRepository {
    suspend fun getFaqList(boardType: String, boardCategory: String, apiVersion: String): Flow<PagingData<FaqItemVo>>
}
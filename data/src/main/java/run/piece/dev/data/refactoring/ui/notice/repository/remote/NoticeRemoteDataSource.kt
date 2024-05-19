package run.piece.dev.data.refactoring.ui.notice.repository.remote

import run.piece.dev.data.refactoring.ui.notice.dto.NoticeItemDto
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeListDto
import run.piece.dev.data.utils.WrappedResponse

interface NoticeRemoteDataSource {
    suspend fun getNoticeList(page:Int, boardType: String): WrappedResponse<NoticeListDto>
    suspend fun getNoticeDetail(boardId: String, apiVersion: String): WrappedResponse<NoticeItemDto>
}
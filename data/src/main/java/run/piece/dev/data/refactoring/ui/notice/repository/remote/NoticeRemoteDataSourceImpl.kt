package run.piece.dev.data.refactoring.ui.notice.repository.remote

import run.piece.dev.data.refactoring.ui.notice.api.NoticeApi
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeItemDto
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeListDto
import run.piece.dev.data.utils.WrappedResponse

class NoticeRemoteDataSourceImpl(private val api: NoticeApi): NoticeRemoteDataSource {
    override suspend fun getNoticeList(page: Int, boardType: String): WrappedResponse<NoticeListDto> = api.getNoticeList(page = page, boardType = boardType)
    override suspend fun getNoticeDetail(boardId: String, apiVersion: String): WrappedResponse<NoticeItemDto> = api.getNoticeDetail(boardId = boardId, apiVersion = apiVersion)
}
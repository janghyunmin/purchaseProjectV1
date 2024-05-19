package run.piece.domain.refactoring.notice.usecase

import run.piece.domain.refactoring.notice.repository.NoticeRepository

class NoticeDetailGetUseCase(private val repository: NoticeRepository) {
    suspend operator fun invoke(boardId: String, apiVersion: String) = repository.getNoticeDetail(boardId = boardId, apiVersion = apiVersion)
}
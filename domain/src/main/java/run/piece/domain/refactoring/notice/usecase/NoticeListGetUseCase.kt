package run.piece.domain.refactoring.notice.usecase

import run.piece.domain.refactoring.notice.repository.NoticeRepository

class NoticeListGetUseCase(private val repository: NoticeRepository) {
    suspend operator fun invoke(boardType: String) = repository.getNoticeList(boardType = boardType)
}
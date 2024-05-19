package run.piece.domain.refactoring.faq.usecase

import run.piece.domain.refactoring.board.repository.BoardRepository
import run.piece.domain.refactoring.faq.repository.FaqRepository
import run.piece.domain.refactoring.notice.repository.NoticeRepository

class FaqListGetUseCase(private val repository: FaqRepository) {
    suspend operator fun invoke(boardType: String, boardCategory: String, apiVersion: String) = repository.getFaqList(boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion)
}
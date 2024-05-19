package run.piece.dev.data.refactoring.ui.faq.repository.remote

import run.piece.dev.data.refactoring.ui.faq.dto.FaqListDto
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeListDto
import run.piece.dev.data.utils.WrappedResponse

interface FaqRemoteDataSource {
    suspend fun getFaqList(page:Int, boardType: String, boardCategory: String, apiVersion: String): WrappedResponse<FaqListDto>
}
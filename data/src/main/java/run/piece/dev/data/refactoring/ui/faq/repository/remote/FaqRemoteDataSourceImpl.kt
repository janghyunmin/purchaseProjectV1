package run.piece.dev.data.refactoring.ui.faq.repository.remote

import run.piece.dev.data.refactoring.ui.faq.api.FaqApi
import run.piece.dev.data.refactoring.ui.faq.dto.FaqListDto
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeListDto
import run.piece.dev.data.utils.WrappedResponse

class FaqRemoteDataSourceImpl(private val api: FaqApi): FaqRemoteDataSource {
    override suspend fun getFaqList(page: Int, boardType: String, boardCategory: String, apiVersion: String): WrappedResponse<FaqListDto> = api.getFaqList(page = page, boardType = boardType, boardCategory = boardCategory, apiVersion = apiVersion)

}
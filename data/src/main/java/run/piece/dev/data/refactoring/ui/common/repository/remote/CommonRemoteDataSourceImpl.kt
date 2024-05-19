package run.piece.dev.data.refactoring.ui.common.repository.remote

import run.piece.dev.data.refactoring.ui.common.api.CommonApi
import run.piece.dev.data.refactoring.ui.common.dto.AddressDefaultDto
import run.piece.dev.data.refactoring.ui.common.dto.CommonFaqDto
import run.piece.dev.data.utils.WrappedResponse

class CommonRemoteDataSourceImpl(private val api: CommonApi): CommonRemoteDataSource {
    override suspend fun getSearchAddress(keyword: String, countPerPage: Int,currentPage: Int): WrappedResponse<AddressDefaultDto> =
        api.getSearchAddress(keyword = keyword,countPerPage = countPerPage, currentPage = currentPage)

    override suspend fun getFaqTabType(upperCode: String): WrappedResponse<List<CommonFaqDto>> =
        api.getFaqTabType(upperCode = upperCode)
}
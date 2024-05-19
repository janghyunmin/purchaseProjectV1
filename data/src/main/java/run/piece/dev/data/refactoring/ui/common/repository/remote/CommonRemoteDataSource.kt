package run.piece.dev.data.refactoring.ui.common.repository.remote

import run.piece.dev.data.refactoring.ui.common.dto.AddressDefaultDto
import run.piece.dev.data.refactoring.ui.common.dto.CommonFaqDto
import run.piece.dev.data.utils.WrappedResponse

interface CommonRemoteDataSource {
    suspend fun getSearchAddress(
        keyword: String,
        countPerPage: Int,
        currentPage: Int
    ) : WrappedResponse<AddressDefaultDto>

    suspend fun getFaqTabType(
        upperCode: String
    ): WrappedResponse<List<CommonFaqDto>>
}
package run.piece.dev.data.refactoring.ui.common.api

import retrofit2.http.GET
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.common.dto.AddressDefaultDto
import run.piece.dev.data.refactoring.ui.common.dto.CommonFaqDto
import run.piece.dev.data.utils.WrappedResponse

interface CommonApi {
    // 주소 검색
    @GET("common/location")
    suspend fun getSearchAddress(
        @Query("keyword") keyword: String,
        @Query("countPerPage") countPerPage: Int,
        @Query("currentPage") currentPage: Int
    ) : WrappedResponse<AddressDefaultDto>

    @GET("common/code")
    suspend fun getFaqTabType(
        @Query("upperCode") upperCode: String
    ) : WrappedResponse<List<CommonFaqDto>>

}
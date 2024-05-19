package run.piece.dev.data.refactoring.ui.board.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.board.dto.BoardDto
import run.piece.dev.data.utils.WrappedResponse

interface BoardApi {
    @GET("portfolio/board/{portfolioId}")
    suspend fun getPortfolioDisclosureSearch(
        @Path("portfolioId") portfolioId: String,
        @Query("keyword") keyword: String,
        @Query("investmentPage") investmentPage: Int,
        @Query("managementPage") managementPage: Int) : WrappedResponse<BoardDto>
}
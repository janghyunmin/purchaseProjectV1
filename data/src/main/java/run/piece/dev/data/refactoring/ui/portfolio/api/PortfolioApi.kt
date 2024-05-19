package run.piece.dev.data.refactoring.ui.portfolio.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioDetailDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioListDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioProductDto
import run.piece.dev.data.utils.WrappedResponse

interface PortfolioApi {
    @GET("portfolio")
    suspend fun getPortfolio(@Header("apiVersion") apiVersion: String, @Query("length") length: Int): WrappedResponse<PortfolioListDto>

    @GET("portfolio/{portfolioId}")
    suspend fun getPortfolioDetail(
        @Header("memberId") memberId: String?,
        @Header("apiVersion") apiVersion: String?,
        @Path("portfolioId") id: String
    ): WrappedResponse<PortfolioDetailDto>

    @GET("portfolio/product/{portfolioId}")
    suspend fun getPortfolioProduct(@Path("portfolioId") id: String,
                                    @Header("apiVersion") apiVersion: String): WrappedResponse<List<PortfolioProductDto>>
}
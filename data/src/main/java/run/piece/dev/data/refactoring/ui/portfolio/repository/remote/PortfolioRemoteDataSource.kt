package run.piece.dev.data.refactoring.ui.portfolio.repository.remote

import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioDetailDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioListDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioProductDto
import run.piece.dev.data.utils.WrappedResponse

interface PortfolioRemoteDataSource {
    suspend fun getPortfolio(apiVersion: String,length: Int): WrappedResponse<PortfolioListDto>
    suspend fun getPortfolioDetail(memberId: String, apiVersion: String, id: String): WrappedResponse<PortfolioDetailDto>
    suspend fun getPortfolioProduct(id: String, apiVersion: String): WrappedResponse<List<PortfolioProductDto>>
}
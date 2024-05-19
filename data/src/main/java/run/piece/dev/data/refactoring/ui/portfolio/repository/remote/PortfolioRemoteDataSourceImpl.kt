package run.piece.dev.data.refactoring.ui.portfolio.repository.remote

import run.piece.dev.data.refactoring.ui.portfolio.api.PortfolioApi
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioDetailDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioListDto
import run.piece.dev.data.refactoring.ui.portfolio.dto.PortfolioProductDto
import run.piece.dev.data.utils.WrappedResponse

class PortfolioRemoteDataSourceImpl(private val api: PortfolioApi): PortfolioRemoteDataSource {
    override suspend fun getPortfolio(apiVersion: String, length: Int): WrappedResponse<PortfolioListDto> = api.getPortfolio(apiVersion,length)
    override suspend fun getPortfolioDetail(memberId: String ,apiVersion: String, id: String): WrappedResponse<PortfolioDetailDto> = api.getPortfolioDetail(memberId, apiVersion, id)
    override suspend fun getPortfolioProduct(id: String, apiVersion: String): WrappedResponse<List<PortfolioProductDto>> = api.getPortfolioProduct(id, apiVersion)
}
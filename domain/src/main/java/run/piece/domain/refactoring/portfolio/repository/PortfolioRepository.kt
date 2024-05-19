package run.piece.domain.refactoring.portfolio.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.portfolio.model.AchieveInfosVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import run.piece.domain.refactoring.portfolio.model.PortfolioListVo
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo
import run.piece.domain.refactoring.portfolio.model.PortfoliosVo

interface PortfolioRepository {
    fun getPortfolio(apiVersion: String, length: Int): Flow<PortfolioListVo>
    fun getPortfolioDetail(memberId: String, apiVersion: String, id: String): Flow<PortfolioDetailVo>
    fun getPortfolioProduct(id: String, apiVersion: String): Flow<List<PortfolioProductVo>>
}
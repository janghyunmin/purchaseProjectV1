package run.piece.dev.data.refactoring.ui.portfolio.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.portfolio.mapper.mapperToPortfolioDetailVo
import run.piece.dev.data.refactoring.ui.portfolio.mapper.mapperToPortfolioProductListVo
import run.piece.dev.data.refactoring.ui.portfolio.mapper.mapperToPortfolioVo
import run.piece.dev.data.refactoring.ui.portfolio.repository.local.PortfolioLocalDataSource
import run.piece.dev.data.refactoring.ui.portfolio.repository.remote.PortfolioRemoteDataSource
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import run.piece.domain.refactoring.portfolio.model.PortfolioListVo
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo
import run.piece.domain.refactoring.portfolio.repository.PortfolioRepository

class PortfolioRepositoryImpl(private val remoteDataSource: PortfolioRemoteDataSource,
                              private val localDataSource: PortfolioLocalDataSource): PortfolioRepository {

//    override fun getPortfolio(apiVersion: String): Flow<PagingData<PortfoliosVo>> {
//        val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false, prefetchDistance = 7)
//        val pagingData = PortfolioPagingSource(remoteDataSource, apiVersion)
//        return Pager(
//            config = pagingConfig,
//            pagingSourceFactory = {
//                pagingData
//            }
//        ).flow
//    }

    override fun getPortfolio(apiVersion: String, length: Int): Flow<PortfolioListVo> = flow {
        emit(remoteDataSource.getPortfolio(apiVersion = apiVersion, length = length).data.mapperToPortfolioVo())
    }

    override fun getPortfolioDetail(memberId: String, apiVersion: String, id: String): Flow<PortfolioDetailVo> = flow {
        emit(remoteDataSource.getPortfolioDetail(memberId,apiVersion,id).data.mapperToPortfolioDetailVo())
    }

    override fun getPortfolioProduct(id: String, apiVersion: String): Flow<List<PortfolioProductVo>> = flow {
        emit(remoteDataSource.getPortfolioProduct(id, apiVersion).data.mapperToPortfolioProductListVo())
    }
}
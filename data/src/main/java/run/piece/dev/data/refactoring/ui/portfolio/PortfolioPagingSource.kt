//package run.piece.dev.data.refactoring.ui.portfolio
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import run.piece.dev.data.refactoring.ui.portfolio.mapper.mapperToPortfolioVo
//import run.piece.dev.data.refactoring.ui.portfolio.repository.remote.PortfolioRemoteDataSource
//import run.piece.dev.data.utils.default
//import run.piece.domain.refactoring.portfolio.model.PortfoliosVo
//
//class PortfolioPagingSource(private val remoteDataSource: PortfolioRemoteDataSource, private val apiVersion: String): PagingSource<Int, PortfoliosVo>() {
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PortfoliosVo> {
//        return try {
//            val next = params.key ?: 1
//            val data = remoteDataSource.getPortfolio(next, apiVersion).data.portfolioDtos?.mapperToPortfolioVo().default()
//            LoadResult.Page(
//                data = data,
//                prevKey = if (next == 1) null else next - 1,
//                nextKey = if (data.isEmpty() || data.size < 10) null else next.plus(1)
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, PortfoliosVo>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//        }
//    }
//}
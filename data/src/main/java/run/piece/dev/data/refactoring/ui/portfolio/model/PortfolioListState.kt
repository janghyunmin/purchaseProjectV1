package run.piece.dev.data.refactoring.ui.portfolio.model

import androidx.paging.PagingData
import run.piece.domain.refactoring.portfolio.model.AchieveInfosVo
import run.piece.domain.refactoring.portfolio.model.PortfoliosVo

sealed class PortfolioListState {
    object Init : PortfolioListState()
    data class IsLoading(val isLoading: Boolean) : PortfolioListState()
    data class PortfolioListSuccess(val portfolioList: PagingData<PortfoliosVo>): PortfolioListState()
    data class AchieveListSuccess(val achieveInfoList: List<AchieveInfosVo>): PortfolioListState()

    data class Failure(val message: String) : PortfolioListState()
}

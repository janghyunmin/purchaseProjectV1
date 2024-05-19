package run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide

import run.piece.domain.refactoring.portfolio.model.PortfolioBoardVo

data class GuideItem(
    val image: Int,
    val title: String,
    val content: String,
    val boards: PortfolioBoardVo? = null,
    var expandable: Boolean = false)
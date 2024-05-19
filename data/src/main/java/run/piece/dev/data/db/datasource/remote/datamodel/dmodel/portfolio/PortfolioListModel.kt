package run.piece.dev.data.db.datasource.remote.datamodel.dmodel.portfolio


sealed class PortfolioItem : DisplayableItem{
    data class PortfolioListModel(
        val portfolioId: String,
        val title: String,
        val subTitle: String,
        val representThumbnailImagePath: String,
        val recruitmentState: String,
        val recruitmentBeginDate: String,
        val dividendsExpecatationDate: String,
        val achievementRate: String,
        override val viewType: Int
    ): PortfolioItem()

    data class AchieveListModel(
        val portfolioId: String,
        val subTitle: String,
        val achieveProfitRate: String,
        override val viewType: Int
    ): PortfolioItem()
}



package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import run.piece.domain.refactoring.board.model.FilesVo


data class DisclosureDataItem(
    val title: String,
    val contents: String,
    val createdAt: String,
    val codeName: String,
    val tabDvn: String,
    val boardId: String,
    val files: List<FilesVo?>,
    val type: Int
)

data class PageDataItem(
    val title: String,
    val contents: String,
    val createdAt: String,
    val codeName: String,
    val tabDvn: String,
    val boardId: String,
    val files: List<FilesVo?>,
    val type: Int
)
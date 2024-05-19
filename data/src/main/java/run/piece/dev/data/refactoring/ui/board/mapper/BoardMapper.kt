package run.piece.dev.data.refactoring.ui.board.mapper

import run.piece.dev.data.refactoring.ui.board.dto.BoardDto
import run.piece.dev.data.refactoring.ui.board.dto.FilesDto
import run.piece.dev.data.refactoring.ui.board.dto.InvestmentDisclosureDto
import run.piece.dev.data.refactoring.ui.board.dto.InvestmentDisclosureItemDto
import run.piece.dev.data.refactoring.ui.board.dto.ManagementDisclosureDto
import run.piece.dev.data.refactoring.ui.board.dto.ManagementDisclosureItemDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.board.model.BoardVo
import run.piece.domain.refactoring.board.model.FilesVo
import run.piece.domain.refactoring.board.model.InvestmentDisclosureItemVo
import run.piece.domain.refactoring.board.model.InvestmentDisclosureVo
import run.piece.domain.refactoring.board.model.ManagementDisclosureItemVo
import run.piece.domain.refactoring.board.model.ManagementDisclosureVo

fun BoardDto.mapperToBoardVo(): BoardVo = BoardVo(
    investmentDisclosure = investmentDisclosure.mapperToInvestmentDisclosureVo(),
    managementDisclosure = managementDisclosure.mapperToManagementDisclosureVo()
)

fun InvestmentDisclosureDto.mapperToInvestmentDisclosureVo(): InvestmentDisclosureVo =
    InvestmentDisclosureVo(
        disclosure = disclosure.mapperToInvestmentDisclosureItemVo(),
        page = this.page.default(),
        length = this.length.default(),
        totalCount = this.totalCount.default()
)

fun ManagementDisclosureDto.mapperToManagementDisclosureVo(): ManagementDisclosureVo =
    ManagementDisclosureVo(
        disclosure = disclosure.mapperToManagementDisclosureItemVo(),
        page = this.page.default(),
        length = this.length.default(),
        totalCount = this.totalCount.default()
    )

fun List<InvestmentDisclosureItemDto>?.mapperToInvestmentDisclosureItemVo(): List<InvestmentDisclosureItemVo> {
    val list = arrayListOf<InvestmentDisclosureItemVo>()
    this?.let {
        forEach {
            list.add(it.mapperToInvestmentDisclosureItemVo())
        }
    }
    return list
}
fun List<ManagementDisclosureItemDto>?.mapperToManagementDisclosureItemVo(): List<ManagementDisclosureItemVo> {
    val list = arrayListOf<ManagementDisclosureItemVo>()
    this?.let {
        forEach {
            list.add(it.mapperToManagementDisclosureItemVo())
        }
    }
    return list
}


fun InvestmentDisclosureItemDto?.mapperToInvestmentDisclosureItemVo() : InvestmentDisclosureItemVo =
    InvestmentDisclosureItemVo(
        title = this?.title.default(),
        contents = this?.contents.default(),
        createdAt = this?.createdAt.default(),
        codeName = this?.codeName.default(),
        tabDvn = this?.tabDvn.default(),
        boardId = this?.boardId.default(),
        files = this?.files.mapperToFilesVo()
    )

fun ManagementDisclosureItemDto?.mapperToManagementDisclosureItemVo() : ManagementDisclosureItemVo =
    ManagementDisclosureItemVo(
        title = this?.title.default(),
        contents = this?.contents.default(),
        createdAt = this?.createdAt.default(),
        codeName = this?.codeName.default(),
        tabDvn = this?.tabDvn.default(),
        boardId = this?.boardId.default(),
        files = this?.files.mapperToFilesVo()
    )

fun List<FilesDto>?.mapperToFilesVo() : List<FilesVo> {
    val list = arrayListOf<FilesVo>()
    this?.let {
        forEach {
            list.add(it.mapperToFilesVo())
        }
    }
    return list
}

fun FilesDto?.mapperToFilesVo(): FilesVo = FilesVo(
    fileId = this?.fileId.default(),
    originFileName = this?.originFileName.default(),
    cdnFilePath = this?.cdnFilePath.default()
)

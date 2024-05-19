package run.piece.domain.refactoring.board.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class BoardVo(
    val investmentDisclosure: InvestmentDisclosureVo,
    val managementDisclosure: ManagementDisclosureVo
) : Parcelable

@Parcelize
class InvestmentDisclosureVo(
    val disclosure: List<InvestmentDisclosureItemVo>,
    val page: Int,
    val length: Int,
    val totalCount: Int
) : Parcelable

@Parcelize
class ManagementDisclosureVo(
    val disclosure: List<ManagementDisclosureItemVo>,
    val page: Int,
    val length: Int,
    val totalCount: Int
) : Parcelable

@Parcelize
class InvestmentDisclosureItemVo(
    val title: String,
    val contents: String,
    val createdAt: String,
    val codeName: String,
    val tabDvn: String,
    val boardId: String,
    val files: List<FilesVo>
) : Parcelable

@Parcelize
class ManagementDisclosureItemVo(
    val title: String,
    val contents: String,
    val createdAt: String,
    val codeName: String,
    val tabDvn: String,
    val boardId: String,
    val files: List<FilesVo>
) : Parcelable

@Parcelize
class FilesVo(
    val fileId: String,
    val originFileName: String,
    val cdnFilePath: String
) : Parcelable


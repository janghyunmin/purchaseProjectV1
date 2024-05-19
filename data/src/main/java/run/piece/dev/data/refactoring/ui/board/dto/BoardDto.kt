package run.piece.dev.data.refactoring.ui.board.dto

import com.google.gson.annotations.SerializedName

class BoardDto(
    @SerializedName("investmentDisclosure") var investmentDisclosure: InvestmentDisclosureDto,
    @SerializedName("managementDisclosure") var managementDisclosure: ManagementDisclosureDto
)

class InvestmentDisclosureDto(
    @SerializedName("disclosure") var disclosure: List<InvestmentDisclosureItemDto>?,
    @SerializedName("page") var page: Int?,
    @SerializedName("length") var length: Int?,
    @SerializedName("totalCount") var totalCount: Int?
)

class ManagementDisclosureDto(
    @SerializedName("disclosure") var disclosure: List<ManagementDisclosureItemDto>?,
    @SerializedName("page") var page: Int?,
    @SerializedName("length") var length: Int?,
    @SerializedName("totalCount") var totalCount: Int?
)

class InvestmentDisclosureItemDto(
    @SerializedName("title") var title: String?,
    @SerializedName("contents") var contents: String?,
    @SerializedName("createdAt") var createdAt: String?,
    @SerializedName("codeName") var codeName: String?,
    @SerializedName("tabDvn") var tabDvn: String?,
    @SerializedName("boardId") var boardId: String?,
    @SerializedName("files") var files: List<FilesDto>?
)

class ManagementDisclosureItemDto(
    @SerializedName("title") var title: String?,
    @SerializedName("contents") var contents: String?,
    @SerializedName("createdAt") var createdAt: String?,
    @SerializedName("codeName") var codeName: String?,
    @SerializedName("tabDvn") var tabDvn: String?,
    @SerializedName("boardId") var boardId: String?,
    @SerializedName("files") var files: List<FilesDto>?
)

class FilesDto(
    @SerializedName("fileId") var fileId: String?,
    @SerializedName("originFileName") var originFileName: String?,
    @SerializedName("cdnFilePath") var cdnFilePath: String?,
)

package run.piece.dev.data.refactoring.base

data class ErrorResponseDto(
    var inspectionBeginDate: String?,
    var inspectionEndDate: String?,
    var inspectionTitle: String?,
    var inspectionContent: String?,
    var inspectionDate: String?
)
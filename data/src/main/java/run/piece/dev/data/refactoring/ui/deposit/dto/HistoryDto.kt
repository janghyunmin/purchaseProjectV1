package run.piece.dev.data.refactoring.ui.deposit.dto

import com.google.gson.annotations.SerializedName

class HistoryDto(
    @SerializedName("result") var result: List<HistoryItemDto>?,
    @SerializedName("totalCount") var totalCount: Int?,
    @SerializedName("page") var page: Int?,
    @SerializedName("length") var length: Int?
)

class HistoryItemDto (
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("seq") var seq: Int?,
    @SerializedName("changeAmount") var changeAmount: Int?,
    @SerializedName("remainAmount") var remainAmount: Int?,
    @SerializedName("changeReason") var changeReason: String?,
    @SerializedName("changeReasonName") var changeReasonName: String?,
    @SerializedName("changeReasonDetail") var changeReasonDetail: String?,
    @SerializedName("createdAt") var createdAt: String?
)
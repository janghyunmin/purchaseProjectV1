package run.piece.domain.refactoring.deposit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class HistoryVo(
    val result: List<HistoryItemVo>,
    val totalCount: Int,
    val page: Int,
    val length: Int
): Parcelable

@Parcelize
class HistoryItemVo(
    val memberId: String,
    val seq: Int,
    val changeAmount: Int,
    val remainAmount: Int,
    val changeReason: String,
    val changeReasonName: String,
    val changeReasonDetail: String,
    val createdAt: String
): Parcelable
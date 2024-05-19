package run.piece.domain.refactoring.deposit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountVo (
    val memberId: String,
    val bankCode: String,
    val accountNo: String,
    val bankName: String
) : Parcelable
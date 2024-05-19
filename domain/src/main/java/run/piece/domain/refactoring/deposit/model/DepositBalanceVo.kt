package run.piece.domain.refactoring.deposit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DepositBalanceVo (
    val depositBalance: Long,
    val memberId: String
) : Parcelable
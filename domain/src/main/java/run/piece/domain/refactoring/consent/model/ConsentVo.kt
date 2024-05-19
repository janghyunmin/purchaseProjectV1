package run.piece.domain.refactoring.consent.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConsentVo(
    val consentCode: String,
    val consentGroup: String,
    val consentTitle: String,
    var isMandatory: String,
    var isChecked: Boolean
) : Parcelable
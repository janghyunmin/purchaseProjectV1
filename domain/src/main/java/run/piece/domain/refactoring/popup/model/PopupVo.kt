package run.piece.domain.refactoring.popup.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PopupVo(
    val popupId: String,
    val popupTitle: String,
    val popupType: String,
    val popupTypeName: String,
    val popupImagePath: String,
    val popupLinkType: String,
    val popupLinkTypeName: String,
    val popupLinkUrl: String
) : Parcelable
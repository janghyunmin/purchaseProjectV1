package run.piece.dev.data.refactoring.ui.popup.dto

import com.google.gson.annotations.SerializedName

class PopupDto (
    @SerializedName("popupId") var popupId: String,
    @SerializedName("popupTitle") var popupTitle: String,
    @SerializedName("popupType") var popupType: String,
    @SerializedName("popupTypeName") var popupTypeName: String,
    @SerializedName("popupImagePath") var popupImagePath: String,
    @SerializedName("popupLinkType") var popupLinkType: String,
    @SerializedName("popupLinkTypeName") var popupLinkTypeName: String,
    @SerializedName("popupLinkUrl") var popupLinkUrl: String,
)
package run.piece.dev.data.refactoring.ui.common.dto

import com.google.gson.annotations.SerializedName

data class CommonFaqDto(
    @SerializedName("codeId") var codeId: String,
    @SerializedName("upperCodeId") var upperCodeId: String,
    @SerializedName("codeName") var codeName: String,
    @SerializedName("displayOrder") var displayOrder: String,
)
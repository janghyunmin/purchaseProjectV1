package run.piece.dev.data.refactoring.ui.consent.dto

import com.google.gson.annotations.SerializedName

data class ConsentDto(@SerializedName("consentCode") var consentCode: String?,
                      @SerializedName("consentGroup") var consentGroup: String?,
                      @SerializedName("consentTitle") var consentTitle: String?,
                      @SerializedName("isMandatory") var isMandatory: String?,
                      @SerializedName("displayOrder") var displayOrder: Int?,
                      @SerializedName("createdAt") var createdAt: String?,
                      @SerializedName("consentContents") var consentContents: String?,
                      var isChecked: Boolean)
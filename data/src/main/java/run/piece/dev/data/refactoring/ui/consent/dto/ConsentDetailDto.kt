package run.piece.dev.data.refactoring.ui.consent.dto

import com.google.gson.annotations.SerializedName

data class ConsentDetailDto(@SerializedName("consentCode") var consentCode: String?,
                            @SerializedName("consentGroup") var consentGroup: String?,
                            @SerializedName("consentTitle") var consentTitle: String?,
                            @SerializedName("consentContents") var consentContents: String?,
                            @SerializedName("isMandatory") var isMandatory: String?,
                            @SerializedName("displayOrder") var displayOrder: String?,
                            @SerializedName("createdAt") var createdAt: String?
)
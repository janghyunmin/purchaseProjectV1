package run.piece.dev.data.refactoring.ui.consent.dto

import com.google.gson.annotations.SerializedName

data class ConsentMemberDto(@SerializedName("required") var required: ConsentMemberItemDto,
                            @SerializedName("selective") var selective: ConsentMemberItemDto,
                            @SerializedName("policy") var policy: ConsentMemberItemDto)

data class ConsentMemberItemDto(@SerializedName("date") var date: String,
                                @SerializedName("consent") var consent: List<ConsentMemberListItemDto>)

data class ConsentMemberListItemDto(@SerializedName("date") var date: String,
                                    @SerializedName("consentCode") var consentCode: String,
                                    @SerializedName("isAgreement") var isAgreement: String,
                                    @SerializedName("consentTitle") var consentTitle: String)
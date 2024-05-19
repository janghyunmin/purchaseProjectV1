package run.piece.dev.data.refactoring.ui.member.model.dto

import com.google.gson.annotations.SerializedName

data class MemberDeleteDto(@SerializedName("status") var status: String?,
                           @SerializedName("statusCode") var statusCode: Int?,
                           @SerializedName("message") var message: String?,
                           @SerializedName("subMessage") var subMessage: String?,
                           @SerializedName("data") var data: Any?)
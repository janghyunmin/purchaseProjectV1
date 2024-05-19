package run.piece.dev.data.refactoring.ui.consent.dto

import com.google.gson.annotations.SerializedName
import retrofit2.Response

data class ConsentSendDto(@SerializedName("status") val status: String?,
                          @SerializedName("statusCode") val statusCode: Int?,
                          @SerializedName("message") val message: String?)
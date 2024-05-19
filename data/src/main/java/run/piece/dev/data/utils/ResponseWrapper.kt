package run.piece.dev.data.utils

import com.google.gson.annotations.SerializedName
import retrofit2.Response

data class WrappedResponse<T> (@SerializedName("status") val status: String?,
                               @SerializedName("statusCode") val statusCode: Int?,
                               @SerializedName("message") val message: String?,
                               @SerializedName("data") val data : T)
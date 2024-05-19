package run.piece.dev.data.refactoring.ui.consent.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDetailDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentMemberDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentSendDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel

interface ConsentApi {
    @GET("common/consent") //일반 약관 API
    suspend fun getConsentList(@Query("consentDvn") consentDvn: String, @Header("apiVersion") apiVersion: String) : WrappedResponse<List<ConsentDto>>

    @GET("member/consent") //더보기 약관 API
    suspend fun getConsentMemberList(@Header("accessToken") accessToken: String,
                                     @Header("deviceId") deviceId: String,
                                     @Header("memberId") memberId: String) : WrappedResponse<ConsentMemberDto>

    @PUT("member/consent") // 선택 동의 전송
    suspend fun sendConsent(@Header("accessToken") accessToken: String,
                            @Header("deviceId") deviceId: String,
                            @Header("memberId") memberId: String,
                            @Body model: ConsentSendModel) : ConsentSendDto

    @GET("board/consent/{consentCode}")
    fun getConsentDetail(@Path("consentCode") consentCode: String) : WrappedResponse<ConsentDetailDto>
}
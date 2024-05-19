package run.piece.dev.data.refactoring.ui.deed.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deed.model.MemberDocumentModel

interface DeedApi {
    @POST("member/document")
    suspend fun sendEmail(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body document: MemberDocumentModel?
    ) : BaseDto
}
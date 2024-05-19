package run.piece.dev.data.refactoring.ui.consent.repository.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDetailDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentMemberDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentSendDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel

interface ConsentRemoteDataSource {
    suspend fun getConsentList(consentDvn: String, apiVersion: String): WrappedResponse<List<ConsentDto>>
    suspend fun getConsentMemberList(accessToken: String, deviceId: String, memberId: String): WrappedResponse<ConsentMemberDto>
    suspend fun getConsentDetail(consentCode: String): WrappedResponse<ConsentDetailDto>

    suspend fun sendConsent(accessToken: String, deviceId: String, memberId: String, model: ConsentSendModel): ConsentSendDto
}
package run.piece.dev.data.refactoring.ui.consent.repository.remote

import run.piece.dev.data.refactoring.ui.consent.api.ConsentApi
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDetailDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentMemberDto
import run.piece.dev.data.refactoring.ui.consent.dto.ConsentSendDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel

class ConsentRemoteDataSourceImpl(private val api: ConsentApi): ConsentRemoteDataSource {
    override suspend fun getConsentList(consentDvn: String, apiVersion: String): WrappedResponse<List<ConsentDto>> =
        api.getConsentList(consentDvn = consentDvn, apiVersion = apiVersion)

    override suspend fun getConsentMemberList(accessToken: String, deviceId: String, memberId: String): WrappedResponse<ConsentMemberDto> =
        api.getConsentMemberList(accessToken = accessToken, deviceId = deviceId, memberId = memberId)

    override suspend fun getConsentDetail(consentCode: String): WrappedResponse<ConsentDetailDto> =
        api.getConsentDetail(consentCode = consentCode)

    override suspend fun sendConsent(accessToken: String, deviceId: String, memberId: String, model: ConsentSendModel): ConsentSendDto =
        api.sendConsent(accessToken = accessToken, deviceId = deviceId, memberId = memberId, model = model)
}
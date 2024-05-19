package run.piece.domain.refactoring.consent.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.consent.model.ConsentDetailVo
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.model.ConsentSendVo
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel

interface ConsentRepository {
    fun getConsentList(consentDvn: String, apiVersion: String): Flow<List<ConsentVo>>
    fun getConsentMemberList(accessToken: String, deviceId: String, memberId: String): Flow<TermsMemberVo>
    fun getConsentDetail(consentCode: String): Flow<ConsentDetailVo>
    fun getConsentWebLink(): String
    fun sendConsent(accessToken: String, deviceId: String, memberId: String, model: ConsentSendModel): Flow<ConsentSendVo>
}
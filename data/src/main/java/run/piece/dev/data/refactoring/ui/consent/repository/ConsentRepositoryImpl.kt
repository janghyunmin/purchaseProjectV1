package run.piece.dev.data.refactoring.ui.consent.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.BuildConfig
import run.piece.dev.data.refactoring.ui.consent.mapper.mapperToConsentDetailVo
import run.piece.dev.data.refactoring.ui.consent.mapper.mapperToConsentListVo
import run.piece.dev.data.refactoring.ui.consent.mapper.mapperToConsentSendVo
import run.piece.dev.data.refactoring.ui.consent.mapper.mapperToMemberTermsConsentVo
import run.piece.dev.data.refactoring.ui.consent.repository.local.ConsentLocalDataSource
import run.piece.dev.data.refactoring.ui.consent.repository.remote.ConsentRemoteDataSource
import run.piece.domain.refactoring.consent.model.ConsentDetailVo
import run.piece.domain.refactoring.consent.model.ConsentSendVo
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel
import run.piece.domain.refactoring.consent.repository.ConsentRepository

class ConsentRepositoryImpl(
    private val remoteDataSource: ConsentRemoteDataSource,
    private val localDataSource: ConsentLocalDataSource): ConsentRepository {

    override fun getConsentList(consentDvn: String, apiVersion:String): Flow<List<ConsentVo>> = flow {
        emit(remoteDataSource.getConsentList(consentDvn = consentDvn, apiVersion = apiVersion).data.mapperToConsentListVo())
    }

    override fun getConsentMemberList(accessToken: String, deviceId: String, memberId: String): Flow<TermsMemberVo> = flow {
        emit(remoteDataSource.getConsentMemberList(accessToken = accessToken, deviceId = deviceId, memberId = memberId).data.mapperToMemberTermsConsentVo())
    }

    override fun getConsentDetail(consentCode: String): Flow<ConsentDetailVo> = flow {
        emit(remoteDataSource.getConsentDetail(consentCode = consentCode).data.mapperToConsentDetailVo())
    }

    override fun getConsentWebLink(): String = BuildConfig.PIECE_WEB_PAGE_LINK
    override fun sendConsent(accessToken: String, deviceId: String, memberId: String, model: ConsentSendModel): Flow<ConsentSendVo> = flow {
        emit(remoteDataSource.sendConsent(accessToken = accessToken, deviceId = deviceId, memberId = memberId, model = model).mapperToConsentSendVo())
    }
}
package run.piece.dev.refactoring.ui.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel
import run.piece.domain.refactoring.consent.usecase.ConsentMemberListGetUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentSendUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentWebLinkUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import javax.inject.Inject

@HiltViewModel
class NewConsentViewModel @Inject constructor(
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val consentWebLinkUseCase: ConsentWebLinkUseCase,
    private val consentMemberListGetUseCase: ConsentMemberListGetUseCase,
    private val consentSendUseCase: ConsentSendUseCase
) : ViewModel() {
    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")

    private val _memberInfo: MutableStateFlow<MembeInfoState> = MutableStateFlow(MembeInfoState.Init)
    val memberInfo: StateFlow<MembeInfoState> = _memberInfo.asStateFlow()

    private val _consentList: MutableStateFlow<ConsentState> = MutableStateFlow(ConsentState.Init)
    val consentList: StateFlow<ConsentState> = _consentList.asStateFlow()

    private val _consentMemberTerms: MutableStateFlow<ConsentMemberTermsState> = MutableStateFlow(ConsentMemberTermsState.Init)
    val consentMemberTerms: StateFlow<ConsentMemberTermsState> = _consentMemberTerms.asStateFlow()

    private val _sendConsent: MutableStateFlow<ConsentSendState> = MutableStateFlow(ConsentSendState.Init)
    val sendConsent: StateFlow<ConsentSendState> = _sendConsent.asStateFlow()

    var selectiveItemDate = ""
    var isShowSnackBar = true

    fun getMemberData() {
        viewModelScope.launch {
            memberInfoGetUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _memberInfo.value = MembeInfoState.Loading(true)
            }.catch { exception ->
                _memberInfo.value = MembeInfoState.Loading(false)
                _memberInfo.value = MembeInfoState.Failure(exception.message.toString())
            }.collect {
                _memberInfo.value = MembeInfoState.Loading(false)
                _memberInfo.value = MembeInfoState.Success(it)
            }
        }
    }

    fun getConsentMemberTermsList() {
        viewModelScope.launch {
            consentMemberListGetUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _consentMemberTerms.value = ConsentMemberTermsState.Loading(true)
            }.catch { exception ->
                _consentMemberTerms.value = ConsentMemberTermsState.Loading(false)
                _consentMemberTerms.value = ConsentMemberTermsState.Failure(exception.message.toString())
            }.collect {
                _consentMemberTerms.value = ConsentMemberTermsState.Loading(false)
                _consentMemberTerms.value = ConsentMemberTermsState.Success(it)

                selectiveItemDate = if (it.selective.consent.isNotEmpty()) it.selective.consent[0].date else ""
            }
        }
    }

    fun sendConsent(consentCode: String, isAgreement: String) {
        val model = ConsentSendModel(memberId = "", consentCode = consentCode, isAgreement = isAgreement)
        viewModelScope.launch {
            consentSendUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                model = model
            ).onStart {
                _sendConsent.value = ConsentSendState.Loading(true)
            }.catch { exception ->
                _sendConsent.value = ConsentSendState.Loading(true)
                _sendConsent.value = ConsentSendState.Failure(exception.message.toString())
            }.collect {
                _sendConsent.value = ConsentSendState.Loading(false)
                _sendConsent.value = ConsentSendState.Success(true)
            }
        }
    }

    fun getConsentWebLink(): String = "${consentWebLinkUseCase()}/terms?tab="

    sealed class MembeInfoState {
        object Init : MembeInfoState()
        data class Loading(val isLoading: Boolean) : MembeInfoState()
        data class Success(val memberVo: MemberVo) : MembeInfoState()
        data class Failure(val message: String) : MembeInfoState()
    }

    sealed class ConsentState {
        object Init : ConsentState()
        data class Loading(val isLoading: Boolean) : ConsentState()
        data class Success(val consentList: List<ConsentVo>) : ConsentState()
        data class Failure(val message: String) : ConsentState()
    }

    sealed class ConsentMemberTermsState {
        object Init : ConsentMemberTermsState()
        data class Loading(val isLoading: Boolean) : ConsentMemberTermsState()
        data class Success(val termsMemberVo: TermsMemberVo) : ConsentMemberTermsState()
        data class Failure(val message: String) : ConsentMemberTermsState()
    }

    sealed class ConsentSendState {
        object Init : ConsentSendState()
        data class Loading(val isLoading: Boolean) : ConsentSendState()
        data class Success(val isSuccess: Boolean) : ConsentSendState()
        data class Failure(val message: String) : ConsentSendState()
    }
}
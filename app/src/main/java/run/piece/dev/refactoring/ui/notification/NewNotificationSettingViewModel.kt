package run.piece.dev.refactoring.ui.notification

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
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.model.request.ConsentSendModel
import run.piece.domain.refactoring.consent.usecase.ConsentMemberListGetUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentSendUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.request.NewMemberNotificationModel
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.MemberPutNotificationUseCase
import javax.inject.Inject

@HiltViewModel
class NewNotificationSettingViewModel @Inject constructor(
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val consentSendUseCase: ConsentSendUseCase,
    private val consentMemberListGetUseCase: ConsentMemberListGetUseCase,
    private val memberPutNotificationUseCase: MemberPutNotificationUseCase
) : ViewModel() {
    var isNotice: String = ""
    var isAd: String = ""
    var adUsageConsent = ""

    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()

    private val _memberNotification: MutableStateFlow<MemberNotificationState> = MutableStateFlow(MemberNotificationState.Init)
    val memberNotification: StateFlow<MemberNotificationState> = _memberNotification.asStateFlow()

    private val _consentMemberTerms: MutableStateFlow<ConsentMemberTermsState> = MutableStateFlow(ConsentMemberTermsState.Init)
    val consentMemberTerms: StateFlow<ConsentMemberTermsState> = _consentMemberTerms.asStateFlow()

    private val _sendConsent: MutableStateFlow<ConsentSendState> = MutableStateFlow(ConsentSendState.Init)
    val sendConsent: StateFlow<ConsentSendState> = _sendConsent.asStateFlow()

    // 회원 정보 조회
    fun getMemberData() {
        viewModelScope.launch {
            memberInfoGetUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _memberInfo.value = MemberInfoState.Loading(true)
            }.catch { exception ->
                _memberInfo.value = MemberInfoState.Loading(false)
                _memberInfo.value = MemberInfoState.Failure(exception.message.toString())
            }.collect {
                _memberInfo.value = MemberInfoState.Loading(false)
                _memberInfo.value = MemberInfoState.Success(it)
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

    fun putMemberNotification(isNotice: String, isAd: String) {
        viewModelScope.launch {
            val model = NewMemberNotificationModel(isNotice, isAd)
            memberPutNotificationUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                model = model
            ).onStart {
                _memberNotification.value = MemberNotificationState.Loading(true)
            }.catch { exception ->
                _memberNotification.value = MemberNotificationState.Loading(false)
                _memberNotification.value = MemberNotificationState.Failure(exception.message.toString())
            }.collect {
                _memberNotification.value = MemberNotificationState.Loading(false)
                _memberNotification.value = MemberNotificationState.Succes(true)
            }
        }
    }

    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }

    sealed class MemberNotificationState {
        object Init : MemberNotificationState()
        data class Loading(val isLoading: Boolean) : MemberNotificationState()
        data class Succes(val isSuccess: Boolean) : MemberNotificationState()
        data class Failure(val message: String) : MemberNotificationState()
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
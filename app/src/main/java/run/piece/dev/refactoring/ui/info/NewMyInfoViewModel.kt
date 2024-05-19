package run.piece.dev.refactoring.ui.info

import androidx.lifecycle.SavedStateHandle
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
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.usecase.ConsentMemberListGetUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.model.request.NotificationModel
import run.piece.domain.refactoring.member.model.request.UpdateConsentItemModel
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.PutMemberUseCase
import javax.inject.Inject

@HiltViewModel
class NewMyInfoViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val savedStateHandle: SavedStateHandle,
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val consentMemberListGetUseCase: ConsentMemberListGetUseCase,
    private val putMemberUseCase: PutMemberUseCase
) : ViewModel() {
    val isLogin: String = PrefsHelper.read("isLogin", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val name = PrefsHelper.read("name", "")
    val pinNumber = PrefsHelper.read("pinNumber", "")
    val cellPhoneNo = PrefsHelper.read("cellPhoneNo", "")
    val cellPhoneIdNo = PrefsHelper.read("cellPhoneIdNo", "")
    val birthDay = PrefsHelper.read("birthDay", "")
    val zipCode = PrefsHelper.read("zipCode", "")
    val baseAddress: String = PrefsHelper.read("baseAddress", "")
    val detailAddress: String = PrefsHelper.read("detailAddress", "")
    val ci = PrefsHelper.read("ci", "")
    val di = PrefsHelper.read("di", "")
    val gender = PrefsHelper.read("gender", "")
    val isFido = PrefsHelper.read("isFido", "")

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()
    private val _putMember: MutableStateFlow<PutMemberState> = MutableStateFlow(PutMemberState.Init)
    val putMember: StateFlow<PutMemberState> = _putMember.asStateFlow()
    private val _consentMemberTerms: MutableStateFlow<ConsentMemberTermsState> = MutableStateFlow(ConsentMemberTermsState.Init)
    val consentMemberTerms: StateFlow<ConsentMemberTermsState> = _consentMemberTerms.asStateFlow()
    val sendConsentList = ArrayList<UpdateConsentItemModel>()

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

    fun putMember(email: String) {
        viewModelScope.launch {
            val notification = NotificationModel(
                memberId,
                "Y",
                "Y",
                "Y",
                "Y",
                PrefsHelper.read("isAd", "N"),
                PrefsHelper.read("isNotice", "N")
            )

            val model = MemberModifyModel(
                memberId = memberId,
                name = name,
                pinNumber = pinNumber,
                cellPhoneIdNo = cellPhoneIdNo,
                birthDay = birthDay,
                cellPhoneNo = cellPhoneNo,
                zipCode = zipCode,
                baseAddress = baseAddress,
                detailAddress = detailAddress,
                ci = ci,
                di = di,
                gender = gender,
                email = email,
                isFido = isFido,
                notification = notification,
                consents = sendConsentList
            )

            val map = HashMap<String, String>()
            map["accessToken"] = "Bearer $accessToken"
            map["deviceId"] = deviceId
            map["memberId"] = memberId

            putMemberUseCase(
                headers = map,
                model = model
            ).onStart {
                _putMember.value = PutMemberState.Loading(true)
            }.catch { exception ->
                _putMember.value = PutMemberState.Loading(false)
                _putMember.value = PutMemberState.Failure(exception.message.toString())
            }.collect {
                _putMember.value = PutMemberState.Loading(false)
                _putMember.value = PutMemberState.Success(it)
            }
        }
    }

    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }

    sealed class PutMemberState {
        object Init : PutMemberState()
        data class Loading(val isLoading: Boolean) : PutMemberState()
        data class Success(val memberVo: MemberVo) : PutMemberState()
        data class Failure(val message: String) : PutMemberState()
    }

    sealed class ConsentMemberTermsState {
        object Init : ConsentMemberTermsState()
        data class Loading(val isLoading: Boolean) : ConsentMemberTermsState()
        data class Success(val termsMemberVo: TermsMemberVo) : ConsentMemberTermsState()
        data class Failure(val message: String) : ConsentMemberTermsState()
    }
}
package run.piece.dev.refactoring.ui.address

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
import run.piece.domain.refactoring.common.model.AddressDefaultVo
import run.piece.domain.refactoring.common.usecase.SearchAddressUseCase
import run.piece.domain.refactoring.consent.model.TermsMemberVo
import run.piece.domain.refactoring.consent.usecase.ConsentMemberListGetUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.model.request.UpdateConsentItemModel
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.PutMemberUseCase
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider,
    private val searchAddressUseCase: SearchAddressUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val consentMemberListGetUseCase: ConsentMemberListGetUseCase,
    private val putMemberUseCase: PutMemberUseCase

) : ViewModel() {
    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")

    private val _addressList: MutableStateFlow<SearchAddressState> = MutableStateFlow(SearchAddressState.Init)
    val addressList: StateFlow<SearchAddressState> get() = _addressList.asStateFlow()

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()
    private val _putMember: MutableStateFlow<PutMemberState> = MutableStateFlow(PutMemberState.Init)
    val putMember: StateFlow<PutMemberState> = _putMember.asStateFlow()
    private val _consentMemberTerms: MutableStateFlow<ConsentMemberTermsState> = MutableStateFlow(ConsentMemberTermsState.Init)
    val consentMemberTerms: StateFlow<ConsentMemberTermsState> = _consentMemberTerms.asStateFlow()
    val sendConsentList = ArrayList<UpdateConsentItemModel>()

    fun getSearchAddress(keyword: String, countPerPage: Int, currentPage: Int) {
        viewModelScope.launch {
            searchAddressUseCase.getSearchAddress(
                keyword = keyword,
                countPerPage = countPerPage,
                currentPage = currentPage // 페이지
            ).onStart {
                _addressList.value = SearchAddressState.IsLoading(true)
            }.catch { exception ->
                _addressList.value = SearchAddressState.IsLoading(false)
                _addressList.value = SearchAddressState.Failure(exception.message.toString())
            }.collect {
                _addressList.value = SearchAddressState.IsLoading(false)
                _addressList.value = SearchAddressState.Success(it)
            }
        }
    }

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

    fun putMember(memberModel: MemberModifyModel) {
        viewModelScope.launch {
            val map = HashMap<String,String>()
            map["accessToken"] = "Bearer $accessToken"
            map["deviceId"] = deviceId
            map["memberId"] = memberId

            putMemberUseCase(
                headers = map,
                model = memberModel
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

    sealed class SearchAddressState {
        object Init: SearchAddressState()
        data class IsLoading(val isLoading: Boolean) : SearchAddressState()
        data class Success(val data: AddressDefaultVo) : SearchAddressState()
        data class Failure(val message: String) : SearchAddressState()
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
package run.piece.dev.refactoring.ui.main

import android.annotation.SuppressLint
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
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.popup.model.PopupVo
import run.piece.domain.refactoring.popup.usecase.PopupUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val savedStateHandle: SavedStateHandle,
    private val memberDeviceCheckUseCase: MemberDeviceCheckUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val popupUseCase: PopupUseCase
) : ViewModel() {

    private val appVersion: String = PrefsHelper.read("appVersion", "")

    val deepLink: String = savedStateHandle.get<String>("deepLink") ?: ""
    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val isLogin: String = PrefsHelper.read("isLogin", "")

    private val _deviceChk: MutableStateFlow<MemberDeviceState> = MutableStateFlow(MemberDeviceState.Init)
    val deviceChk: StateFlow<MemberDeviceState> = _deviceChk.asStateFlow()

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()

    private val _popupInfo: MutableStateFlow<PopupUIState> = MutableStateFlow(PopupUIState.Init)
    val popupInfo: StateFlow<PopupUIState> = _popupInfo.asStateFlow()

    @SuppressLint("SimpleDateFormat")
    val uiDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

    fun memberDeviceChk() {
        viewModelScope.launch {
            memberDeviceCheckUseCase(
                memberId = memberId,
                deviceId = deviceId,
                memberAppVersion = appVersion
            ).onStart {
                _deviceChk.value = MemberDeviceState.Loading(true)
            }.catch { exception ->
                _deviceChk.value = MemberDeviceState.Loading(false)
                _deviceChk.value = MemberDeviceState.Failure(exception.message.toString())
            }.collect {
                _deviceChk.value = MemberDeviceState.Loading(false)
                _deviceChk.value = MemberDeviceState.Success(it.default())
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

    fun getPopup() {
        viewModelScope.launch {
            popupUseCase.openPopUp("POP0102")
                .onStart { _popupInfo.value = PopupUIState.Loading(isLoading = true) }
                .catch { exception ->
                    _popupInfo.value = PopupUIState.Loading(false)
                    _popupInfo.value =  PopupUIState.Failure(exception.message.toString())
                }
                .collect {
                    _popupInfo.value = PopupUIState.Loading(false)
                    _popupInfo.value = PopupUIState.Success(it)
                }

        }
    }


    sealed class MemberDeviceState {
        object Init : MemberDeviceState()
        data class Loading(val isLoading: Boolean) : MemberDeviceState()
        data class Success(val isSuccess: Any) : MemberDeviceState()
        data class Failure(val message: String) : MemberDeviceState()
    }

    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }

    sealed class PopupUIState {
        object Init: PopupUIState()
        data class Loading(val isLoading: Boolean) : PopupUIState()
        data class Success(val popupVo: PopupVo) : PopupUIState()
        data class Failure(val message: String) : PopupUIState()
    }
}
package run.piece.dev.refactoring.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.install.model.AppUpdateType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.base.ErrorResponseDto
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor (
    private val resourcesProvider: ResourcesProvider,
    private val memberDeviceCheckUseCase: MemberDeviceCheckUseCase): ViewModel() {
    val updateType = AppUpdateType.IMMEDIATE
    val updateCode = 3040

    private val _serverChkObj = MutableLiveData<ErrorResponseDto>()
    val serverChkObj: LiveData<ErrorResponseDto> get() =_serverChkObj

    val memberId: String
        get() = PrefsHelper.read("memberId", "")

    private val deviceId: String
        get() = PrefsHelper.read("deviceId", "")

    val isLogin: String
        get() = PrefsHelper.read("isLogin", "")

    var appVersion: String
        set(value) = PrefsHelper.write("appVersion", value)
        get() = PrefsHelper.read("appVersion", "")

    private val _deviceChk: MutableStateFlow<MemberDeviceState> = MutableStateFlow(MemberDeviceState.Init)
    val deviceChk: StateFlow<MemberDeviceState> = _deviceChk.asStateFlow()

    // 다른기기 로그인 체크
    fun memberDeviceChk() {
        viewModelScope.launch {
            if (isLogin.isNotEmpty() && memberId.isNotEmpty()) {
                memberDeviceCheckUseCase.invoke(
                    memberId = memberId,
                    deviceId = deviceId,
                    memberAppVersion = appVersion
                ).onStart {
                    _deviceChk.value = MemberDeviceState.Loading(true)
                }.catch { exception ->
                    _deviceChk.value = MemberDeviceState.Loading(false)
                    _deviceChk.value = MemberDeviceState.Failure(exception.message.toString())
                }.collect {
                    LogUtil.v("BaseVo : $it")
                    _deviceChk.value = MemberDeviceState.Loading(false)
                    _deviceChk.value = MemberDeviceState.Success(it)
                }
            }
        }
    }

    sealed class MemberDeviceState {
        object Init : MemberDeviceState()
        data class Loading(val isLoading: Boolean) : MemberDeviceState()
        data class Success(val isSuccess: BaseVo?) : MemberDeviceState()
        data class Failure(val message: String) : MemberDeviceState()
    }
}
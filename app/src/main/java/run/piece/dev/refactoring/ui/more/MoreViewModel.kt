package run.piece.dev.refactoring.ui.more

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
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(private val memberInfoGetUseCase: MemberInfoGetUseCase,
                                        private val memberDeviceCheckUseCase: MemberDeviceCheckUseCase) : ViewModel() {

    val isLogin: String = PrefsHelper.read("isLogin","")
    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val appVersion: String = PrefsHelper.read("appVersion", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")

    private val _deviceChk: MutableStateFlow<MemberDeviceState> = MutableStateFlow(MemberDeviceState.Init)
    val deviceChk: StateFlow<MemberDeviceState> = _deviceChk.asStateFlow()

    private val _memberInfo: MutableStateFlow<MembeInfoState> = MutableStateFlow(MembeInfoState.Init)
    val memberInfo: StateFlow<MembeInfoState> = _memberInfo.asStateFlow()

    fun getMemberDeviceCheck() {
        viewModelScope.launch {
            memberDeviceCheckUseCase(
                memberId = memberId,
                deviceId = deviceId,
                memberAppVersion = appVersion
            ).onStart {
                _deviceChk.value = MemberDeviceState.Loading(true)
            }.catch {exception ->
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
                _memberInfo.value = MembeInfoState.Loading(true)
            }.catch { exception ->
                _memberInfo.value = MembeInfoState.Loading(false)
                _memberInfo.value = MembeInfoState.Failure(exception.message.toString())
            }.collect {
                _memberInfo.value = MembeInfoState.Loading(false)
                setPrefData(memberVo = it)
                _memberInfo.value = MembeInfoState.Success(it)
            }
        }
    }

    private fun setPrefData(memberVo: MemberVo) {
        PrefsHelper.write("name", memberVo.name)
        PrefsHelper.write("pinNumber", memberVo.pinNumber)
        PrefsHelper.write("cellPhoneNo", memberVo.cellPhoneNo)
        PrefsHelper.write("cellPhoneIdNo", memberVo.cellPhoneIdNo)
        PrefsHelper.write("birthDay", memberVo.birthDay)
        PrefsHelper.write("zipCode", memberVo.zipCode)
        PrefsHelper.write("baseAddress", memberVo.baseAddress)
        PrefsHelper.write("detailAddress", memberVo.detailAddress)
        PrefsHelper.write("gender", memberVo.gender)
        PrefsHelper.write("email", memberVo.email)

        PrefsHelper.write("idNo", memberVo.idNo)
        PrefsHelper.write("memberId", memberVo.memberId)
        PrefsHelper.write("ci", memberVo.ci)
        PrefsHelper.write("di", memberVo.di)
        PrefsHelper.write("createdAt", memberVo.createdAt)
        PrefsHelper.write("joinDay", memberVo.joinDay)
        PrefsHelper.write("vran", memberVo.vran)
        PrefsHelper.write("requiredConsentDate",memberVo.requiredConsentDate) // 필수약관 동의 시간 저장
        PrefsHelper.write("notRequiredConsentDate",memberVo.notRequiredConsentDate) // 선택 약관 동의 시간 저장
        PrefsHelper.write("assetNotification",memberVo.notification.assetNotification)
        PrefsHelper.write("portfolioNotification",memberVo.notification.portfolioNotification)
        PrefsHelper.write("marketingSms",memberVo.notification.marketingSms)
        PrefsHelper.write("marketingApp",memberVo.notification.marketingApp)
        PrefsHelper.write("isAd",memberVo.notification.isAd)
        PrefsHelper.write("isNotice",memberVo.notification.isNotice)
    }

    sealed class MemberDeviceState {
        object Init: MemberDeviceState()
        data class Loading(val isLoading: Boolean): MemberDeviceState()
        data class Success(val isSuccess: Any): MemberDeviceState()
        data class Failure(val message: String): MemberDeviceState()
    }

    sealed class MembeInfoState {
        object Init: MembeInfoState()
        data class Loading(val isLoading: Boolean): MembeInfoState()
        data class Success(val memberVo: MemberVo): MembeInfoState()
        data class Failure(val message: String): MembeInfoState()
    }
}
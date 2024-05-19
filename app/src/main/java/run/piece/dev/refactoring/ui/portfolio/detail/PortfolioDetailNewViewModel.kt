package run.piece.dev.refactoring.ui.portfolio.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
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
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.alarm.model.PortfolioGetAlarmVo
import run.piece.domain.refactoring.alarm.usecase.AlarmPortfolioDeleteUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPortfolioGetUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPortfolioSendUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import run.piece.domain.refactoring.portfolio.usecase.PortfolioDetailGetUseCase
import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.model.PurchaseInfoVo
import run.piece.domain.refactoring.purchase.usecase.PurchaseCancelUseCase
import run.piece.domain.refactoring.purchase.usecase.PurchaseInfoUseCase
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject


@HiltViewModel
class PortfolioDetailNewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider,
    private val portfolioDetailGetUseCase: PortfolioDetailGetUseCase,
    private val purchaseInfoUseCase: PurchaseInfoUseCase,
    private val purchaseCancelUseCase: PurchaseCancelUseCase,
    private val alarmPortfolioSendUseCase: AlarmPortfolioSendUseCase,
    private val alarmPortfolioDeleteUseCase: AlarmPortfolioDeleteUseCase,
    private val alarmPortfolioGetUseCase: AlarmPortfolioGetUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase
) : ViewModel() {

    private val portfolioId: String? = savedStateHandle.get<String>("portfolioId")

    var portfolioDetailDefaultVo: PortfolioDetailDefaultVo? = null

    var accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private val _purchaseCancelState: MutableStateFlow<PurchaseCancelState> = MutableStateFlow(PurchaseCancelState.Loading)
    val purchaseCancelState: StateFlow<PurchaseCancelState> = _purchaseCancelState.asStateFlow()

    private val _purchaseInfoState: MutableStateFlow<PurchaseInfoState> = MutableStateFlow(PurchaseInfoState.Init)
    val purchaseInfoState: StateFlow<PurchaseInfoState> = _purchaseInfoState.asStateFlow()

    private val _portfolioDetail: MutableStateFlow<PortfolioDetailState> = MutableStateFlow(PortfolioDetailState.Init)
    val portfolioDetail: StateFlow<PortfolioDetailState> get() = _portfolioDetail.asStateFlow()

    private val _portfolioRefreshDetail: MutableStateFlow<PortfolioRefreshDetailState> = MutableStateFlow(PortfolioRefreshDetailState.Init)
    val portfolioRefreshDetail: StateFlow<PortfolioRefreshDetailState> get() = _portfolioRefreshDetail.asStateFlow()

    private val _sendAlarm: MutableStateFlow<AlarmPortfolioSendState> = MutableStateFlow(AlarmPortfolioSendState.Init)
    val sendAlarm: StateFlow<AlarmPortfolioSendState> get() = _sendAlarm.asStateFlow()

    private val _deleteAlarm: MutableStateFlow<AlarmPortfolioDeleteState> = MutableStateFlow(AlarmPortfolioDeleteState.Init)
    val deleteAlarm: StateFlow<AlarmPortfolioDeleteState> get() = _deleteAlarm.asStateFlow()

    private val _alarmState: MutableStateFlow<AlarmPortfolioGetState> = MutableStateFlow(AlarmPortfolioGetState.Init)
    val alarmState: StateFlow<AlarmPortfolioGetState> get() = _alarmState.asStateFlow()

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()

    val achievementRateLiveData = MutableLiveData<String>("0")
    val offerMemberCountLiveData = MutableLiveData<String>("0")
    val recruitmentStateLiveData = MutableLiveData<String>()
    val notificationCountLiveData = MutableLiveData<String>("0")

    var pieceInfoLayoutHeight = 0

    // 청약신청 버튼 높이
    var buttonHeight = 0

    // 상단 카드뷰 높이
    var cardHeight = 0

    var hideBarHeight = 0

    var portfolioIvAlpha = 1F
    var portfolioIvScale = 1F
    var headerAlpha = 0F

    var isButtonShow = false

    var offerId = ""

    /** 관리자 PDF 등록 코드
    PAF0201	- PAF02 - 증권신고서
    PAF0202 - PAF02 - 투자설명서
    PAF0203	- PAF02 - 청약안내문 */
    var investmentProspectusUrl = ""

    //청약 상세 조회
    fun getPortfolioDetail(isRefresh: Boolean = false) {
        viewModelScope.launch {
            portfolioId?.let { portfolioId ->
                portfolioDetailGetUseCase(memberId, apiVersion = "v0.0.2", portfolioId)
                    .onStart {
                        if (isRefresh) _portfolioRefreshDetail.value = PortfolioRefreshDetailState.IsLoading(true)
                        else _portfolioDetail.value = PortfolioDetailState.IsLoading(true)
                    }
                    .catch { exception ->
                        if (isRefresh) {
                            _portfolioRefreshDetail.value = PortfolioRefreshDetailState.IsLoading(false)
                            _portfolioRefreshDetail.value = PortfolioRefreshDetailState.Failure(exception.message.default())
                        } else {
                            _portfolioDetail.value = PortfolioDetailState.IsLoading(false)
                            _portfolioDetail.value = PortfolioDetailState.Failure(exception.message.default())
                        }

                    }
                    .collect {
                        investmentProspectusUrl = it.attachFile.getAttachFilePositionUrl("PAF0202")
                        if (isRefresh) {
                            _portfolioRefreshDetail.value = PortfolioRefreshDetailState.IsLoading(false)
                            _portfolioRefreshDetail.value = PortfolioRefreshDetailState.Success(it)

                            portfolioDetailDefaultVo = it.detailDefault
                        } else {
                            _portfolioDetail.value = PortfolioDetailState.IsLoading(false)
                            _portfolioDetail.value = PortfolioDetailState.Success(it)

                            portfolioDetailDefaultVo = it.detailDefault
                        }
                    }
            }
        }
    }

    //청약 알림 신청
    fun sendPortfolioAlarm() = viewModelScope.launch {
        portfolioId?.let {
            memberId?.let { memberId ->
                alarmPortfolioSendUseCase(accessToken, deviceId, memberId, it)
                    .onStart {
                        _sendAlarm.value = AlarmPortfolioSendState.IsLoading(true)
                    }
                    .catch { exception ->
                        _sendAlarm.value = AlarmPortfolioSendState.IsLoading(false)
                        _sendAlarm.value = AlarmPortfolioSendState.Failure(exception.message.default())
                    }
                    .collect {
                        _sendAlarm.value = AlarmPortfolioSendState.IsLoading(false)
                        _sendAlarm.value = AlarmPortfolioSendState.Success(true, it.data)
                    }
            }
        }
    }

    //청약 알림 취소
    fun deletePortfolioAlarm() = viewModelScope.launch {
        portfolioId?.let {
            memberId?.let { memberId ->
                alarmPortfolioDeleteUseCase(accessToken, deviceId, memberId, it)
                    .onStart {
                        _deleteAlarm.value = AlarmPortfolioDeleteState.IsLoading(true)
                    }
                    .catch { exception ->
                        _deleteAlarm.value = AlarmPortfolioDeleteState.IsLoading(false)
                        _deleteAlarm.value = AlarmPortfolioDeleteState.Failure(exception.message.default())
                    }
                    .collect {
                        _deleteAlarm.value = AlarmPortfolioDeleteState.IsLoading(false)
                        _deleteAlarm.value = AlarmPortfolioDeleteState.Success(true, it.data)
                    }
            }
        }
    }

    //청약 알람 리스트 조회
    fun getPortfolioAlarm(isInitAndRefresh: Boolean? = true) = viewModelScope.launch {
        portfolioId?.let {
            memberId?.let { memberId ->
                alarmPortfolioGetUseCase(accessToken, deviceId, memberId, it)
                    .onStart {
                        _alarmState.value = AlarmPortfolioGetState.IsLoading(true)
                    }
                    .catch { exception ->
                        _alarmState.value = AlarmPortfolioGetState.IsLoading(false)
                        _alarmState.value = AlarmPortfolioGetState.Failure(exception.message.default())
                    }
                    .collect {
                        _alarmState.value = AlarmPortfolioGetState.IsLoading(false)
                        isInitAndRefresh?.let { bool ->
                            _alarmState.value = AlarmPortfolioGetState.Success(it, bool)
                        }
                    }
            }
        }
    }

    //청약 정보 조회
    fun getPurchaseInfo() = viewModelScope.launch {
        portfolioId?.let {
            purchaseInfoUseCase("Bearer $accessToken", deviceId, memberId, it)
                .onStart {
                    _purchaseInfoState.value = PurchaseInfoState.IsLoading(true)
                }.catch { exception ->
                    _purchaseInfoState.value = PurchaseInfoState.IsLoading(false)
                    _purchaseInfoState.value = PurchaseInfoState.Failure(exception.message.default())
                }.collect {
                    _purchaseInfoState.value = PurchaseInfoState.IsLoading(false)
                    _purchaseInfoState.value = PurchaseInfoState.Success(it)
                }
        }
    }

    //청약 취소
    fun purchaseCancel() {
        viewModelScope.launch {
            _purchaseCancelState.value = PurchaseCancelState.Loading
            memberId?.let { memberId ->
                purchaseCancelUseCase.deletePurchaseUseCase(
                    accessToken = "Bearer $accessToken",
                    deviceId = deviceId,
                    memberId = memberId,
                    PurchaseCancelModel(offerId)
                ).onStart {
                    _purchaseCancelState.value = PurchaseCancelState.Loading
                }.catch { exception ->
                    _purchaseCancelState.value = PurchaseCancelState.Loading
                    _purchaseCancelState.value = PurchaseCancelState.Failure(exception.message.toString())
                }.collect { item ->
                    _purchaseCancelState.value = PurchaseCancelState.Loading
                    _purchaseCancelState.value = PurchaseCancelState.Success(item)
                }
            }
        }
    }

    //회원 정보 조회
    fun getMemberData(portfolioDetailVo: PortfolioDetailVo? = null) {
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
                portfolioDetailVo?.let { vo ->
                    _memberInfo.value = MemberInfoState.Success(it, vo)
                } ?: run {
                    _memberInfo.value = MemberInfoState.Success(it)
                }
            }
        }
    }

    fun getMemberDataNvo() {
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

    fun getStatusBarHeight(): Int = resourcesProvider.getStatusBarHeight()
    fun getDeviceHeight(): Int = resourcesProvider.getDeviceHeight()
    fun getDeviceWidth(): Int = resourcesProvider.getDeviceWidth()
    fun dpToPixel(pixel: Int): Int = resourcesProvider.dpToPixel(pixel)

    fun getInitParamHeight(): Int {
        return (getDeviceHeight() + getStatusBarHeight() + cardHeight) - pieceInfoLayoutHeight
    }

    fun getInitScrollHeight(): Int {
        return cardHeight + buttonHeight
    }

    fun List<AttachFileItemVo>.getAttachFilePositionUrl(key: String): String {
        if (size > 1) {
            forEach {
                if (key == it.attachFileCode) return it.attachFilePath
            }
        } else return ""
        return ""
    }


    // 만 나이 구하기
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateKoreanAge(birthDate: LocalDate, currentDate: LocalDate): Int {
        val age = Period.between(birthDate, currentDate).years
        val isBeforeBirthday = currentDate.monthValue < birthDate.monthValue ||
                (currentDate.monthValue == birthDate.monthValue &&
                        currentDate.dayOfMonth < birthDate.dayOfMonth)
        return if (isBeforeBirthday) age - 1 else age
    }

    sealed class PurchaseInfoState {
        object Init : PurchaseInfoState()
        data class IsLoading(val isLoading: Boolean) : PurchaseInfoState()
        data class Success(val purchaseInfoVo: PurchaseInfoVo) : PurchaseInfoState()
        data class Failure(val message: String) : PurchaseInfoState()
    }

    sealed class PurchaseCancelState {
        object Loading : PurchaseCancelState()
        object Empty : PurchaseCancelState()
        data class Success(val baseVo: BaseVo?) : PurchaseCancelState()
        data class Failure(val message: String) : PurchaseCancelState()
    }

    sealed class PortfolioDetailState {
        object Init : PortfolioDetailState()
        data class IsLoading(val isLoading: Boolean) : PortfolioDetailState()
        data class Success(val portfolioDetailVo: PortfolioDetailVo) : PortfolioDetailState()
        data class Failure(val message: String) : PortfolioDetailState()
    }

    sealed class PortfolioRefreshDetailState {
        object Init : PortfolioRefreshDetailState()

        data class IsLoading(val isLoading: Boolean) : PortfolioRefreshDetailState()
        data class Success(val portfolioDetailVo: PortfolioDetailVo) : PortfolioRefreshDetailState()
        data class Failure(val message: String) : PortfolioRefreshDetailState()
    }

    sealed class AlarmPortfolioSendState {
        object Init : AlarmPortfolioSendState()
        data class IsLoading(val isLoading: Boolean) : AlarmPortfolioSendState()
        data class Success(val isSuccess: Boolean, val notificationCount: Int) : AlarmPortfolioSendState()
        data class Failure(val message: String) : AlarmPortfolioSendState()
    }

    sealed class AlarmPortfolioDeleteState {
        object Init : AlarmPortfolioDeleteState()
        data class IsLoading(val isLoading: Boolean) : AlarmPortfolioDeleteState()
        data class Success(val isSuccess: Boolean, val notificationCount: Int) : AlarmPortfolioDeleteState()
        data class Failure(val message: String) : AlarmPortfolioDeleteState()
    }

    sealed class AlarmPortfolioGetState {
        object Init : AlarmPortfolioGetState()
        data class IsLoading(val isLoading: Boolean) : AlarmPortfolioGetState()
        data class Success(val portfolioGetAlarmVo: PortfolioGetAlarmVo, val isInit: Boolean) : AlarmPortfolioGetState()
        data class Failure(val message: String) : AlarmPortfolioGetState()
    }

    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo, val portfolioDetailVo: PortfolioDetailVo? = null) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }
}
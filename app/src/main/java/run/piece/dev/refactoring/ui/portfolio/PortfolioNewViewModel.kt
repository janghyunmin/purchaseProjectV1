package run.piece.dev.refactoring.ui.portfolio

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okhttp3.Request
import run.piece.dev.data.BuildConfig
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.utils.default
import run.piece.dev.refactoring.ui.alarm.AlarmViewModel
import run.piece.dev.refactoring.ui.main.MainViewModel
import run.piece.dev.refactoring.ui.portfolio.detail.TimeDifference
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.alarm.model.AlarmItemVo
import run.piece.domain.refactoring.alarm.usecase.AlarmListGetUseCase
import run.piece.domain.refactoring.investment.model.request.InvestRiskModel
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.MemberPostInvestUseCase
import run.piece.domain.refactoring.portfolio.model.PortfolioListVo
import run.piece.domain.refactoring.portfolio.usecase.PortfolioListGetUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class PortfolioNewViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val savedStateHandle: SavedStateHandle,
    private val portfolioGetUseCase: PortfolioListGetUseCase,
    private val memberDeviceCheckUseCase: MemberDeviceCheckUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val alarmListGetUseCase: AlarmListGetUseCase,
    private val memberPostInvestUseCase: MemberPostInvestUseCase // 투자 위험 동의 UseCase 추가
) : ViewModel() {
    private val mat = floatArrayOf(
        1 / 3f, 1 / 3f, 1 / 3f, 0f, 0f,
        1 / 3f, 1 / 3f, 1 / 3f, 0f, 0f,
        1 / 3f, 1 / 3f, 1 / 3f, 0f, 0f,
        0f, 0f, 0f, 0.5f, 0f
    )

    private val appVersion: String = PrefsHelper.read("appVersion", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val isLogin: String = PrefsHelper.read("isLogin", "")
    val deepLink: String = savedStateHandle.get<String>("deepLink") ?: ""

    private val _portfolioList: MutableStateFlow<PortfolioState> = MutableStateFlow(PortfolioState.Init)
    val portfolioList: StateFlow<PortfolioState> get() = _portfolioList.asStateFlow()

    private val _alarmList: MutableStateFlow<AlarmViewModel.AlarmGetState> = MutableStateFlow(AlarmViewModel.AlarmGetState.Init)
    val alarmList: StateFlow<AlarmViewModel.AlarmGetState> get() = _alarmList.asStateFlow()

    private val _deviceChk: MutableStateFlow<MainViewModel.MemberDeviceState> = MutableStateFlow(MainViewModel.MemberDeviceState.Init)
    val deviceChk: StateFlow<MainViewModel.MemberDeviceState> = _deviceChk.asStateFlow()

    private val _memberInfo: MutableStateFlow<MainViewModel.MemberInfoState> = MutableStateFlow(MainViewModel.MemberInfoState.Init)
    val memberInfo: StateFlow<MainViewModel.MemberInfoState> = _memberInfo.asStateFlow()

    private val _investAgreement: MutableStateFlow<MemberInvestAgreementState> = MutableStateFlow(MemberInvestAgreementState.Init)
    val investAgreement: StateFlow<MemberInvestAgreementState> = _investAgreement.asStateFlow()

    private var disposable: Disposable? = null // Disposable 추가



    private val _wsStatus: MutableLiveData<Boolean> = MutableLiveData()
    val wsStatus: LiveData<Boolean> get() = _wsStatus

    var displayType: String = ""
    var userName : String = PrefsHelper.read("name","")


    val request: Request = Request.Builder().url(BuildConfig.PIECE_WS_PORTFOLIO).build()

    // 웹소켓 데이터
    private val _responseArray: MutableStateFlow<JsonArray?> = MutableStateFlow(null)
    val responseArray: StateFlow<JsonArray?> get() = _responseArray.asStateFlow()


    fun getDisplayType(displayMetrics: DisplayMetrics): String {
        return when {
            displayMetrics.widthPixels > 1600 -> {
                displayType = "FOLD_DISPLAY_EXPAND"
                displayType
            }

            displayMetrics.widthPixels < 980 -> {
                displayType = "FOLD_DISPLAY_COLLAPSE"
                displayType
            }

            else -> {
                displayType = "BASIC_DISPLAY"
                displayType
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        // onCleared() 될 때 dispose() 호출
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
    }

    suspend fun getPortfolio() {
        viewModelScope.launch {
            portfolioGetUseCase.getPortfolioList("v0.0.2", 500)
                .onStart {
                    _portfolioList.value = PortfolioState.Loading(true)
                }.catch { exception ->
                    _portfolioList.value = PortfolioState.Loading(false)
                    _portfolioList.value = PortfolioState.Failure(exception.message.toString())
                }.collect {
                    _portfolioList.value = PortfolioState.Loading(false)
                    _portfolioList.value = PortfolioState.Success(it)
                }
        }
    }


    suspend fun getAlarm(type: String) =
        viewModelScope.launch {
            alarmListGetUseCase(accessToken, deviceId, memberId, type)
                .onStart {
                    _alarmList.value = AlarmViewModel.AlarmGetState.IsLoading(true)
                }.catch { exception ->
                    _alarmList.value = AlarmViewModel.AlarmGetState.IsLoading(false)
                    _alarmList.value = AlarmViewModel.AlarmGetState.Failure(exception.message.toString())
                }.collect {
                    _alarmList.value = AlarmViewModel.AlarmGetState.IsLoading(false)
                    _alarmList.value = AlarmViewModel.AlarmGetState.Success(type, it)
                }
        }

    fun memberDeviceChk() {
        viewModelScope.launch {
            memberDeviceCheckUseCase(
                memberId = memberId,
                deviceId = deviceId,
                memberAppVersion = appVersion
            ).onStart {
                _deviceChk.value = MainViewModel.MemberDeviceState.Loading(true)
            }.catch { exception ->
                _deviceChk.value = MainViewModel.MemberDeviceState.Loading(false)
                _deviceChk.value = MainViewModel.MemberDeviceState.Failure(exception.message.toString())
            }.collect {
                _deviceChk.value = MainViewModel.MemberDeviceState.Loading(false)
                _deviceChk.value = MainViewModel.MemberDeviceState.Success(it.default())
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
                _memberInfo.value = MainViewModel.MemberInfoState.Loading(true)
            }.catch { exception ->
                _memberInfo.value = MainViewModel.MemberInfoState.Loading(false)
                _memberInfo.value = MainViewModel.MemberInfoState.Failure(exception.message.toString())
            }.collect {
                _memberInfo.value = MainViewModel.MemberInfoState.Loading(false)
                _memberInfo.value = MainViewModel.MemberInfoState.Success(it)
            }
        }
    }


    fun postInvestAgreement(portfolioId: String) {
        viewModelScope.launch {
            memberPostInvestUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                investRiskModel = InvestRiskModel(portfolioId = portfolioId)
            ).onStart {
                _investAgreement.value = MemberInvestAgreementState.Loading(true)
            }.catch { exception ->
                _investAgreement.value = MemberInvestAgreementState.Loading(false)
                _investAgreement.value = MemberInvestAgreementState.Failure(exception.message.toString())
            }.collect {
                _investAgreement.value = MemberInvestAgreementState.Loading(false)
                _investAgreement.value = MemberInvestAgreementState.Success(it)
            }
        }
    }


    sealed class PortfolioState {
        object Init : PortfolioState()
        data class Loading(val isLoading: Boolean) : PortfolioState()
        data class Success(val isSuccess: PortfolioListVo) : PortfolioState()
        data class Failure(val message: String) : PortfolioState()
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

    sealed class AlarmGetState {
        object Init : AlarmGetState()
        data class IsLoading(val isLoading: Boolean) : AlarmGetState()
        data class Success(val type: String, val alarmList: List<AlarmItemVo>) : AlarmGetState()
        data class Failure(val message: String) : AlarmGetState()
    }

    sealed class MemberInvestAgreementState {
        object Init : MemberInvestAgreementState()
        data class Loading(val isLoading: Boolean) : MemberInvestAgreementState()
        data class Success(val isSuccess: BaseVo?) : MemberInvestAgreementState()
        data class Failure(val message: String) : MemberInvestAgreementState()
    }



    // 웹소켓 데이터 Array
    fun ptWsArrayData(responseArray: JsonArray) {
        viewModelScope.launch {
            _responseArray.value = responseArray
        }
    }

    fun portfolioTimerStatus(days: Long, hours: Long, minutes: Long, seconds: Long): Boolean? {
        _wsStatus.value = hours.toInt() == 0 && minutes.toInt() == 0 && seconds.toInt() == 0
        return _wsStatus.value
    }




    fun getGrayscaleColor(): FloatArray = mat


    // 배너 이미지 Radius
    fun defaultBannerRadius(): Int {
        return when (displayType) {
            "FOLD_DISPLAY_EXPAND" -> 72
            else -> 16
        }
    }


    // 하단 배너 radius
    fun footerBannerRadius(
        context: Context,
        cornerRadiusDp: Int
    ): Int {
        val cornerRadiusDp = cornerRadiusDp

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            cornerRadiusDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    fun footerBannerLeftMargins(): Int {
        return dpToPixels(8f)
    }

    fun footerBannerRightMargins(): Int {
        return dpToPixels(8f)
    }

    fun footerBannerBottomMargins(): Int {
        return when (displayType) {
            "FOLD_DISPLAY_EXPAND" -> dpToPixels(8f)
            else -> dpToPixels(16f)
        }
    }

    fun footerBannerTopToTop(): Int {
        return ConstraintLayout.LayoutParams.UNSET
    }

    fun footerBannerBottomToBottom(): Int {
        return ConstraintLayout.LayoutParams.PARENT_ID
    }

    fun footerBannerStartToStart(): Int {
        return when (displayType) {
            "FOLD_DISPLAY_EXPAND" -> ConstraintLayout.LayoutParams.UNSET
            else -> ConstraintLayout.LayoutParams.PARENT_ID
        }
    }

    fun footerBannerEndToEnd(): Int {
        return ConstraintLayout.LayoutParams.PARENT_ID
    }

    // 하단 배너 position
    fun dpToPixels(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getStatusText(
        recruitmentBeginDate: String,
        achievementRate: String,
        recruitmentState: String,
        dividendsExpecatationDate: String
    ): String {
        var textValue = ""
        val timeDifference2 = timeDifference2(dividendsExpecatationDate)

        when (recruitmentState) {
            // 모집 예정
            "PRS0101" -> {
                // 로직 변경으로 인하여 주석
//                textValue = getResultTimeText(recruitmentBeginDate)
            }
            // 모집 중
            "PRS0102" -> {
                // 테스트시 주석
                textValue = "$achievementRate%\n모집되었어요"
            }

            // 모집 마감
            "PRS0103" -> textValue = "청약이\n마감되었어요"

            // 분배 예정
            "PRS0104" -> {
                textValue = if (timeDifference2.day == 0) {
                    "오늘은\n만기일이에요"
                } else {
                    "만기까지\n${timeDifference2.day}일 남았어요"
                }
            }

            // 분배 예정 - 분배 만기
            "PRS0108" -> {
                textValue = "오늘은\n만기일이에요"
            }

            "PRS0111" -> {
                textValue = "분배금이\n지급되었어요"
            }

            else -> {
                textValue = "만기까지\n${timeDifference2.day}일 남았어요"
            }
        }
        return textValue
    }

    /** 현재시간 구하기 ["yyyy-MM-dd HH:mm:ss"] (*HH: 24시간)*/
    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        var now = System.currentTimeMillis()
        var date = Date(now)

        var dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var getTime = dateFormat.format(date)

        return getTime
    }

    /** 두 날짜 사이의 간격 계산해서 텍스트로 반환 */
    @SuppressLint("SimpleDateFormat")
    fun getResultTimeText(beforeDate: String): String {
        val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
        val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beforeDate)
        val diffMilliseconds = nowFormat.time - beforeFormat.time
        val diffSeconds = diffMilliseconds / 1000
        val diffDays = diffMilliseconds / (24 * 60 * 60 * 1000)
        val nowCalendar = Calendar.getInstance().apply { time = nowFormat }
        val beforeCalendar = Calendar.getInstance().apply { time = beforeFormat }
        val diffYears = nowCalendar.get(Calendar.YEAR) - beforeCalendar.get(Calendar.YEAR)
        var diffMonths =
            diffYears * 12 + nowCalendar.get(Calendar.MONTH) - beforeCalendar.get(Calendar.MONTH)
        if (nowCalendar.get(Calendar.DAY_OF_MONTH) < beforeCalendar.get(Calendar.DAY_OF_MONTH)) {
            // 현재 날짜의 일(day) 값이 이전 날짜의 일(day) 값보다 작은 경우에만 월 차이를 1 감소시킴
            // Ex) 5.31일과 6.2일은 2일차이지만 month가 1로 계속 나오는 이슈해결을 위해서
            diffMonths--
        }

        var secondMsg = TimeUnit.SECONDS.toSeconds(diffSeconds)
            .minus(TimeUnit.SECONDS.toMinutes(diffSeconds) * 60)
        var minuteMsg = TimeUnit.SECONDS.toMinutes(diffSeconds)
            .minus(TimeUnit.SECONDS.toHours(diffSeconds) * 60)
        var hourMsg =
            TimeUnit.SECONDS.toHours(diffSeconds)
                .minus(TimeUnit.SECONDS.toDays(diffSeconds) * 24)
        var result = ""


        if (diffDays < 0) {
            result = "청약까지 \n" +
                    diffDays.toString().replace("-", "") + "일 " +
                    hourMsg.toString().replace("-", "") + "시간 남았어요"
        } else if (hourMsg < 0) {
            result = "청약까지 \n" +
                    hourMsg.toString().replace("-", "") + "시간 " +
                    minuteMsg.toString().replace("-", "") + "분 남았어요"
        } else if (minuteMsg < 0) {
            result = "청약까지 \n" + minuteMsg.toString().replace("-", "") + "분 " +
                    secondMsg.toString().replace("-", "") + "초 남았어요"
        } else {
            result = "청약까지 \n" + "00분 " + secondMsg.toString().replace("-", "") + "초 남았어요"
        }

        return result.replace("-", "")
    }

    fun newResultText(day: Long, hours: Long, minute: Long, second: Long): String {
        var result = ""
        if(day > 0) {
            result = "청약까지 \n" +
                    day + "일 " +
                    hours + "시간 남았어요"
        } else if(hours > 0) {
            result = "청약까지 \n" +
                    hours + "시간 " +
                    minute + "분 남았어요"
        } else if(minute > 0) {
            result = "청약까지 \n" + minute + "분 " +
                    second + "초 남았어요"
        } else {
            result = "청약까지 \n" + "00분 " + second + "초 남았어요"
        }
        return result
    }



    @SuppressLint("SimpleDateFormat")
    fun timeDifference2(date: String): TimeDifference {
        val dateFormat = SimpleDateFormat("yyyy-M-d")
        val now = dateFormat.format(Date(System.currentTimeMillis()))

        val nowFormat = dateFormat.parse(now)
        val afterFormat = dateFormat.parse(date)

        var diffMilliseconds = afterFormat.time - nowFormat.time

        var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(diffMilliseconds)
        var day = TimeUnit.SECONDS.toDays(seconds).toInt()
        var hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        var minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        var second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60

        if (day <= 0) day = 0
        if (hours <= 0) hours = 0
        if (minute <= 0) minute = 0
        if (second <= 0) second = 0
        if (seconds <= 0) diffMilliseconds = 0L

        return TimeDifference(day, hours, minute, second, diffMilliseconds)
    }

}
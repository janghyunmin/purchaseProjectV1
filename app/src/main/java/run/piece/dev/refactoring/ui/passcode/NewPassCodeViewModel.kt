package run.piece.dev.refactoring.ui.passcode

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.module.BiometricAuthenticationManager
import run.piece.dev.data.refactoring.ui.purchase.api.PurchaseApi
import run.piece.dev.data.utils.default
import run.piece.dev.refactoring.ui.main.MainViewModel
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.member.model.AuthPinErrorVo
import run.piece.domain.refactoring.member.model.AuthPinVo
import run.piece.domain.refactoring.member.model.Consents
import run.piece.domain.refactoring.member.model.DeviceInfo
import run.piece.domain.refactoring.member.model.JoinBodyVo
import run.piece.domain.refactoring.member.model.JoinVo
import run.piece.domain.refactoring.member.model.MemberPinModel
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.NotificationInfo
import run.piece.domain.refactoring.member.model.SsnVo
import run.piece.domain.refactoring.member.usecase.GetAccessTokenUseCase
import run.piece.domain.refactoring.member.usecase.GetAuthPinUseCase
import run.piece.domain.refactoring.member.usecase.GetSsnYnUseCase
import run.piece.domain.refactoring.member.usecase.JoinPostUseCase
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.PutAuthPinUseCase
import run.piece.domain.refactoring.purchase.model.PurchaseDefaultVo
import run.piece.domain.refactoring.purchase.model.PurchaseErrorItemVo
import run.piece.domain.refactoring.purchase.model.PurchaseModel
import run.piece.domain.refactoring.purchase.usecase.PurchaseOfferUseCase
import java.util.regex.Pattern
import javax.inject.Inject


@ViewModelScoped
enum class PassCodeState(val title: String, val id: Int) {
    FIRST("회원가입", 1), // 최초 진입시
    LOGIN("로그인", 2), // 기회원 로그인시
    CHANGE("비밀번호 변경", 3), // 비밀버호 변경 모달을 통하여 간편 비밀번호 변경시
    PURCHASE("포트폴리오 구매", 4), // 청약 신청시
    CLEAR("비밀번호 변경", 5), // 내정보 - 인증 및 보안 - 간편 비밀번호 변경시
    REAUTH("재인증",6) // 내정보 - 재인증
}

@HiltViewModel
class NewPassCodeViewModel @Inject constructor(
    private val api: PurchaseApi,
    private val savedStateHandle: SavedStateHandle,
    private val memberDeviceCheckUseCase: MemberDeviceCheckUseCase,
    private val joinPostUseCase: JoinPostUseCase, // 회원가입 UseCase
    private val getAccessTokenUseCase: GetAccessTokenUseCase, // 토큰 검증 UseCase
    private val getAuthPinUseCase: GetAuthPinUseCase, // 핀번호 검증 UseCase
    private val putAuthPinUseCase: PutAuthPinUseCase, // 핀번호 변경 UseCase
    private val getSsnYnUseCase: GetSsnYnUseCase, // 실명인증 여부 조회 UseCase
    private val purchaseOfferUseCase: PurchaseOfferUseCase, // 청약 신청 UseCase
    private val biometricAuthenticationManager: BiometricAuthenticationManager, // 생체인증
    private val memberInfoGetUseCase: MemberInfoGetUseCase // 회원 정보 조회
) : ViewModel() {

    private val accessToken: String = PrefsHelper.read("accessToken","")
    private val deviceId: String = PrefsHelper.read("deviceId", "")
    private val appVersion: String = PrefsHelper.read("appVersion", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val isLogin: String = PrefsHelper.read("isLogin", "")
    val isFido: String = PrefsHelper.read("isFido","N")
    val inputPinNumber: String = PrefsHelper.read("inputPinNumber","")

    private val _deviceChk: MutableStateFlow<MainViewModel.MemberDeviceState> = MutableStateFlow(MainViewModel.MemberDeviceState.Init)
    val deviceChk: StateFlow<MainViewModel.MemberDeviceState> = _deviceChk.asStateFlow()
    private val _joinResponse: MutableStateFlow<JoinState> = MutableStateFlow(JoinState.Init)
    val joinResponse: StateFlow<JoinState> = _joinResponse.asStateFlow()
    private val _tokenChk: MutableStateFlow<AccessTokenState> = MutableStateFlow(AccessTokenState.Init)
    val tokenChk: StateFlow<AccessTokenState> = _tokenChk.asStateFlow()
    private val _pinChk: MutableStateFlow<AuthPinState> = MutableStateFlow(AuthPinState.Init)
    val pinChk: StateFlow<AuthPinState> = _pinChk.asStateFlow()
    private val _pinUpdateChk: MutableStateFlow<AuthPinPutState> = MutableStateFlow(AuthPinPutState.Init)
    val pinUpdateChk: StateFlow<AuthPinPutState> = _pinUpdateChk.asStateFlow()
    private val _ssnChk: MutableStateFlow<SsnState> = MutableStateFlow(SsnState.Init)
    val ssnChk: StateFlow<SsnState> = _ssnChk.asStateFlow()
    private val _purchaseChk: MutableStateFlow<PurchaseState> = MutableStateFlow(PurchaseState.Init)
    val purchaseChk: StateFlow<PurchaseState> = _purchaseChk.asStateFlow()
    private val _bioIsLoginState = MutableStateFlow<String>("")
    val bioIsLoginState: StateFlow<String> get() = _bioIsLoginState

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()

    private var pwdVerifyCnt: Int = 0
    private var name: String = PrefsHelper.read("name","")
    private var birthDay: String = ""
    private var customBirthDay: String = ""
    private var cellPhoneNo: String = ""
    private var consents: List<Consents>? = null

    var code0 = MutableLiveData<String>()
    var code1 = MutableLiveData<String>()
    var code2 = MutableLiveData<String>()
    var code3 = MutableLiveData<String>()
    var code4 = MutableLiveData<String>()
    var code5 = MutableLiveData<String>()
    var code6 = MutableLiveData<String>()
    var code7 = MutableLiveData<String>()
    var code8 = MutableLiveData<String>()
    var code9 = MutableLiveData<String>()

    // 최초 셋팅
    init {
        viewModelScope.launch {
            when (savedStateHandle.get<String>("Step")) {
                "1", "3","6" -> {
                    name = savedStateHandle.get<Any>("name").toString()
                    birthDay = savedStateHandle.get<Any>("birthDay").toString()
                    customBirthDay = birthDay.substring(0, 4).plus("-").plus(birthDay.substring(4, 6)).plus("-").plus(birthDay.substring(6, 8))
                    cellPhoneNo = savedStateHandle.get<Any>("cellPhoneNo").toString()
                    consents = savedStateHandle.get<List<Consents>>("consentList")
                }

                "5" -> {
                    name = PrefsHelper.read("name", "")
                    birthDay = PrefsHelper.read("birthDay", "").replace("-", "")
                    customBirthDay = birthDay.substring(0, 4).plus("-").plus(birthDay.substring(4, 6)).plus("-").plus(birthDay.substring(6, 8))
                    cellPhoneNo = PrefsHelper.read("cellPhoneNo", "")
                }
            }

            val padList: MutableList<String?> = ArrayList()
            for (index in 0..9) {
                padList.add(index.toString())
            }
            padList.shuffle()

            code0.value = padList[0]
            code1.value = padList[1]
            code2.value = padList[2]
            code3.value = padList[3]
            code4.value = padList[4]
            code5.value = padList[5]
            code6.value = padList[6]
            code7.value = padList[7]
            code8.value = padList[8]
            code9.value = padList[9]

        }
    }

    fun padShuffle() {
        val padList: MutableList<String?> = ArrayList()
        for (index in 0..9) {
            padList.add(index.toString())
        }
        padList.shuffle()

        code0.value = padList[0]
        code1.value = padList[1]
        code2.value = padList[2]
        code3.value = padList[3]
        code4.value = padList[4]
        code5.value = padList[5]
        code6.value = padList[6]
        code7.value = padList[7]
        code8.value = padList[8]
        code9.value = padList[9]
    }

    // 간편비밀번호 화면 진입시 분기
    fun launchStep(): Int {
        when (savedStateHandle.get<String>("Step")) {
            "1" -> {
                return PassCodeState.FIRST.id
            }

            "2" -> {
                return PassCodeState.LOGIN.id
            }

            "3" -> {
                return PassCodeState.CHANGE.id
            }

            "4" -> {
                return PassCodeState.PURCHASE.id
            }

            "5" -> {
                return PassCodeState.CLEAR.id
            }
            "6" -> {
                return PassCodeState.REAUTH.id
            }

            else -> {
                return 1
            }
        }
    }

    // 상단 제목 변경
    fun topTitleTxt(context: Context, status: PassCodeState): String {
        return when (status) {
            PassCodeState.FIRST, PassCodeState.REAUTH -> {
                context.getString(R.string.passcode_title)
            }

            PassCodeState.LOGIN -> {
                context.getString(R.string.password_title_input)
            }

            PassCodeState.CHANGE -> {
                context.getString(R.string.password_title_change)
            }

            PassCodeState.PURCHASE -> {
                context.getString(R.string.password_title_input)
            }

            PassCodeState.CLEAR -> {
                context.getString(R.string.password_title_change)
            }
        }
    }

    // 서브 제목 변경
    fun subTitleTxt(context: Context, passWord: String, status: PassCodeState, oneMoreChk: Boolean, equalsChk: Boolean, retryCount: String, step: String): String {
        when (status) {
            PassCodeState.FIRST, PassCodeState.REAUTH -> {
                when (oneMoreChk) {
                    false -> {
                        context.getString(R.string.password_sub_title)
                    }

                    true -> {
                        return context.getString(R.string.password_sub_re_title)
                    }
                }

                when (equalsChk) {
                    false -> {
                        context.getString(R.string.password_sub_title)
                    }

                    true -> {
                        return context.getString(R.string.password_sub_verify_title)
                    }
                }

                when (passWord.length) {
                    3, 4, 5, 6 -> {
                        return if (repeatVerifyNumber(passWord = passWord)) {
                            context.getString(R.string.password_sub_verify_repeat_title)
                        } else if (continuousVerifyNumber(passWord = passWord)) {
                            context.getString(R.string.password_sub_verify_continuous_title)
                        } else if (birthVerifyNumber(passWord = passWord)) {
                            context.getString(R.string.password_sub_verify_info_title)
                        } else if (phoneVerifyNumber(passWord = passWord)) {
                            context.getString(R.string.password_sub_verify_info_title)
                        } else {
                            context.getString(R.string.password_sub_title)
                        }
                    }

                    else -> {
                        context.getString(R.string.password_sub_title)
                    }
                }
                return context.getString(R.string.password_sub_title)
            }

            PassCodeState.LOGIN -> {
                return when (equalsChk) {
                    false -> {
                        context.getString(R.string.password_sub_title)
                    }

                    true -> {
                        return String.format(context.getString(R.string.password_sub_not_equals), retryCount)
                    }
                }
            }

            PassCodeState.CHANGE -> {
                return context.getString(R.string.password_sub_title)
            }

            PassCodeState.PURCHASE -> {
                return when (equalsChk) {
                    false -> {
                        context.getString(R.string.password_sub_title)
                    }

                    true -> {
                        return context.getString(R.string.password_sub_purchase_title)
                    }
                }
            }

            PassCodeState.CLEAR -> {
                var result = ""
                when (step) {
                    "first" -> {
                        when (passWord.length) {
                            0 -> {
                                result = context.getString(R.string.password_sub_change_title)
                            }

                            6 -> {
                                result = if (!equalsChk) {
                                    context.getString(R.string.password_sub_title)
                                } else {
                                    context.getString(R.string.password_sub_purchase_title)
                                }
                            }

                            else -> {
                                result = context.getString(R.string.password_sub_title)
                            }
                        }
                    }

                    "second" -> {
                        when (passWord.length) {
                            0 -> {
                                result = context.getString(R.string.password_sub_change_next_title)
                            }

                            3, 4, 5, 6 -> {
                                result = if (repeatVerifyNumber(passWord = passWord)) {
                                    context.getString(R.string.password_sub_verify_repeat_title)
                                } else (if (continuousVerifyNumber(passWord = passWord)) {
                                    context.getString(R.string.password_sub_verify_continuous_title)
                                } else if (birthVerifyNumber(passWord = passWord)) {
                                    context.getString(R.string.password_sub_verify_info_title)
                                } else if (phoneVerifyNumber(passWord = passWord)) {
                                    context.getString(R.string.password_sub_verify_info_title)
                                } else {
                                    context.getString(R.string.password_sub_change_next_title)

                                    when (oneMoreChk) {
                                        false -> {
                                            context.getString(R.string.password_sub_title)
                                        }

                                        true -> {
                                            when (equalsChk) {
                                                false -> {
                                                    context.getString(R.string.password_sub_change_next_title)
                                                }

                                                true -> {
                                                    context.getString(R.string.password_sub_verify_equals_title)
                                                }
                                            }
                                        }
                                    }
                                }).toString()
                            }

                            else -> {
                                result = context.getString(R.string.password_sub_title)
                            }
                        }

                    }

                    "third" -> {
                        when (passWord.length) {
                            0 -> {
                                result = context.getString(R.string.password_sub_re_title)
                            }

                            3, 4, 5, 6 -> {
                                if (repeatVerifyNumber(passWord = passWord)) {
                                    result = context.getString(R.string.password_sub_verify_repeat_title)
                                } else if (continuousVerifyNumber(passWord = passWord)) {
                                    result = context.getString(R.string.password_sub_verify_continuous_title)
                                } else if (birthVerifyNumber(passWord = passWord)) {
                                    result = context.getString(R.string.password_sub_verify_info_title)
                                } else if (phoneVerifyNumber(passWord = passWord)) {
                                    result = context.getString(R.string.password_sub_verify_info_title)
                                }
                            }

                            else -> {
                                result = context.getString(R.string.password_sub_title)
                            }
                        }

                    }
                }




                return result
            }
        }
    }

    fun btnInitView(activity: FragmentActivity, status: PassCodeState, backIvLayout: ConstraintLayout, bioLayout: ConstraintLayout, checkBoxIv: AppCompatImageView, titleView: AppCompatTextView , retryIv: AppCompatImageView) {
        when (status) {
            PassCodeState.FIRST,PassCodeState.REAUTH -> {
                backIvLayout.visibility = View.VISIBLE
                backIvLayout.onThrottleClick {
                    activity.finish()
                }
                bioLayout.visibility = View.GONE
            }

            PassCodeState.LOGIN -> {
                backIvLayout.visibility = View.GONE
                bioLayout.visibility = View.VISIBLE
                if(PrefsHelper.read("isFido","N") == "N") {
                    bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, false)
                    titleView.text = activity.getString(R.string.face_title)
                    checkBoxIv.visibility = View.VISIBLE
                    retryIv.visibility = View.GONE
                } else {
                    bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, true)
                    titleView.text = activity.getString(R.string.face_title_retry)
                    checkBoxIv.visibility = View.GONE
                    retryIv.visibility = View.VISIBLE
                }

                bioLayout.setOnClickListener {
                    if(titleView.text !=  activity.getString(R.string.face_title)) {
                        authenticateUsingBiometrics(PassCodeState.LOGIN, activity = activity, context = activity, bioLayout = bioLayout, bioChkIv = checkBoxIv, titleView = titleView, retryIv = retryIv)
                    }

                    if(!it.isSelected) {
                        it.isSelected = true
                        bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, true)
                    } else {
                        it.isSelected = false
                        bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, false)
                    }
                }
            }

            PassCodeState.CHANGE -> {
                backIvLayout.visibility = View.VISIBLE
                backIvLayout.onThrottleClick {
                    activity.finish()
                }
                bioLayout.visibility = View.GONE
            }

            PassCodeState.PURCHASE -> {
                backIvLayout.visibility = View.VISIBLE
                backIvLayout.onThrottleClick {
                    activity.finish()
                }
                backIvLayout.visibility = View.GONE
                bioLayout.visibility = View.VISIBLE
                if(PrefsHelper.read("isFido","N") == "N") {
                    bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, false)
                    titleView.text = activity.getString(R.string.face_title)
                    checkBoxIv.visibility = View.VISIBLE
                    retryIv.visibility = View.GONE
                } else {
                    bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, true)
                    titleView.text = activity.getString(R.string.face_title_retry)
                    checkBoxIv.visibility = View.GONE
                    retryIv.visibility = View.VISIBLE
                }

                bioLayout.setOnClickListener {
                    if(titleView.text !=  activity.getString(R.string.face_title)) {
                        authenticateUsingBiometrics(PassCodeState.PURCHASE, activity = activity, context = activity, bioLayout = bioLayout, bioChkIv = checkBoxIv, titleView = titleView, retryIv = retryIv)
                    }

                    if(!it.isSelected) {
                        it.isSelected = true
                        bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, true)
                    } else {
                        it.isSelected = false
                        bioChkSelector(activity = activity, bioChkIv = checkBoxIv,titleView = titleView,retryIv = retryIv, false)
                    }
                }
            }

            PassCodeState.CLEAR -> {
                backIvLayout.visibility = View.VISIBLE
                backIvLayout.onThrottleClick {
                    activity.finish()
                }
                bioLayout.visibility = View.GONE
            }
        }
    }


    // 생체인증 결과 Callback
    fun authenticateUsingBiometrics(status: PassCodeState,activity: FragmentActivity,context: Context, bioLayout: ConstraintLayout, bioChkIv: AppCompatImageView, titleView: AppCompatTextView, retryIv: AppCompatImageView) {
        biometricAuthenticationManager.authenticate(activity, object : BiometricAuthenticationManager.BiometricAuthenticationCallback {
            override fun onAuthenticationSuccessful() {
                viewModelScope.launch {
                    _bioIsLoginState.value = status.title
                }
            }

            override fun onAuthenticationFailed(errorCode: Int, errorMessage: String) {
                when(errorCode) {
                    10 -> {} // 사용자가 SystemBackKey를 눌러 작업을 취소함
                    11 -> { showEnrollBiometricsDialog(activity, bioChkIv, titleView, retryIv, context) } // 기기에 등록된 생체인증이 없습니다.
                    12 -> { bioLayout.visibility = View.GONE } // 기기에서 생체 인증을 지원 하지 않습니다.
                    13 -> {} // 생체인증 SystemModal 에서 취소를 누름
                    else -> {
                        showEnrollBiometricsDialog(activity, bioChkIv, titleView, retryIv, context)
                    }
                }
            }
        })
    }


    // 입력 또는 지웠을때 비밀번호 동그라미 색 변화
    fun markChange(
        passWord: String,
        markIv1: AppCompatImageView,
        markIv2: AppCompatImageView,
        markIv3: AppCompatImageView,
        markIv4: AppCompatImageView,
        markIv5: AppCompatImageView,
        markIv6: AppCompatImageView
    ): Boolean {
        return when (passWord.length) {
            0 -> {
                markIv1.isActivated = false
                markIv2.isActivated = false
                markIv3.isActivated = false
                markIv4.isActivated = false
                markIv5.isActivated = false
                markIv6.isActivated = false
                false
            }

            1 -> {
                markIv1.isActivated = true
                markIv2.isActivated = false
                markIv3.isActivated = false
                markIv4.isActivated = false
                markIv5.isActivated = false
                markIv6.isActivated = false
                true
            }

            2 -> {
                markIv1.isActivated = true
                markIv2.isActivated = true
                markIv3.isActivated = false
                markIv4.isActivated = false
                markIv5.isActivated = false
                markIv6.isActivated = false
                true
            }

            3 -> {
                markIv1.isActivated = true
                markIv2.isActivated = true
                markIv3.isActivated = true
                markIv4.isActivated = false
                markIv5.isActivated = false
                markIv6.isActivated = false
                true
            }

            4 -> {
                markIv1.isActivated = true
                markIv2.isActivated = true
                markIv3.isActivated = true
                markIv4.isActivated = true
                markIv5.isActivated = false
                markIv6.isActivated = false
                true
            }

            5 -> {
                markIv1.isActivated = true
                markIv2.isActivated = true
                markIv3.isActivated = true
                markIv4.isActivated = true
                markIv5.isActivated = true
                markIv6.isActivated = false
                true
            }

            6 -> {
                markIv1.isActivated = true
                markIv2.isActivated = true
                markIv3.isActivated = true
                markIv4.isActivated = true
                markIv5.isActivated = true
                markIv6.isActivated = true
                true
            }

            else -> {
                markIv1.isActivated = false
                markIv2.isActivated = false
                markIv3.isActivated = false
                markIv4.isActivated = false
                markIv5.isActivated = false
                markIv6.isActivated = false
                false
            }
        }
    }

    // 반복된 숫자 검증
    fun repeatVerifyNumber(
        passWord: String
    ): Boolean {
        return Pattern.compile("(\\w)\\1\\1").matcher(passWord).find()
    }

    // 연속된 숫자 검증
    fun continuousVerifyNumber(
        passWord: String
    ): Boolean {
        return Pattern.compile("/(012)|(123)|(234)|(345)|(456)|(567)|(678)|(789)|(987)|(876)|(765)|(654)|(543)|(432)|(321)|(210)/").matcher(passWord).find()
    }

    // 생년월일 포함 숫자 검증
    fun birthVerifyNumber(
        passWord: String
    ): Boolean {
        return passWord == customBirthDay.substring(2, 4) + customBirthDay.substring(5, 7) + customBirthDay.substring(8, 10)
    }

    // 전화번호 포함 숫자 검증
    fun phoneVerifyNumber(
        passWord: String
    ): Boolean {
        return passWord.contains(cellPhoneNo.substring(3, 7)) || passWord.contains(cellPhoneNo.substring(7, 11))
    }

    // 이름
    fun getName(): String {
        return name
    }

    // 간편 비밀번호 불일치 횟수
    fun incorrectCount(count: String): String {
        return count
    }

    fun localCount() {
        pwdVerifyCnt++
    }

    fun getLocalCount(): Int {
        return pwdVerifyCnt
    }

    // 지문 등록 유도 모달
    @SuppressLint("UseCompatLoadingForDrawables")
    fun showEnrollBiometricsDialog(activity: FragmentActivity, bioChkIv: AppCompatImageView, titleView: AppCompatTextView, retryIv: AppCompatImageView, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("PIECE")
            .setIcon(context.getDrawable(R.drawable.app_icon_re))
            .setMessage("생체 등록이 필요합니다. 생체등록 설정화면으로 이동하시겠습니까?")
            .setPositiveButton("등록") { _, _ ->
                openBiometricSettings(context = context)
            }
            .setNegativeButton("취소") { _, _ ->
                bioChkSelector(activity = activity, bioChkIv = bioChkIv,titleView = titleView,retryIv = retryIv, false)
            }
            .show()
    }

    // 휴대폰 환경 설정 지문 등록으로 이동
    private fun openBiometricSettings(context: Context) {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        context.startActivity(intent)
    }

    // 생체인증 체크박스 및 텍스트 , 컬러 Change
    fun bioChkSelector(activity: FragmentActivity, bioChkIv: AppCompatImageView, titleView: AppCompatTextView ,retryIv: AppCompatImageView, chk: Boolean): Boolean {
        when(titleView.text) {
            activity.getString(R.string.face_title_retry) -> {}
            else -> {
                when(chk) {
                    false -> {
                        bioChkIv.isSelected = false
                        Glide.with(activity).load(R.drawable.ic_x16_check).into(bioChkIv)
                        titleView.setTextColor(activity.getColor(R.color.c_b8bcc8))
                    }
                    true -> {
                        bioChkIv.isSelected = true
                        Glide.with(activity).load(R.drawable.ic_x16_check_10cf).into(bioChkIv)
                        titleView.setTextColor(activity.getColor(R.color.c_10cfc9))
                    }
                }
            }
        }

        return chk
    }

    // 다른기기 로그인 체크
    fun memberDeviceChk() {
        viewModelScope.launch {
            if (isLogin.isNotEmpty() && memberId.isNotEmpty()) {
                memberDeviceCheckUseCase.invoke(
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
    }

    // 실명인증 여부 조회
    fun ssnCheck() {
        viewModelScope.launch {
            getSsnYnUseCase.invoke(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _ssnChk.value = SsnState.Loading(true)
            }.catch { exception ->
                _ssnChk.value = SsnState.Loading(false)
                _ssnChk.value = SsnState.Failure(exception.message.toString())
            }.collect {
                _ssnChk.value = SsnState.Loading(false)
                _ssnChk.value = SsnState.Success(it)
            }
        }
    }

    // 회원가입
    fun joinPost(passWord: String) {
        viewModelScope.launch {
            val deviceInfo = DeviceInfo(
                deviceId = deviceId,
                "MDO0101",
                "",
                PrefsHelper.read("fcmToken", ""),
                PrefsHelper.read("fcmToken", ""),
            )

            val notificationInfo = NotificationInfo(
                "Y",
                "Y",
                "Y",
                "Y"
            )

            val vo = JoinBodyVo(
                name = savedStateHandle.get<Any>("name").toString(),
                pinNumber = passWord,
                cellPhoneNo = savedStateHandle.get<Any>("cellPhoneNo").toString(),
                birthDay = customBirthDay,
                ci = savedStateHandle.get<Any>("ci").toString(),
                di = savedStateHandle.get<Any>("di").toString(),
                gender = savedStateHandle.get<Any>("gender").toString(),
                isFido = savedStateHandle.get<Any>("isFido").toString(),
                publicKey = savedStateHandle.get<Any>("publicKey").toString(),
                ssn = savedStateHandle.get<Any>("ssn").toString(),
                deviceInfo = deviceInfo,
                notificationInfo = notificationInfo,
                consents = consents!!
            )

            joinPostUseCase(vo)
                .onStart {
                    _joinResponse.value = JoinState.Loading(true)
                }
                .catch { exception ->
                    _joinResponse.value = JoinState.Loading(false)
                    _joinResponse.value = JoinState.Failure(exception.message.toString())
                }
                .collect {
                    _joinResponse.value = JoinState.Loading(false)
                    _joinResponse.value = JoinState.Success(it)
                }
        }
    }

    // accessToken 검증
    fun getAccessTokenChk(grantType: String) {
        viewModelScope.launch {
            getAccessTokenUseCase.invoke(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                grantType = grantType,
                memberId = memberId
            )
                .onStart {
                    _tokenChk.value = AccessTokenState.Loading(true)
                }
                .catch { exception ->
                    _tokenChk.value = AccessTokenState.Loading(false)
                    _tokenChk.value = AccessTokenState.Failure(exception.message.toString())
                }
                .collect {
                    _tokenChk.value = AccessTokenState.Loading(false)
                    _tokenChk.value = AccessTokenState.Success(it)
                }

        }
    }

    // 핀번호 변경
    fun putAuthPin(memberPinModel: MemberPinModel) {
        viewModelScope.launch {
            putAuthPinUseCase.invoke(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                memberPinModel = memberPinModel
            )
                .onStart {
                    _pinUpdateChk.value = AuthPinPutState.Loading(true)
                }
                .catch { exception ->
                    _pinUpdateChk.value = AuthPinPutState.Loading(false)
                    _pinUpdateChk.value = AuthPinPutState.Failure(exception.message.toString())
                }
                .collect {
                    _pinUpdateChk.value = AuthPinPutState.Loading(false)
                    _pinUpdateChk.value = AuthPinPutState.Success(it)
                }

        }
    }

    // 핀번호 검증
    fun getAuthPin(pinNumber: String) {
        viewModelScope.launch {
            try {
                getAuthPinUseCase.authPinVerify(
                    accessToken = "Bearer $accessToken",
                    deviceId = deviceId,
                    memberId = memberId,
                    pinNumber = pinNumber
                ).onStart {
                    _pinChk.value = AuthPinState.Loading(true)
                }.collect {
                    _pinChk.value = AuthPinState.Loading(false)
                    _pinChk.value = AuthPinState.Success(it)
                }
            } catch (exception: Exception) {
                _pinChk.value = AuthPinState.Loading(false)

                // Check for HTTP status code 400
                if (exception is HttpException && exception.code() == 400) {
                    // Handle the 400 Bad Request case
                    try {
                        _pinChk.value = AuthPinState.Loading(false)
                        val errorBody = exception.response()?.errorBody()?.string()
                        val authPinException = parseAuthPinException(errorBody)
                        _pinChk.value = AuthPinState.AuthPinException(authPinException!!.authPinVo, authPinException.message)
                    } catch (e: Exception) {
                        _pinChk.value = AuthPinState.Loading(false)
                    }
                } else {
                    _pinChk.value = AuthPinState.Loading(false)
                }
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

    // 청약 신청
    suspend fun buyPurchase() {
        viewModelScope.launch {
            LogUtil.v("agreeTime : ${savedStateHandle.get<String>("agreeTime")}")
            val purchaseModel = PurchaseModel(
                savedStateHandle.get<String>("portfolioId"),
                savedStateHandle.get<Int>("purchaseInputVolume"),
                savedStateHandle.get<String>("agreeTime")
            )

            val purchaseResponse = api.newPurchaseOffer(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                purchaseModel = purchaseModel
            )

            if(purchaseResponse.isSuccessful && purchaseResponse.body() != null) {
                _purchaseChk.onStart {
                    _purchaseChk.value = PurchaseState.Loading(true)
                }.collect {
                    LogUtil.d("Response Code : ${purchaseResponse.code()}")
                    LogUtil.d("Response isSuccessful : ${purchaseResponse.isSuccessful}")

                    _purchaseChk.value = PurchaseState.Loading(false)
                    _purchaseChk.value = PurchaseState.Success(purchaseResponse.code(), purchaseResponse.body())
                }
            } else if(purchaseResponse.errorBody() != null) {
                val errorBody = purchaseResponse.errorBody()?.string()
                val purchaseException = parsePurchaseException(errorBody)

                _purchaseChk.value = PurchaseState.Loading(false)
                _purchaseChk.value = PurchaseState.HttpFailure(purchaseResponse.code(), purchaseException?.baseVo, purchaseException?.message)

            } else {
                if(purchaseResponse.code() == 202) {
                    _purchaseChk.value = PurchaseState.Loading(false)
                    _purchaseChk.value = PurchaseState.Success(purchaseResponse.code(), purchaseResponse.body())
                } else {
                    val errorBody = purchaseResponse.errorBody()?.string()
                    val purchaseException = parseBaseException(errorBody)

                    _purchaseChk.value = PurchaseState.Loading(false)
                    _purchaseChk.value = PurchaseState.BaseException(purchaseException?.baseVo, purchaseException?.message)
                }
            }
        }
    }

    private fun parseBaseException(errorBody: String?): PurchaseState.BaseException? {
        // gson 객체 생성
        val gson = Gson()

        // errorBody 를 gson 으로 변경
        val errorResponse = gson.fromJson(errorBody, BaseVo::class.java)

        val status = errorResponse.status
        val statusCode = errorResponse.statusCode
        val message = errorResponse.message // 여기가 null임
        val subMessage = errorResponse.subMessage
        val data = null

        // constructor
        val baseVo = BaseVo(status, statusCode, message, subMessage, data)


        return errorResponse.message?.let { PurchaseState.BaseException(baseVo, it) }
    }

    // Function to parse the error response body to AuthPinException
    private fun parseAuthPinException(errorBody: String?): AuthPinState.AuthPinException? {
        if (errorBody.isNullOrEmpty()) {
            return null
        }

        try {
            // gson 객체 생성
            val gson = Gson()

            // errorBody 를 gson 으로 변경
            val errorResponse = gson.fromJson(errorBody, AuthPinErrorVo::class.java)

            val memberId = errorResponse.data.memberId
            val pinNumber = errorResponse.data.pinNumber
            val passwordUpdatedAt = errorResponse.data.passwordUpdatedAt
            val passwordIncorrectCount = errorResponse.data.passwordIncorrectCount
            val isExistMember = errorResponse.data.isExistMember

            // constructor
            val authPinVo = AuthPinVo(memberId, pinNumber, passwordUpdatedAt, passwordIncorrectCount, isExistMember)


            return AuthPinState.AuthPinException(authPinVo, errorResponse.message)
        } catch (e: Exception) {
            return null
        }
    }

//    // Function to parse the error response body to PurchaseException
    private fun parsePurchaseException(errorBody: String?): PurchaseState.PurchaseException? {
        if (errorBody.isNullOrEmpty()) {
            return null
        }

        try {
            // gson 객체 생성
            val gson = Gson()

            // errorBody 를 gson 으로 변경
            val errorResponse = gson.fromJson(errorBody, PurchaseErrorItemVo::class.java)

            val responseCode = errorResponse.responseCode
            val message = errorResponse.message
            val subMessage = errorResponse.subMessage
            val ptWithDrawDate = savedStateHandle.get<String>("ptWithDrawDate")

            // constructor
            val purchaseErrorVo = PurchaseErrorItemVo(responseCode, message, subMessage , ptWithDrawDate)

            return PurchaseState.PurchaseException(purchaseErrorVo, errorResponse.message)
        } catch (e: Exception) {
            return null
        }
    }


    // 회원가입
    sealed class JoinState {
        object Init : JoinState()
        data class Loading(val isLoading: Boolean) : JoinState()
        data class Success(val isSuccess: JoinVo) : JoinState()
        data class Failure(val message: String) : JoinState()
    }

    // AccessToken 검증
    sealed class AccessTokenState {
        object Init : AccessTokenState()
        data class Loading(val isLoading: Boolean) : AccessTokenState()
        data class Success(val isSuccess: BaseVo?) : AccessTokenState()
        data class Failure(val message: String) : AccessTokenState()
    }

    // 핀번호 검증
    sealed class AuthPinState {
        object Init : AuthPinState()
        data class Loading(val isLoading: Boolean) : AuthPinState()
        data class Success(val data: AuthPinVo) : AuthPinState()
        data class Failure(val authPinVo: AuthPinVo?, val errorMessage: String) : AuthPinState()
        data class AuthPinException(val authPinVo: AuthPinVo, val message: String) : AuthPinState()
    }

    // 핀번호 변경
    sealed class AuthPinPutState {
        object Init : AuthPinPutState()
        data class Loading(val isLoading: Boolean) : AuthPinPutState()
        data class Success(val data: BaseVo?) : AuthPinPutState()
        data class Failure(val message: String) : AuthPinPutState()
    }

    // 회원 정보 조회
    sealed class MemberInfoState {
        object Init: MemberInfoState()
        data class Loading(val isLoading: Boolean): MemberInfoState()
        data class Success(val memberVo: MemberVo): MemberInfoState()
        data class Failure(val message: String): MemberInfoState()
    }


    // 실명인증 여부 조회
    sealed class SsnState {
        object Init : SsnState()
        data class Loading(val isLoading: Boolean) : SsnState()
        data class Success(val isSuccess: SsnVo) : SsnState()
        data class Failure(val message: String) : SsnState()
    }

    // 청약 신청
    sealed class PurchaseState {
        object Init : PurchaseState()
        data class Loading(val isLoading: Boolean) : PurchaseState()
        data class Success(val responseCode: Int, val isSuccess: PurchaseDefaultVo?) : PurchaseState()

        data class BaseException(val baseVo: BaseVo?, val message: String?) : PurchaseState()
        data class BaseFailure(val baseVo: PurchaseErrorItemVo?, val errorMessage: String?) : PurchaseState()

        data class HttpFailure(val responseCode:Int, val baseVo: PurchaseErrorItemVo?, val errorMessage: String?) : PurchaseState()
        data class PurchaseException(val baseVo: PurchaseErrorItemVo, val message: String?) : PurchaseState()
    }

}

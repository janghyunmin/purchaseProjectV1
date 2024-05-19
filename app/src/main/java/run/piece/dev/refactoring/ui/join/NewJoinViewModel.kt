package run.piece.dev.refactoring.ui.join

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.HttpException
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.consent.usecase.ConsentListGetUseCase
import run.piece.domain.refactoring.consent.usecase.ConsentWebLinkUseCase
import run.piece.domain.refactoring.member.model.Consents
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.PostSmsAuthVo
import run.piece.domain.refactoring.member.model.PostSmsVerificationVo
import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.domain.refactoring.member.model.request.PostSmsVerificationModel
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.PostReSmsAuthUseCase
import run.piece.domain.refactoring.member.usecase.PostSmsAuthUseCase
import run.piece.domain.refactoring.member.usecase.PostSmsVerificationUseCase
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class NewJoinViewModel @Inject constructor(
    private val consentListGetUseCase: ConsentListGetUseCase,
    private val postSmsAuthUseCase: PostSmsAuthUseCase,
    private val postReSmsAuthUseCase: PostReSmsAuthUseCase,
    private val postSmsVerificationUseCase: PostSmsVerificationUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase, // 회원 정보 조회
    private val consentWebLinkUseCase: ConsentWebLinkUseCase
) : ViewModel() {
    private val _userName = MutableLiveData("")
    val userName: LiveData<String> get() = _userName

    private val _userBirth = MutableLiveData("")
    val userBirth: LiveData<String> get() = _userBirth

    private val _userPhone = MutableLiveData("")
    val userPhone: LiveData<String> get() = _userPhone

    private val _consentList: MutableStateFlow<ConsentState> = MutableStateFlow(ConsentState.Init)
    val consentList: StateFlow<ConsentState> = _consentList.asStateFlow()

    private val _postSmsAuth: MutableStateFlow<PostSmsAuthState> = MutableStateFlow(PostSmsAuthState.Init)
    val postSmsAuth: StateFlow<PostSmsAuthState> = _postSmsAuth.asStateFlow()

    private val _postReSmsAuth: MutableStateFlow<PostReSmsAuthState> = MutableStateFlow(PostReSmsAuthState.Init)
    val postReSmsAuth: StateFlow<PostReSmsAuthState> = _postReSmsAuth.asStateFlow()

    private val _postSmsVerification: MutableStateFlow<PostSmsVerificationState> = MutableStateFlow(PostSmsVerificationState.Init)
    val postSmsVerification: StateFlow<PostSmsVerificationState> = _postSmsVerification.asStateFlow()

    var sendConsentItemList: ArrayList<Consents> = ArrayList()

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()

    private val accessToken: String = PrefsHelper.read("accessToken", "")
    private val deviceId: String = PrefsHelper.read("deviceId", "")
    private val memberId: String = PrefsHelper.read("memberId", "")

    var telComCd = ""
    var txSeqNo = ""
    var userGender: String = ""

    fun onInputName(input: CharSequence) {
        if (input.isNotEmpty()) _userName.value = input.toString()
    }

    fun onInputBirth(input: CharSequence) {
        if (input.isNotEmpty()) _userBirth.value = input.toString()
    }

    fun onInputPhone(input: CharSequence) {
        if (input.isNotEmpty()) _userPhone.value = input.toString()
    }

    fun focusText(context: Context, editText: AppCompatEditText, target: AppCompatTextView) {
        editText.setOnFocusChangeListener { view, focus ->
            if (focus) {
                target.setTextColor(ContextCompat.getColor(context, R.color.black))
            } else {
                target.setTextColor(ContextCompat.getColor(context, R.color.c_b8bcc8))
            }
        }
    }

    // 입력한 생년월일 유효성 검사
    fun onInputBirthUI(context: Context, input: String, targetView: View, changeView: View) {
        if (!birthValid(input)) {
            targetView.visibility = View.VISIBLE
            changeView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.c_FF7878))
        } else {
            targetView.visibility = View.GONE
            changeView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.c_EAECF0))
        }
    }

    // 입력한 휴대폰 번호 유효성 검사
    fun onInputPhoneUI(context: Context, input: String, targetView: AppCompatTextView, changeView: View) {
        if (!phoneValid(input)) {
            targetView.visibility = View.VISIBLE
            changeView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.c_FF7878))
        } else {
            targetView.visibility = View.GONE
            changeView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.c_EAECF0))
        }
    }

    // 입력한 이름 유효성 검사
    /*fun onInputNameUI(context: Context, input: String, targetView: AppCompatTextView, changeView: View) {
        if (!phoneValid(input)) {
            targetView.visibility = View.VISIBLE
            changeView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.c_FF7878))
        } else {
            targetView.visibility = View.GONE
            changeView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.c_EAECF0))
        }
    }*/

    // 생년월일 유효성 검사
    fun birthValid(birth: CharSequence): Boolean {
        return Pattern.matches("^(19[0-9][0-9]|20\\d{2})?([-/.])?(0[0-9]|1[0-2])?([-/.])?(0[1-9]|[1-2][0-9]|3[0-1])$", birth)
    }

    // 휴대번 번호 유효성 검사
    fun phoneValid(phone: String): Boolean {
        return Pattern.matches("^\\d{3}\\d{4}\\d{4}$", phone)
    }

    //이름 유효성 검사
    /*fun nameValid(name: String): Boolean {
        return Pattern.matches("^[a-zA-Z가-힣]*\$", name)
    }*/

    // 남자 / 여자 Click Event
    fun genderSelect(status: String): String {
        return if (status == "남자") {
            "M"
        } else {
            "F"
        }
    }

    fun getConsentList() {
        viewModelScope.launch {
            consentListGetUseCase("SIGN", "v0.0.2")
                .onStart {
                    _consentList.value = ConsentState.Loading(true)
                }
                .catch { exception ->
                    _consentList.value = ConsentState.Loading(false)
                    _consentList.value = ConsentState.Failure(exception.message.toString())
                }.collect {
                    _consentList.value = ConsentState.Loading(false)
                    _consentList.value = ConsentState.Success(it)
                }
        }
    }


    fun postSmsAuth(model: PostSmsAuthModel) {
        viewModelScope.launch {
            try {
                postSmsAuthUseCase(model)
                    .onStart {
                        _postSmsAuth.value = PostSmsAuthState.Loading(true)
                    }.collect {
                        _postSmsAuth.value = PostSmsAuthState.Loading(false)
                        _postSmsAuth.value = PostSmsAuthState.Success(it)

                        txSeqNo = it.txSeqNo
                    }
            } catch (exception: Exception) {
                _postSmsAuth.value = PostSmsAuthState.Loading(true)
                if(exception is HttpException && exception.code() == 400) {
                    _postSmsAuth.value = PostSmsAuthState.Loading(false)
                    _postSmsAuth.value = PostSmsAuthState.Failure(exception.message.toString())
                }
                // 만 14세 이상만 가입 할 수 있어요
                else if(exception is HttpException && exception.code() == 425) {
                    val errorBody = exception.response()?.errorBody()?.string()
                    val ageException = parseBaseException(errorBody)

                    _postSmsAuth.value = PostSmsAuthState.Loading(false)
                    _postSmsAuth.value = PostSmsAuthState.BaseException(baseVo = ageException?.baseVo, ageException?.message)
                }
                // Response code 500
                else {
                    _postSmsAuth.value = PostSmsAuthState.Loading(false)
                    _postSmsAuth.value = PostSmsAuthState.Failure(exception.message.toString())
                }
            }
        }
    }

    fun postReSmsAuth(model: PostSmsAuthModel) {
        viewModelScope.launch {
            postReSmsAuthUseCase(model)
                .onStart {
                    _postReSmsAuth.value = PostReSmsAuthState.Loading(true)
                }
                .catch { exception ->
                    _postReSmsAuth.value = PostReSmsAuthState.Loading(false)
                    _postReSmsAuth.value = PostReSmsAuthState.Failure(exception.message.toString())
                }
                .collect {
                    _postReSmsAuth.value = PostReSmsAuthState.Loading(false)
                    _postReSmsAuth.value = PostReSmsAuthState.Success(it)

                    txSeqNo = it.txSeqNo
                }
        }
    }

    fun postSmsVerification(model: PostSmsVerificationModel) {
        viewModelScope.launch {
            try {
                postSmsVerificationUseCase(model)
                    .onStart {
                        _postSmsVerification.value = PostSmsVerificationState.Loading(true)
                    }
                    .collect {
                        _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                        _postSmsVerification.value = PostSmsVerificationState.Success(it)
                    }
            } catch (exception: Exception) {
                exception.printStackTrace()
                _postSmsVerification.value = PostSmsVerificationState.Loading(true)

                // Response Code 202
                if (exception is HttpException && exception.code() == 202) {
                    Log.v("Exception Code (${exception.code()})", "| Exception Message : (${exception.message()})")
                    try {
                        val errorBody = exception.response()?.errorBody()?.string()
                        val smsVerifyException = parseBaseException(errorBody = errorBody)
                        _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                        _postSmsVerification.value = PostSmsVerificationState.BaseException(smsVerifyException?.baseVo, smsVerifyException?.message)

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                        Log.e("Error Code 202 Parsing Error Body : ", "${ex.message}")
                    }
                }

                // Response Code 400 ( 회원가입 후 탈퇴한 회원 )
                else if (exception is HttpException && exception.code() == 400) {
                    Log.v("Exception Code (${exception.code()})", "| Exception Message : (${exception.message()})")
                    try {
                        val errorBody = exception.response()?.errorBody()?.string()
                        val smsVerifyException = parseBaseException(errorBody = errorBody)

                        _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                        _postSmsVerification.value = PostSmsVerificationState.Failure(exception.message())

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                        Log.e("Error Code 400 Parsing Error Body : ", " ${ex.message}")
                    }
                }

                // Response Code 500 ( 탈퇴한 회원이 아닌 모든 에러 케이스 )
                else if (exception is HttpException && exception.code() == 500) {
                    Log.v("Exception Code (${exception.code()})", "| Exception Message : (${exception.message()})")

                    val errorBody = exception.response()?.errorBody()?.string()
                    val smsVerifyException = parseBaseException(errorBody = errorBody)

                    _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                    _postSmsVerification.value = PostSmsVerificationState.BaseException(smsVerifyException?.baseVo, smsVerifyException?.message)

                } else {
                    Log.e("Error : ", "${exception.message}")
                    exception.printStackTrace()
                    _postSmsVerification.value = PostSmsVerificationState.Loading(false)
                }
            }

        }
    }


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

    fun getConsentWebLink(): String = "${consentWebLinkUseCase()}terms?tab="


    // HttpCode Handle
    // 200 , 202 , 400 , 500

    // Default Exception Body
    private fun parseBaseException(errorBody: String?): PostSmsVerificationState.BaseException? {
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


        return errorResponse.message?.let { PostSmsVerificationState.BaseException(baseVo, it) }
    }

    sealed class PostReSmsAuthState {
        object Init : PostReSmsAuthState()
        data class Loading(val isLoading: Boolean) : PostReSmsAuthState()
        data class Success(val postSmsAuthVo: PostSmsAuthVo) : PostReSmsAuthState()
        data class Failure(val message: String) : PostReSmsAuthState()
    }

    sealed class PostSmsAuthState {
        object Init : PostSmsAuthState()
        data class Loading(val isLoading: Boolean) : PostSmsAuthState()
        data class Success(val postSmsAuthVo: PostSmsAuthVo) : PostSmsAuthState()
        data class Failure(val message: String) : PostSmsAuthState()
        data class BaseException(val baseVo: BaseVo?, val message: String?) : PostSmsAuthState()
    }


    sealed class ConsentState {
        object Init : ConsentState()
        data class Loading(val isLoading: Boolean) : ConsentState()
        data class Success(val consentList: List<ConsentVo>) : ConsentState()
        data class Failure(val message: String) : ConsentState()
    }

    sealed class PostSmsVerificationState {
        object Init : PostSmsVerificationState()
        data class Loading(val isLoading: Boolean) : PostSmsVerificationState()
        data class Success(val postSmsVerificationVo: PostSmsVerificationVo) : PostSmsVerificationState()
        data class Failure(val message: String) : PostSmsVerificationState()
        data class BaseException(val baseVo: BaseVo?, val message: String?) : PostSmsVerificationState()
    }


    // 회원 정보 조회
    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }

}
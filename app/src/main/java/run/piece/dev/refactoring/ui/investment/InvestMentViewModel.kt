package run.piece.dev.refactoring.ui.investment

import android.content.res.Resources
import android.util.TypedValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.investment.model.InvestMentErrorVo
import run.piece.domain.refactoring.investment.model.InvestMentVo
import run.piece.domain.refactoring.investment.model.InvestmentAnswerVo
import run.piece.domain.refactoring.investment.model.InvestmentQuestionVo
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel
import run.piece.domain.refactoring.investment.usecase.GetInvestMentUseCase
import run.piece.domain.refactoring.investment.usecase.PostInvestMentUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.SsnVo
import run.piece.domain.refactoring.member.usecase.GetSsnYnUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import javax.inject.Inject


@HiltViewModel
class InvestMentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val postInvestMentUseCase: PostInvestMentUseCase,
    private val getSsnYnUseCase: GetSsnYnUseCase, // 실명인증 여부 조회 UseCase
) : ViewModel() {
    private val accessToken: String = PrefsHelper.read("accessToken", "")
    private val deviceId: String = PrefsHelper.read("deviceId", "")
    private val memberId: String = PrefsHelper.read("memberId", "")

    private val _investMentResult: MutableStateFlow<InvestMentPostState> = MutableStateFlow(InvestMentPostState.Init)
    val investMentResult: StateFlow<InvestMentPostState> get() = _investMentResult.asStateFlow()

    private val _ssnChk: MutableStateFlow<SsnState> = MutableStateFlow(SsnState.Init)
    val ssnChk: StateFlow<SsnState> = _ssnChk.asStateFlow()

    // 툴팁 영역 position
    fun dpToPixels(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }


    // 투자 성향 분석 요청
    suspend fun postInvestMent(model: InvestBodyModel) {
        viewModelScope.launch {
            try {
                postInvestMentUseCase.invoke(
                    accessToken = "Bearer ${savedStateHandle.get<String>("accessToken") ?: PrefsHelper.read("accessToken", "")}",
                    deviceId = savedStateHandle.get<String>("deviceId") ?: PrefsHelper.read("deviceId", ""),
                    memberId = savedStateHandle.get<String>("memberId") ?: PrefsHelper.read("memberId", ""),
                    investBodyModel = model
                ).onStart {
                    _investMentResult.value = InvestMentPostState.Loading(true)
                }.collect {
                    _investMentResult.value = InvestMentPostState.Loading(false)
                    _investMentResult.value = InvestMentPostState.Success(it)
                }
            } catch (exception: Exception) {
                _investMentResult.value = InvestMentPostState.Loading(true)
                if (exception is HttpException && exception.code() == 400) {
                    try {
                        val errorBody = exception.response()?.errorBody()?.string()
                        val investMentException = parseInvestMent(errorBody = errorBody)

                        _investMentResult.value = InvestMentPostState.Loading(false)
                        _investMentResult.value = InvestMentPostState.Failure(investMentException?.errorVo, investMentException?.message)

                    } catch (e: Exception) {
                        _investMentResult.value = InvestMentPostState.Loading(false)
                        e.printStackTrace()
                    }
                } else if (exception is HttpException && exception.code() == 500) {
                    val errorBody = exception.response()?.errorBody()?.string()
                    val baseException = parseBaseException(errorBody = errorBody)

                    _investMentResult.value = InvestMentPostState.Loading(false)
                    _investMentResult.value = InvestMentPostState.BaseException(baseException?.baseVo, baseException?.message)
                } else {
                    _investMentResult.value = InvestMentPostState.Loading(false)
                }
            }

        }
    }

    // 메인화면으로 화면 전환시 ssnYn 데이터를 넘겨야해서 사용
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
}

private fun parseBaseException(errorBody: String?): InvestMentPostState.BaseException? {
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


    return errorResponse.message?.let { InvestMentPostState.BaseException(baseVo, it) }
}

private fun parseInvestMent(errorBody: String?): InvestMentPostState.InvestException? {
    if (errorBody.isNullOrEmpty()) {
        return null
    }
    return try {
        val gson = Gson()
        val errorResponse = gson.fromJson(errorBody, InvestMentErrorVo::class.java)
        val responseCode = errorResponse.responseCode
        val message = errorResponse.message
        val investMentErrorVo = InvestMentErrorVo(responseCode, message)

        InvestMentPostState.InvestException(investMentErrorVo, errorResponse.message)
    } catch (e: Exception) {
        null
    }
}

// 투자 성향 분석 요청 State
sealed class InvestMentPostState {
    object Init : InvestMentPostState()
    data class Loading(val isLoading: Boolean) : InvestMentPostState()
    data class Success(val investMentVo: InvestMentVo) : InvestMentPostState()
    data class Failure(val errorVo: InvestMentErrorVo?, val message: String?) : InvestMentPostState()
    data class BaseException(val baseVo: BaseVo?, val message: String?) : InvestMentPostState()
    data class InvestException(val errorVo: InvestMentErrorVo, val message: String?) : InvestMentPostState()
}

// 투자 성향 분석 질문,답변 조회 State
sealed class InvestMentGetState {
    object Init : InvestMentGetState()
    data class Loading(val isLoading: Boolean) : InvestMentGetState()
    data class Success(val investmentQuestionVo: List<InvestmentQuestionVo>) : InvestMentGetState()
    data class Failure(val message: String) : InvestMentGetState()
}

// 실명인증 여부 조회
sealed class SsnState {
    object Init : SsnState()
    data class Loading(val isLoading: Boolean) : SsnState()
    data class Success(val isSuccess: SsnVo) : SsnState()
    data class Failure(val message: String) : SsnState()
}
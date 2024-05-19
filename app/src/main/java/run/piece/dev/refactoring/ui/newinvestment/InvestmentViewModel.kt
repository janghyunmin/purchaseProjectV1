package run.piece.dev.refactoring.ui.newinvestment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.tools.build.jetifier.core.utils.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.domain.refactoring.investment.model.InvestMentVo
import run.piece.domain.refactoring.investment.model.InvestmentQuestionVo
import run.piece.domain.refactoring.investment.usecase.GetInvestMentUseCase
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider,
    private val investmentGetUseCase: GetInvestMentUseCase
) : ViewModel() {

    private val _investmentGetList: MutableStateFlow<InvestmentGetState> = MutableStateFlow(InvestmentGetState.Init)
    private val _investmentPost: MutableStateFlow<InvestMentPostState> = MutableStateFlow(InvestMentPostState.Init)

    /**
     * case nomal 취약투자자인 경우
     * case survey 취약투자자가 아닌 경우
     **/
    val startType: String = savedStateHandle.get<String>("type")?: "nomal"
    val investMentGetList: StateFlow<InvestmentGetState> get() = _investmentGetList.asStateFlow()
    val investmentPost: StateFlow<InvestMentPostState> get() = _investmentPost.asStateFlow()

    var questionList: List<InvestmentQuestionVo> = ArrayList()

    var requestIndex = 0
    var progressCount = 0
    var totalSize = 0
    var isMultiple = ""

    fun getInvestmentList() {
        viewModelScope.launch {
            investmentGetUseCase()
                .onStart {
                    _investmentGetList.value = InvestmentGetState.Loading(true)
                }.catch {
                    _investmentGetList.value = InvestmentGetState.Loading(false)
                    _investmentGetList.value = InvestmentGetState.Failure(it.message.toString())
                }.collect {
                    _investmentGetList.value = InvestmentGetState.Loading(false)
                    _investmentGetList.value = InvestmentGetState.Success(it)
                }
        }
    }

    fun getDeviceWidth(): Int = resourcesProvider.getDeviceWidth()
    fun dpToPixel(pixel: Int) = resourcesProvider.dpToPixel(pixel)

    fun getTotalScore(): Int {
        var totalScore = 0
        questionList.forEachIndexed { position, question ->
            val total = if (question.isMultiple == "Y") {
                val tempList = ArrayList<Int>()
                question.answers.forEach { answer ->
                    if (answer.isSelected) tempList.add(answer.score)
                }
                tempList.max()
            } else {
                var tempScore = 0
                question.answers.forEach { answer ->
                    if (answer.isSelected) tempScore = answer.score
                }
                tempScore
            }
            totalScore += total
        }
        return totalScore
    }

    fun getMultiple(position: Int): Boolean {
        return questionList[position].isMultiple == "Y"

    }

    // 투자 성향 분석 질문,답변 조회 State
    sealed class InvestmentGetState {
        object Init : InvestmentGetState()
        data class Loading(val isLoading: Boolean) : InvestmentGetState()
        data class Success(val investmentQuestionVo: List<InvestmentQuestionVo>) : InvestmentGetState()
        data class Failure(val message: String) : InvestmentGetState()
    }

    sealed class InvestMentPostState {
        object Init : InvestMentPostState()
        data class Loading(val isLoading: Boolean) : InvestMentPostState()
        data class Success(val investMentVo: InvestMentVo) : InvestMentPostState()
        data class Failure(val message: String) : InvestMentPostState()
    }
}
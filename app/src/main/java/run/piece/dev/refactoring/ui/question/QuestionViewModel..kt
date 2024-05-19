package run.piece.dev.refactoring.ui.question

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.common.usecase.CommonFaqTabGetUseCase
import run.piece.domain.refactoring.common.vo.CommonFaqVo
import run.piece.domain.refactoring.question.model.QuestionItemVo
import run.piece.domain.refactoring.question.usecase.QuestionListGetUseCase
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle,
                                            private val resourcesProvider: ResourcesProvider,
                                            private val commonFaqTabGetUseCase: CommonFaqTabGetUseCase,
                                            private val questionListGetUseCase: QuestionListGetUseCase) : ViewModel() {

    private val _questionList: MutableStateFlow<QuestionListState> = MutableStateFlow(QuestionListState.Init)
    val questionList: StateFlow<QuestionListState> get() = _questionList.asStateFlow()

    private val _faqTabList: MutableStateFlow<FaqTabListState> = MutableStateFlow(FaqTabListState.Init)
    val faqTabList: StateFlow<FaqTabListState> get()  = _faqTabList.asStateFlow()
    var tabType = ""


    fun getQuestionList(boardType: String = "BRT03", boardCategory: String, apiVersion: String = "v0.0.2") {
        viewModelScope.launch {
            questionListGetUseCase(boardType, boardCategory, apiVersion)
                .onStart {
                    _questionList.value = QuestionListState.IsLoading(true)
                }
                .catch { exception ->
                    _questionList.value = QuestionListState.IsLoading(false)
                    _questionList.value = QuestionListState.Failure(exception.message.default())
                }
                .collect {
                    _questionList.value = QuestionListState.IsLoading(false)
                    _questionList.value = QuestionListState.Success(it)
                }
        }
    }

    fun getFaqTabList() {
        viewModelScope.launch {
            commonFaqTabGetUseCase()
                .onStart {
                    _faqTabList.value = FaqTabListState.IsLoading(true)
                }
                .catch {exception ->
                    _faqTabList.value = FaqTabListState.IsLoading(false)
                    _faqTabList.value = FaqTabListState.Failure(exception.message.default())
                }
                .collect {
                    _faqTabList.value = FaqTabListState.IsLoading(false)
                    _faqTabList.value = FaqTabListState.Success(it)
                }
        }
    }

    sealed class QuestionListState {
        object Init : QuestionListState()
        data class IsLoading(val isLoading: Boolean) : QuestionListState()
        data class Success(val questionList: PagingData<QuestionItemVo>): QuestionListState()
        data class Failure(val message: String) : QuestionListState()
    }

    sealed class FaqTabListState {
        object Init: FaqTabListState()
        data class IsLoading(val isLoading: Boolean) : FaqTabListState()
        data class Success(val faqTabList: List<CommonFaqVo>) : FaqTabListState()
        data class Failure(val message: String) : FaqTabListState()
    }
}
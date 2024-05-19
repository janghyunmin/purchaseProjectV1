package run.piece.dev.refactoring.ui.faq

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
import run.piece.domain.refactoring.faq.model.FaqItemVo
import run.piece.domain.refactoring.faq.usecase.FaqListGetUseCase
import javax.inject.Inject

@HiltViewModel
class FaqTabViewModel @Inject constructor (private val resourcesProvider: ResourcesProvider,
                                           private val savedStateHandle: SavedStateHandle,
                                           private val faqListGetUseCase: FaqListGetUseCase
): ViewModel() {
    private val _faqList: MutableStateFlow<FaqListState> = MutableStateFlow(FaqListState.Init)
    val faqList: StateFlow<FaqListState> get() = _faqList.asStateFlow()

    fun getFaqList(boardType: String = "BRT06", boardCategory: String, apiVersion: String = "v0.0.2") {
        viewModelScope.launch {
            faqListGetUseCase(boardType, boardCategory, apiVersion)
                .onStart {
                    _faqList.value = FaqListState.IsLoading(true)
                }
                .catch { exception ->
                    _faqList.value = FaqListState.IsLoading(false)
                    _faqList.value = FaqListState.Failure(exception.message.default())
                }
                .collect {
                    _faqList.value = FaqListState.IsLoading(false)
                    _faqList.value = FaqListState.Success(it)
                }
        }
    }

    fun dpToPixel(pixel: Int) = resourcesProvider.dpToPixel(pixel)

    sealed class FaqListState {
        object Init : FaqListState()
        data class IsLoading(val isLoading: Boolean) : FaqListState()
        data class Success(val faqList: PagingData<FaqItemVo>): FaqListState()
        data class Failure(val message: String) : FaqListState()
    }
}
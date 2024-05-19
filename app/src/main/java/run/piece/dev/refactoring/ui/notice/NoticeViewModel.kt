package run.piece.dev.refactoring.ui.notice

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
import run.piece.domain.refactoring.notice.model.NoticeItemVo
import run.piece.domain.refactoring.notice.usecase.NoticeListGetUseCase
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle,
                                          private val resourcesProvider: ResourcesProvider,
                                          private val noticeListGetUseCase: NoticeListGetUseCase) : ViewModel() {
    private val _noticeList: MutableStateFlow<NoticeListState> = MutableStateFlow(NoticeListState.Init)
    val noticeList: StateFlow<NoticeListState> get() = _noticeList.asStateFlow()

    fun getNoticeList() {
        viewModelScope.launch {
            noticeListGetUseCase("BRT02")
                .onStart {
                    _noticeList.value = NoticeListState.IsLoading(true)
                }
                .catch { exception ->
                    _noticeList.value = NoticeListState.IsLoading(false)
                    _noticeList.value = NoticeListState.Failure(exception.message.default())
                }
                .collect {
                    _noticeList.value = NoticeListState.IsLoading(false)
                    _noticeList.value = NoticeListState.Success(it)
                }
        }

    }

    sealed class NoticeListState {
        object Init : NoticeListState()
        data class IsLoading(val isLoading: Boolean) : NoticeListState()
        data class Success(val noticeList: PagingData<NoticeItemVo>): NoticeListState()
        data class Failure(val message: String) : NoticeListState()
    }
}
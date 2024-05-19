package run.piece.dev.refactoring.ui.event

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
import run.piece.domain.refactoring.event.model.EventItemVo
import run.piece.domain.refactoring.event.usecase.EventListGetUseCase
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle,
                                         private val resourcesProvider: ResourcesProvider,
                                         private val eventListGetUseCase: EventListGetUseCase) : ViewModel() {
    private val _eventList: MutableStateFlow<EventListState> = MutableStateFlow(EventListState.Init)
    val eventList: StateFlow<EventListState> get() = _eventList.asStateFlow()

    fun getEventList() {
        viewModelScope.launch {
            eventListGetUseCase()
                .onStart {
                    _eventList.value = EventListState.IsLoading(true)
                }.catch { exception ->
                    _eventList.value = EventListState.IsLoading(false)
                    _eventList.value = EventListState.Failure(exception.message.default())
                }.collect {
                    _eventList.value = EventListState.IsLoading(false)
                    _eventList.value = EventListState.Success(it)
                }
        }
    }

    sealed class EventListState {
        object Init : EventListState()
        data class IsLoading(val isLoading: Boolean) : EventListState()
        data class Success(val eventList: PagingData<EventItemVo>): EventListState()
        data class Failure(val message: String) : EventListState()
    }
}
package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

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
import run.piece.domain.refactoring.board.model.BoardVo
import run.piece.domain.refactoring.board.usecase.BoardListGetUseCase
import javax.inject.Inject

@HiltViewModel
class DisclosureSearchViewModel @Inject constructor(
    private val boardListGetUseCase: BoardListGetUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val isLogin: String = PrefsHelper.read("isLogin", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private val _disclosureList: MutableStateFlow<DisclosureListState> = MutableStateFlow(DisclosureListState.Init)
    val disclosureList: StateFlow<DisclosureListState> get() = _disclosureList.asStateFlow()
    var portfolioId = savedStateHandle.get<String>("portfolioId").toString()

    fun getBoard(viewType: String, portfolioId: String, keyword: String, investmentPage: Int, managementPage: Int) {
        viewModelScope.launch {
            when (viewType) {
                "경영공시" -> {
                    boardListGetUseCase.getDisclosureSearchList(portfolioId, keyword, investmentPage, managementPage)
                        .onStart {
                            _disclosureList.value = DisclosureListState.IsLoading(true)
                        }
                        .catch { exception ->
                            _disclosureList.value = DisclosureListState.IsLoading(false)
                            _disclosureList.value = DisclosureListState.Failure(exception.message.toString())
                        }
                        .collect {
                            _disclosureList.value = DisclosureListState.IsLoading(false)
                            _disclosureList.value = DisclosureListState.Success(it)
                        }
                }

                "투자공시" -> {
                    boardListGetUseCase.getDisclosureSearchList(portfolioId, keyword, investmentPage, managementPage)
                        .onStart {
                            _disclosureList.value = DisclosureListState.IsLoading(true)
                        }
                        .catch { exception ->
                            _disclosureList.value = DisclosureListState.IsLoading(false)
                            _disclosureList.value = DisclosureListState.Failure(exception.message.toString())
                        }
                        .collect {
                            _disclosureList.value = DisclosureListState.IsLoading(false)
                            _disclosureList.value = DisclosureListState.Success(it)
                        }
                }
            }
        }
    }

    // 공시 리스트 State
    sealed class DisclosureListState {
        object Init : DisclosureListState()
        data class IsLoading(val isLoading: Boolean) : DisclosureListState()
        data class Success(val disclosure: BoardVo) : DisclosureListState()
        data class Failure(val message: String) : DisclosureListState()
    }
}
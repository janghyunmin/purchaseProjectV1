package run.piece.dev.refactoring.ui.deposit

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
import run.piece.dev.data.refactoring.ui.deposit.api.NhApi
import run.piece.dev.data.refactoring.ui.deposit.model.NhBankHistoryState
import run.piece.dev.refactoring.utils.LogUtil
import javax.inject.Inject


@HiltViewModel
class NhHistoryViewModel @Inject constructor(private val api: NhApi) : ViewModel() {
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private val _nhHistory: MutableStateFlow<NhBankHistoryState> = MutableStateFlow(NhBankHistoryState.Init)
    val nhHistory: StateFlow<NhBankHistoryState> get() = _nhHistory.asStateFlow()

    fun nhBankHistory() {
        viewModelScope.launch {

            val response = api.postNhHistory("Bearer $accessToken", deviceId, memberId)

            if(response.isSuccessful && response.body() != null) {
                _nhHistory.onStart {
                    _nhHistory.value = NhBankHistoryState.IsLoading(true)
                }.catch {
                    _nhHistory.value = NhBankHistoryState.IsLoading(false)
                    _nhHistory.value = NhBankHistoryState.Failure(response.code().toString())
                }.collect {
                    _nhHistory.value = NhBankHistoryState.IsLoading(false)
                    _nhHistory.value = NhBankHistoryState.Success(response.body())
                }
            } else if(response.errorBody() != null) {
                _nhHistory.value = NhBankHistoryState.IsLoading(false)
                _nhHistory.value = NhBankHistoryState.Failure(response.code().toString())
            } else {
                if(response.code() == 204) {
                    _nhHistory.value = NhBankHistoryState.IsLoading(false)
                    _nhHistory.value = NhBankHistoryState.Empty(response.code().toString())
                } else {
                    _nhHistory.value = NhBankHistoryState.IsLoading(false)
                    _nhHistory.value = NhBankHistoryState.Failure(response.code().toString())
                }
            }
        }
    }
}
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
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deposit.model.WithDrawVo
import run.piece.domain.refactoring.deposit.usecase.NhBankWithDrawUseCase
import javax.inject.Inject

// NH 출금 신청 ViewModel


@HiltViewModel
class NhWithDrawViewModel @Inject constructor(
    private val nhBankWithDrawUseCase: NhBankWithDrawUseCase) :
    ViewModel() {
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    private val _state: MutableStateFlow<WithDrawState> = MutableStateFlow(WithDrawState.Loading)
    val state: StateFlow<WithDrawState> = _state.asStateFlow()

    fun nhWithDraw(tram: String) {
        viewModelScope.launch {
            _state.value = WithDrawState.Loading
            nhBankWithDrawUseCase.withDrawAmountReturn(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                withDrawModel = WithDrawVo(tram = tram)
            )
                .onStart {
                    _state.value = WithDrawState.Loading
                }.catch { exception ->
                    _state.value = WithDrawState.Loading
                    _state.value = WithDrawState.Failure
                }.collect { item ->
                    _state.value = WithDrawState.Loading
                    _state.value = WithDrawState.Success(item)
                }
        }
    }

    sealed class WithDrawState {
        data class Success(val isSuccess: BaseVo) : WithDrawState()
        object Empty : WithDrawState()
        object Failure : WithDrawState()
        object Loading : WithDrawState()

    }
}
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
import run.piece.domain.refactoring.deposit.model.NhBankRegisterVo
import run.piece.domain.refactoring.deposit.usecase.NhChangeAccountUseCase
import run.piece.domain.refactoring.deposit.usecase.NhCreateAccountUseCase
import javax.inject.Inject


@HiltViewModel
class NhAccountViewModel @Inject constructor(
    private val nhCreateAccountUseCase: NhCreateAccountUseCase,
    private val nhChangeAccountUseCase: NhChangeAccountUseCase) : ViewModel() {

    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    fun createNhBankAccount(memberName: String, bankCode: String?, bankAccount: String) {
        viewModelScope.launch {
            _state.value = State.Loading
            nhCreateAccountUseCase.postNhCreateAccount(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                NhBankRegisterVo(memberName, bankCode, bankAccount)
            ).onStart {
                _state.value = State.Loading
            }.catch { exception ->
                _state.value = State.Loading
                _state.value = State.Failure
            }.collect { item ->
                _state.value = State.Loading
                _state.value = State.Success(item)
            }
        }
    }

    fun changeAccount(memberName: String, bankCode: String?, bankAccount: String) {
        viewModelScope.launch {
            _state.value = State.Loading
            nhChangeAccountUseCase.changeNhAccount(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                NhBankRegisterVo(memberName, bankCode, bankAccount)
            ).onStart {
                _state.value = State.Loading
            }.catch { exception ->
                _state.value = State.Loading
                _state.value = State.Failure
            }.collect { item ->
                _state.value = State.Loading
                _state.value = State.Success(item)
            }
        }
    }

    sealed class State {
        data class Success(
            val isSuccess: BaseVo
        ) : State()

        object Empty : State()
        object Failure : State()
        object Loading : State()
    }
}
package run.piece.dev.refactoring.ui.alarm

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
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.alarm.model.AlarmItemVo
import run.piece.domain.refactoring.alarm.usecase.AlarmListGetUseCase
import run.piece.domain.refactoring.alarm.usecase.AlarmPutUseCase
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmPutUseCase: AlarmPutUseCase,
    private val alarmListGetUseCase: AlarmListGetUseCase
) : ViewModel() {
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    private val _alarmList: MutableStateFlow<AlarmGetState> = MutableStateFlow(AlarmGetState.Init)
    val alarmList: StateFlow<AlarmGetState> get() = _alarmList.asStateFlow()
    private val _putAlarm: MutableStateFlow<AlarmPutState> = MutableStateFlow(AlarmPutState.Init)
    val putAlarm: StateFlow<AlarmPutState> get() = _putAlarm.asStateFlow()
    private val _network: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val network: StateFlow<Boolean> get() = _network.asStateFlow()

    fun getCreatedAtAlarmItemVo(createdAt: String): AlarmItemVo {
        return AlarmItemVo(
            1,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            createdAt = createdAt
        )
    }

    fun getAlarm(type: String) {
        viewModelScope.launch {
            alarmListGetUseCase(accessToken, deviceId, memberId, type)
                .onStart {
                    _alarmList.value = AlarmGetState.IsLoading(true)
                }
                .catch { exception ->
                    _alarmList.value = AlarmGetState.IsLoading(false)
                    _alarmList.value = AlarmGetState.Failure(exception.message.default())
                }
                .collect {
                    _alarmList.value = AlarmGetState.IsLoading(false)
                    _alarmList.value = AlarmGetState.Success(type, it)
                }
        }
    }

    fun putAlarm() = viewModelScope.launch {
        alarmPutUseCase(accessToken, deviceId, memberId)
            .onStart {
                _putAlarm.value = AlarmPutState.IsLoading(true)
            }
            .catch { exception ->
                _putAlarm.value = AlarmPutState.IsLoading(false)
                _putAlarm.value = AlarmPutState.Failure(exception.message.default())
            }
            .collect {
                _putAlarm.value = AlarmPutState.IsLoading(false)
                _putAlarm.value = AlarmPutState.Success(true)
            }
    }

    sealed class AlarmGetState {
        object Init : AlarmGetState()
        data class IsLoading(val isLoading: Boolean) : AlarmGetState()
        data class Success(val type: String, val alarmList: List<AlarmItemVo>) : AlarmGetState()
        data class Failure(val message: String) : AlarmGetState()
    }

    sealed class AlarmPutState {
        object Init : AlarmPutState()
        data class IsLoading(val isLoading: Boolean) : AlarmPutState()
        data class Success(val isSuccess: Boolean) : AlarmPutState()
        data class Failure(val message: String) : AlarmPutState()
    }
}
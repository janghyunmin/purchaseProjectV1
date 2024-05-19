package run.piece.dev.refactoring.ui.deletemember

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
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.domain.refactoring.member.model.MemberDeleteVo
import run.piece.domain.refactoring.member.model.request.MemberDeleteModel
import run.piece.domain.refactoring.member.usecase.MemberDeleteUseCase
import javax.inject.Inject

@HiltViewModel
class DeleteMemberDetialViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle,
                                                      private val resourcesProvider: ResourcesProvider,
                                                      private val memberDeleteUseCase: MemberDeleteUseCase) : ViewModel() {

    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    val withdrawalReasonCode: String? = savedStateHandle.get<String>("withdrawalReasonCode")
    val withdrawalReasonText: String? = savedStateHandle.get<String>("withdrawalReasonText")

    private val _memberDelete: MutableStateFlow<MemberDeleteState> = MutableStateFlow(MemberDeleteState.Init)
    val memberDelete: StateFlow<MemberDeleteState> = _memberDelete.asStateFlow()

    fun memberDelete() {
        val model = MemberDeleteModel(withdrawalReasonCode = "$withdrawalReasonCode", withdrawalReasonText = "$withdrawalReasonText")
        viewModelScope.launch {
            viewModelScope.launch {
                memberDeleteUseCase(
                    accessToken = "Bearer $accessToken",
                    deviceId = deviceId,
                    memberId = memberId,
                    memberDeleteModel = model
                ).onStart {
                    _memberDelete.value = MemberDeleteState.Loading(true)
                }.catch { exception ->
                    _memberDelete.value = MemberDeleteState.Loading(false)
                    _memberDelete.value = MemberDeleteState.Failure(exception.message.toString())
                }.collect {
                    _memberDelete.value = MemberDeleteState.Loading(false)
                    _memberDelete.value = MemberDeleteState.Success(it)
                }
            }
        }
    }

    sealed class MemberDeleteState {
        object Init : MemberDeleteState()
        data class Loading(val isLoading: Boolean) : MemberDeleteState()
        data class Success(val memberDeleteVo: MemberDeleteVo) : MemberDeleteState()
        data class Failure(val message: String) : MemberDeleteState()
    }
}
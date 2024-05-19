package run.piece.dev.refactoring.ui.newinvestment

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.domain.refactoring.member.model.MemberVo
import javax.inject.Inject

@HiltViewModel
class InvestmentIntroViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle, private val resourcesProvider: ResourcesProvider) : ViewModel() {
    val memberVo = savedStateHandle.get<MemberVo>("memberVo")
}
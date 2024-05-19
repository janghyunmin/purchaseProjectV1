package run.piece.dev.refactoring.ui.logout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.refactoring.module.ResourcesProvider
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel@Inject constructor(private val resourcesProvider: ResourcesProvider,
                                         private val savedStateHandle: SavedStateHandle): ViewModel() {

}
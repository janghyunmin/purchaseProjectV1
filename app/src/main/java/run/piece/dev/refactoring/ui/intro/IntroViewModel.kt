package run.piece.dev.refactoring.ui.intro

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.utils.DisplayManager
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor (private val displayManager: DisplayManager): ViewModel() {
    fun getDeviceType(width: Int) = displayManager.getDeviceType(width)
}
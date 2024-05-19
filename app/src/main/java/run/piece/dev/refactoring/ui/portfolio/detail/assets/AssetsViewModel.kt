package run.piece.dev.refactoring.ui.portfolio.detail.assets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.refactoring.module.ResourcesProvider
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor (private val resourcesProvider: ResourcesProvider,
                                           private val savedStateHandle: SavedStateHandle): ViewModel() {
    val portfolioId: String? = savedStateHandle["portfolioId"]
    val recruitmentState: String? = savedStateHandle["recruitmentState"]

    fun getDeviceHeight(): Int = resourcesProvider.getDeviceHeight()
    fun getDeviceWidth(): Int = resourcesProvider.getDeviceWidth()
    fun dpToPixel(pixel: Int) = resourcesProvider.dpToPixel(pixel)
}
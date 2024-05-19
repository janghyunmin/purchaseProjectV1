package run.piece.dev.refactoring.ui.portfolio.detail.marketinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.refactoring.module.ResourcesProvider
import javax.inject.Inject

@HiltViewModel
class MarketInfoViewModel @Inject constructor (private val savedStateHandle: SavedStateHandle,
                                               private val resourcesProvider: ResourcesProvider): ViewModel() {
    fun dpToPixel(pixel: Int): Int = resourcesProvider.dpToPixel(pixel)
    fun getDeviceWidth(): Int = resourcesProvider.getDeviceWidth()
}
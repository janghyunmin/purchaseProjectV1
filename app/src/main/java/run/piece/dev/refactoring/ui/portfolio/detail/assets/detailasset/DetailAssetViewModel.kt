package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.refactoring.module.ResourcesProvider
import javax.inject.Inject

@HiltViewModel
class DetailAssetViewModel @Inject constructor(private val resourcesProvider: ResourcesProvider) : ViewModel() {
    fun dpToPixel(dp: Int) = resourcesProvider.dpToPixel(dp)
    fun getDeviceWidth(): Int = resourcesProvider.getDeviceWidth()
}
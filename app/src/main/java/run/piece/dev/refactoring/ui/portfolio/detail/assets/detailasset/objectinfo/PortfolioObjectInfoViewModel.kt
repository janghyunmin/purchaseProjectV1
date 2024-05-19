package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.refactoring.module.ResourcesProvider
import javax.inject.Inject

@HiltViewModel
class PortfolioObjectInfoViewModel @Inject constructor (private val resourcesProvider: ResourcesProvider): ViewModel() {
    fun dpToPixel(dp: Int): Int = resourcesProvider.dpToPixel(dp)
    fun getPercentText(item: ObjectInfoItem): String = "${item.percent}%"
}
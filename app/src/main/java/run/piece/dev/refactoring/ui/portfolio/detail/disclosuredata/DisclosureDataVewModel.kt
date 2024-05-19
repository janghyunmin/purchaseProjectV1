package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import run.piece.dev.data.refactoring.module.ResourcesProvider
import javax.inject.Inject

@HiltViewModel
class DisclosureDataVewModel @Inject constructor(private val resourcesProvider: ResourcesProvider): ViewModel() {
    fun dpToPixel(dp: Int): Int = resourcesProvider.dpToPixel(dp)
}
package run.piece.dev.refactoring.ui.portfolio.detail.story

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StoryDetailViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle): ViewModel() {
    var html: String? = savedStateHandle["html"]

}
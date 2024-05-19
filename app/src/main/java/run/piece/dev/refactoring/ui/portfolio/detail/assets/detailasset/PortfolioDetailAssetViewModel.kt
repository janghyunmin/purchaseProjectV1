package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

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
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo
import run.piece.domain.refactoring.portfolio.usecase.PortfolioProductGetUseCase
import javax.inject.Inject

@HiltViewModel
class PortfolioDetailAssetViewModel @Inject constructor (private val savedStateHandle: SavedStateHandle,
                                                         private val resourcesProvider: ResourcesProvider,
                                                         private  val portfolioProductGetUseCase: PortfolioProductGetUseCase): ViewModel() {
    var id: String? = savedStateHandle["portfolioId"]
    val recruitmentState: String? = savedStateHandle["recruitmentState"]
    val productId: String? = savedStateHandle["productId"]

    private val _portfolioProduct: MutableStateFlow<PortfolioProductState> = MutableStateFlow(
        PortfolioProductState.Init)
    val portfolioProduct: StateFlow<PortfolioProductState> get() = _portfolioProduct.asStateFlow()

    fun getPortfolioProduct() {
        viewModelScope.launch {
            id?.let {
                portfolioProductGetUseCase(it, "v0.0.2")
                    .onStart {
                        _portfolioProduct.value = PortfolioProductState.IsLoading(true)
                    }
                    .catch { exception ->
                        _portfolioProduct.value = PortfolioProductState.IsLoading(false)
                        _portfolioProduct.value = PortfolioProductState.Failure(exception.message.default())
                    }
                    .collect { data ->
                        _portfolioProduct.value = PortfolioProductState.IsLoading(false)
                        _portfolioProduct.value = PortfolioProductState.Success(data)
                    }
            }
        }
    }

    fun dpToPixel(dp: Int): Int = resourcesProvider.dpToPixel(dp)
}

sealed class PortfolioProductState {
    object Init : PortfolioProductState()
    data class IsLoading(val isLoading: Boolean) : PortfolioProductState()
    data class Success(val portfolioProductVo: List<PortfolioProductVo>): PortfolioProductState()
    data class Failure(val message: String) : PortfolioProductState()
}
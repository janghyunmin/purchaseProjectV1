package run.piece.dev.refactoring.ui.deed

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.airbnb.lottie.LottieAnimationView
import com.rajat.pdfviewer.PdfRendererView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deed.model.MemberDocumentVo
import run.piece.domain.refactoring.deed.usecase.SendEmailUseCase
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.portfolio.model.PortfolioStockItemVo
import javax.inject.Inject

@HiltViewModel
class DeedPdfViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sendEmailUseCase: SendEmailUseCase
) : ViewModel() {
    val purchaseId: String? = savedStateHandle.get<String>("purchaseId")
    val detailDefaultVo: PortfolioDetailDefaultVo? = savedStateHandle.get<PortfolioDetailDefaultVo>("detailDefaultVo")
    val stockVo: PortfolioStockItemVo? = savedStateHandle.get<PortfolioStockItemVo>("stockVo")
    val attachFileVo: ArrayList<AttachFileItemVo>? = savedStateHandle.get<ArrayList<AttachFileItemVo>>("attachFileItemVo")
    val title: String? = savedStateHandle.get<String>("title")
    val pdfUrl: String? = savedStateHandle.get<String>("pdfUrl")
    val viewType: String? = savedStateHandle.get<String>("viewType")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private val _emailSend: MutableStateFlow<EmailSendState> = MutableStateFlow(
        EmailSendState.Init
    )
    val emailSend: StateFlow<EmailSendState> get() = _emailSend.asStateFlow()

    fun showPdf(
        callName: String,
        activity: Activity,
        pdfView: PdfRendererView,
        loading: LottieAnimationView,
        url: String,
        lifecycleScope: LifecycleCoroutineScope,
        lifecycle: Lifecycle
    ) {
        try {
            viewModelScope.launch(Dispatchers.Main) {
                pdfView.statusListener = object : PdfRendererView.StatusCallBack {
                    override fun onPdfLoadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {
                        super.onPdfLoadProgress(progress, downloadedBytes, totalBytes)
                        loading.visibility = View.VISIBLE
                    }

                    override fun onPdfLoadStart() {
                        Log.i("statusCallBack", "onPdfLoadStart")
                    }

                    override fun onPdfLoadSuccess(absolutePath: String) {
                        super.onPdfLoadSuccess(absolutePath)
                        loading.visibility = View.GONE
                        pdfView.post {
                            pdfView.recyclerView.scrollToPosition(0)
                        }
                    }

                    override fun onError(error: Throwable) {
                        super.onError(error)
                        Log.e("onError : ", "${error.message}")
                    }
                }

                pdfView.initWithUrl(
                    url = url,
                    lifecycleCoroutineScope = lifecycleScope,
                    lifecycle = lifecycle
                )

            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.e("Error : ", "${ex.message}")
        }
    }

    fun sendEmail() {
        viewModelScope.launch {
            purchaseId?.let {
                sendEmailUseCase("Bearer $accessToken", deviceId, memberId, MemberDocumentVo(memberId, it, "EMAIL"))
                    .onStart {
                        _emailSend.value = EmailSendState.IsLoading(true)
                    }
                    .catch { exception ->
                        _emailSend.value = EmailSendState.IsLoading(false)
                        _emailSend.value = EmailSendState.Failure(exception.message.default())
                    }
                    .collect { item ->
                        _emailSend.value = EmailSendState.IsLoading(false)
                        _emailSend.value = EmailSendState.Success(item)
                    }
            }
        }
    }


    sealed class EmailSendState {
        object Init : EmailSendState()
        data class IsLoading(val isLoading: Boolean) : EmailSendState()
        data class Success(val isSuccess: BaseVo) : EmailSendState()
        data class Failure(val message: String) : EmailSendState()
    }
}

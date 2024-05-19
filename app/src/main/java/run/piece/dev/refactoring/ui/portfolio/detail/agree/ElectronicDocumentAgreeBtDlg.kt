package run.piece.dev.refactoring.ui.portfolio.detail.agree

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import run.piece.dev.databinding.SlideupElectronicDocumentBinding
import run.piece.dev.refactoring.ui.portfolio.detail.PortfolioDetailNewActivity
import run.piece.dev.refactoring.utils.onThrottleClick

class ElectronicDocumentAgreeBtDlg(context: Context) : BottomSheetDialogFragment() {
    lateinit var binding: SlideupElectronicDocumentBinding
    lateinit var activity: PortfolioDetailNewActivity
    private var listener: OnSendFromBottomSheetDialog? = null
    private val display = context.resources?.displayMetrics // Fold UI 대응

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SlideupElectronicDocumentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this@ElectronicDocumentAgreeBtDlg
        binding.apply {

            confirmBtn.onThrottleClick {
                listener?.sendValue()
                dismiss()
            }
        }

        isCancelable = true

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as PortfolioDetailNewActivity
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }

    interface OnSendFromBottomSheetDialog {
        fun sendValue()
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }

    private fun getBottomSheetDialogDefaultHeight(): Double {
        if(display != null) {
            // 폴드 펼침
            if(display.widthPixels > 1600) {
                return getWindowHeight() * 68.9 / 100
            }
            // 미니 , 폴드 닫힘
            else if(display.widthPixels < 980) {
                return getWindowHeight() * 65.0 / 100
            }
            // 일반
            else {
                return getWindowHeight() * 77.3 / 100
            }
        }
        return getWindowHeight() * 77.3 / 100
    }
    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}
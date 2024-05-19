package run.piece.dev.refactoring.ui.deposit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import run.piece.dev.databinding.SlideupNhAccountNotiBinding
import run.piece.dev.refactoring.utils.onThrottleClick

// 가상계좌 만들기 BottomSheet Dialog
class NhAccountNotiBtDlg(context: Context) : BottomSheetDialogFragment() {
    lateinit var binding: SlideupNhAccountNotiBinding
    private var listener: OnSendFromBottomSheetDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SlideupNhAccountNotiBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this@NhAccountNotiBtDlg

        binding.apply {

            nhBtnTv.onThrottleClick {
                listener?.sendValue()
            }

            closeIconLayout.onThrottleClick {
                dismiss()
            }
        }

        // x 버튼으로 닫기 컨트롤하려고 false 적용
        isCancelable = false

        return binding.root
    }

    interface OnSendFromBottomSheetDialog {
        fun sendValue()
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }

}
package run.piece.dev.refactoring.ui.magazine

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import run.piece.dev.R
import run.piece.dev.databinding.SlideupSharedBinding

class NewMagazineBtDlg(context: Context, layoutId: Int): Dialog(context, layoutId) {
    private lateinit var binding: SlideupSharedBinding
    private lateinit var dialog: Dialog
    private var listener: OnSendFromBottomSheetDialog? = null


    fun showDialog(isFavorite: String?) {

        binding = SlideupSharedBinding.inflate(LayoutInflater.from(context))
        dialog = setDialogOptions()


        binding.run {
            setCancelable(true)

            if(isFavorite == "N" || isFavorite.isNullOrEmpty() || isFavorite.isBlank()) {
                binding.bIcon.isSelected = true
                binding.bIcon.background = context.getDrawable(R.drawable.bm_bookmark_on_icon)
                binding.bTitle.text = "북마크"
            } else {
                binding.bIcon.isSelected = false
                binding.bIcon.background = context.getDrawable(R.drawable.bm_bookmark_off_icon)
                binding.bTitle.text = "북마크 취소"
            }


            binding.closeIcon.setOnClickListener {
                dialog.dismiss()
            }

            binding.shareLayout.setOnClickListener{
                if (listener == null) return@setOnClickListener
                listener?.sendValue("공유",false)
                dialog.dismiss()
            }

            binding.copyLayout.setOnClickListener {
                if (listener == null) return@setOnClickListener
                listener?.sendValue("링크 복사",false)
                dialog.dismiss()
            }
            binding.bookmarkLayout.setOnClickListener{
                binding.bIcon.isSelected = !binding.bIcon.isSelected

                listener?.sendValue("북마크",binding.bIcon.isSelected)
                dialog.dismiss()
            }

        }
    }


    private fun setDialogOptions(): Dialog = Dialog(context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        window?.run {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setGravity(Gravity.BOTTOM)
        }
        show()
    }

    interface OnSendFromBottomSheetDialog {
        fun sendValue(value: String,boolean: Boolean)
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }
}
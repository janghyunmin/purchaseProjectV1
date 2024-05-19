package run.piece.dev.widget.utils

import android.app.Dialog
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import run.piece.dev.R
import run.piece.dev.databinding.DocumentImageDialogBinding

object ImageDialogManager {
    fun getDialog(context: Context, name: String, target: ImageCloseListener) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DocumentImageDialogBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)

        Glide.with(context)
            .load(name)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(dialogBinding.documentImagePath)


        dialogBinding.closeBtn.setOnClickListener {
            target.onClickCancelButton()
            dialog.dismiss()
        }

        dialog.show()
    }
}
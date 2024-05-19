package run.piece.dev.refactoring.base

import android.app.ActionBar
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

open class BaseDF: AppCompatDialogFragment() {
    open fun getWidthMarginDp(): Float {
        return 16f
    }

    open fun getDimAmount(): Float {
        return 0.35f
    }

    override fun onResume() {
        context?.let {
            val displayWidth = it.resources.displayMetrics.widthPixels
            val marginDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getWidthMarginDp(), it.resources.displayMetrics).toInt()

            val dfWidth = displayWidth - (marginDp * 2)

            val layoutParams = dialog?.window?.attributes
            layoutParams?.width = dfWidth
            layoutParams?.height = ActionBar.LayoutParams.WRAP_CONTENT

            dialog?.window?.attributes = layoutParams
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)    // dialog 테두리 shadow 없애기 위함
            dialog?.window?.setDimAmount(getDimAmount())
        }
        // call super onResume after sizing
        super.onResume()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val fragmentByTag = manager.findFragmentByTag(tag)
        if (fragmentByTag == null) {
            manager.beginTransaction().add(this, tag).commitNow()
        }
    }
}
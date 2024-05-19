package run.piece.dev.refactoring.base

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBDF: BottomSheetDialogFragment() {

    lateinit var bottomSheetDialog: BottomSheetDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        // BDF가 항상 expand 상태여야해서 넣은 코드 (직접입력 눌러서 레이아웃이 커졌을때도 expand 상태로 만들기 위함)android.support.design.R.id.design_bottom_sheet
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

        }
        bottomSheetDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        return bottomSheetDialog
    }

    open fun updateBdfStyle(style: Int) {
        //BaseBDF의 style을 즉각 변경하기위한 메서드
        // BaseBDF는 1개, 자식 BDF의 View 종류가 복수일 경우 사용하기 위해서 만들었습니다.
        bottomSheetDialog.window?.setSoftInputMode(style)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setDimAmount(0.35f)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val fragmentByTag = manager.findFragmentByTag(tag)
        if (fragmentByTag == null) {
            manager.beginTransaction().add(this, tag).commitNow()
        }
    }
}
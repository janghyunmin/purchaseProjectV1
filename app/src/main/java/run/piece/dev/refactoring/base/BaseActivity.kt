package run.piece.dev.refactoring.base

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import run.piece.dev.R
import run.piece.dev.refactoring.utils.LogUtil

abstract class BaseActivity<B : ViewDataBinding, VM: ViewModel>(@LayoutRes val layout: Int) : AppCompatActivity() {
    protected lateinit var binding: B
    protected lateinit var viewModel: VM
    abstract fun getViewModelClass(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layout)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(getViewModelClass())
        settingStatusColor()
    }

    fun settingStatusColor(isBlack: Boolean = true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            window.decorView.systemUiVisibility =
                if (isBlack) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // minSdk 6.0부터 사용 가능
                else window.decorView.systemUiVisibility
        }
        if (isBlack) window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        else window.statusBarColor = ContextCompat.getColor(this, R.color.black)
    }

    fun customBackPressed(activity: Activity) {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    LogUtil.d("Activity BackPressed 실행")

                    if (isEnabled) {
                        isEnabled = false
                        activity.finish()
                        overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
                    }
                }
            }
        )
    }
}
package run.piece.dev.refactoring.ui.common

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import run.piece.dev.databinding.ActivityServerCheckBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick

@AndroidEntryPoint
class ServerCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServerCheckBinding
    private lateinit var coroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServerCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this@ServerCheckActivity
        binding.activity = this@ServerCheckActivity

        LogUtil.v("ServerCheckActivity onCreate ! ")

        window?.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        coroutineScope = lifecycleScope

        intent?.let { intent ->
            binding.titleTv.text = intent.getStringExtra("inspectionTitle").toString().default()
            binding.subTitleTv.text = intent.getStringExtra("inspectionContent").toString().default()
            binding.dateTv.text = intent.getStringExtra("inspectionDate").toString().default()
        }

        // 앱 종료
        binding.confirmBtn.onThrottleClick {
            finishAffinity()
        }

        BackPressedUtil().activityCreate(this@ServerCheckActivity,this@ServerCheckActivity)

        val backPressCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@ServerCheckActivity.finishAffinity()
            }
        }
        this@ServerCheckActivity.onBackPressedDispatcher.addCallback(this@ServerCheckActivity, backPressCallBack)


    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onResume() {
        super.onResume()

        coroutineScope = lifecycleScope
    }

}
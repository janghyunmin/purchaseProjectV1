package run.piece.dev.refactoring.ui.logout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityLogoutBinding
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection

//로그아웃 화면 Activity
@AndroidEntryPoint
class LogoutActivity : AppCompatActivity(R.layout.activity_logout){
    private lateinit var coroutineScope: CoroutineScope

    private lateinit var binding: ActivityLogoutBinding
    private val viewModel: LogoutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogoutBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.activity = this@LogoutActivity
        binding.viewModel = viewModel

        setContentView(binding.root)

        App()

        coroutineScope = lifecycleScope

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this@LogoutActivity) { isConnected ->
            if (!isConnected) startActivity(Intent(this, NetworkActivity::class.java))
        }

        binding.apply {

        }

        coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

            }
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        BackPressedUtil().activityCreate(this@LogoutActivity,this@LogoutActivity)
        BackPressedUtil().systemBackPressed(this@LogoutActivity,this@LogoutActivity)

    }

    fun reLogin() {
        val intent = Intent(this@LogoutActivity, NewPassCodeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("requestCode","9999")
        intent.putExtra("Step", "2")
        setResult(RESULT_OK,intent)
        startActivityForResult(intent,9999)
        finish()
    }

    companion object {
        fun getIntent(context: Context): Intent = Intent(context, LogoutActivity::class.java)
    }
}
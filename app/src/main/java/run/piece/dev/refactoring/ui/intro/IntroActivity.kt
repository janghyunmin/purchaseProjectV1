package run.piece.dev.refactoring.ui.intro

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityIntroBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.join.NewJoinActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.PermissionHelper
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.DeviceInfoUtil

@AndroidEntryPoint
class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel: IntroViewModel by viewModels()
    private val dataNexusViewModel by viewModels<DataNexusViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.activity = this@IntroActivity
        binding.lifecycleOwner = this@IntroActivity
        binding.dataStoreViewModel = dataNexusViewModel

        binding.apply {
            if(!isNetworkConnected(this@IntroActivity)) {
                startActivity(getNetworkActivity(this@IntroActivity))
            }

            iButton.onThrottleClick {
                val intent = Intent(this@IntroActivity, NewJoinActivity::class.java)
                intent.putExtra("Step", "1")
                startActivity(intent)
            }

            pieceText.onThrottleClick {
                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                startActivity(intent)
            }

            val uri = Uri.parse("android.resource://${packageName}/${R.raw.on_boarding}")
            videoView.setZOrderOnTop(false)
            videoView.setVideoURI(uri)
            videoView.setOnPreparedListener {
                videoView.setZOrderOnTop(true)
                it.isLooping = true
                it.start()
            }
        }

        intent?.let {
            it.getStringExtra("another")?.let { another ->
                if(another.isNotEmpty() && another.isNotBlank() && another != "null") {
                    val appConfirmDF = AppConfirmDF.newInstance(
                        "로그아웃 알림",
                        "새로운 기기 로그인",
                        false,
                        R.string.confirm,
                        positiveAction = {},
                        dismissAction = {}
                    )
                    appConfirmDF.show(supportFragmentManager, "Logout")
                }
            }
        }

        window?.apply {
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            addFlags(WindowManager.LayoutParams.FLAG_SECURE) //캡처 방지
//            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            val deviceIdSave = async { deviceIdSave() }
            val permissionChk = async { permissionChk() }

            deviceIdSave.join()
            permissionChk.join()
        }

        createNotificationChannel()
        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.stopPlayback()

        coroutineScope.cancel()
        BackPressedUtil().activityFinish(this@IntroActivity,this@IntroActivity)
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private suspend fun deviceIdSave() {
        delay(100)
        val deviceId: String = DeviceInfoUtil.getUUID(this@IntroActivity)
        PrefsHelper.write("deviceId", deviceId)
        dataNexusViewModel.putDeviceId(deviceId)
    }

    private suspend fun permissionChk() {
        delay(400)
        PermissionHelper.requestAllPermission(this@IntroActivity)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel("96071973609", "piece_noti", NotificationManager.IMPORTANCE_HIGH)
        channel.description = "Description"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            for ((i, permission) in permissions.withIndex()) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.e("테스트 : 퍼미션 - ", "권한 부여 안됨 false $permission")
                } else {
                    Log.e("테스트 : 퍼미션 - : ", "권한 부여 됨 true $permission")
                }
            }
        }
    }

    companion object {
        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }
    }
}

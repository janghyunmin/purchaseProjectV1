package run.piece.dev.refactoring.ui.splash

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import run.piece.dev.App
import run.piece.dev.BuildConfig
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivitySplashBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.datastore.EventState
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil

/* AppUpdateType.FLEXIBLE 선택
     AppUpdateType.IMMEDIATE 강제 */
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val viewModel by viewModels<SplashViewModel>()
    private val dataNexusViewModel by viewModels<DataNexusViewModel>()
    private var fbToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coroutineScope = lifecycleScope
        binding.activity = this@SplashActivity
        binding.lifecycleOwner = this@SplashActivity
        binding.dataStoreViewModel = dataNexusViewModel

        binding.apply {
            appUpdateManager = AppUpdateManagerFactory.create(this@SplashActivity)
            // 상단 StatusBar 투명처리
            window.apply {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }

        coroutineScope.launch {
            withContext(Dispatchers.Default) {

                // Google Analytics Setting
                firebaseAnalytics = Firebase.analytics
                launch(Dispatchers.IO) {
                    App()
                    FirebaseApp.initializeApp(applicationContext) // App 시작시 Firebase init
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            PrefsHelper.write("fcmToken", task.result)
                            fbToken = task.result
                        }
                    }
                }.join()

                launch(Dispatchers.IO) {
                    delay(500)
                    dataNexusViewModel.setFbToken(fbToken = fbToken).collect { event ->
                        updateViewOnEvent(event)
                    }
                }
            }

            launch(Dispatchers.Main) {
                if (!BuildConfig.DEBUG) {
                    viewLogEvent(getActivityName())
                }

                binding.splashAnimationView.setAnimation("splash1.json")
                binding.splashAnimationView.loop(false)
                binding.splashAnimationView.playAnimation()
                val mApp = packageManager.getPackageInfo(packageName, 0)

                // EventBus에서 전송되는 값을 받음
                App.EventBus.subscribe<String>().collect { value ->
                    when (value) {
                        "APP_FINISH" -> {
                            val appConfirmDF = AppConfirmDF.newInstance(
                                "보안알림",
                                "해킹위험이 탐지되어 앱을 종료할게요",
                                false,
                                R.string.confirm_text,
                                positiveAction = {
                                    finishAffinity()
                                }, dismissAction = {

                                }
                            )
                            delay(300)
                            appConfirmDF.show(supportFragmentManager, "위변조 앱 종료")
                        }

                        "APP_RUNNING" -> {
                            val localVersion = BuildConfig.VERSION_NAME // 현재 AppVersion Code
                            viewModel.appVersion = localVersion

                            binding.splashAnimationView.addAnimatorListener(object :
                                Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {}
                                override fun onAnimationEnd(animation: Animator) {

                                    when (BuildConfig.FLAVOR) {
                                        "real" -> {
                                            if (!BuildConfig.DEBUG) {
                                                appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                                                    when (info.updateAvailability()) {
                                                        UpdateAvailability.UPDATE_AVAILABLE -> {
                                                            // 현재 최신버전이 아니며 업데이트가 필요한 경우
                                                            if (info.isUpdateTypeAllowed(viewModel.updateType)) {
                                                                requestUpdate(info)
                                                            }
                                                        }

                                                        UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                                                            // 현재 최신버전이며 업데이트가 필요 하지 않은 경우
                                                            nextStep()
                                                        }

                                                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {}
                                                        UpdateAvailability.UNKNOWN -> {}
                                                    }
                                                }.addOnFailureListener {
                                                    /**
                                                     * 디바이스에 연결된 계정으로 해당 앱을 받지 않았을 경우,
                                                     * 디바이스에 연결된 계정들 중 어느 하나도 해당 앱을 다운 받은 적이 없을 때를 의미한다.
                                                     * 로그인 한 계정으로 들어가 앱을 설치한 뒤 앱을 실행하면 해결이 된다고 한다.
                                                     * 구글스토어 측 오류...
                                                     **/
                                                    nextStep()
                                                }

                                            } else {
                                                nextStep()
                                            }
                                        }

                                        else -> {
                                            nextStep()
                                        }
                                    }
                                }

                                override fun onAnimationCancel(animation: Animator) {}
                                override fun onAnimationRepeat(animation: Animator) {}
                            })
                        }
                    }
                }
            }
        }

        BackPressedUtil().systemBackPressed(this@SplashActivity, this@SplashActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                requestUpdate(info)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == viewModel.updateCode) {
            if (resultCode != RESULT_OK) {
                BackPressedUtil().activityFinish(this@SplashActivity, this@SplashActivity)
            }
        }
    }

    private fun nextStep() {
        // 스플래쉬도 끝났고 서버에서 406 에러를 줄 경우 화면전환
        CoroutineScope(Dispatchers.Main).launch {
            if(viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                launch(Dispatchers.IO) {
                    this@SplashActivity.viewModel.memberDeviceChk()
                }

                launch(Dispatchers.Main) {
                    try {
                        binding.loadingLayout.visibility = View.VISIBLE
                        this@SplashActivity.viewModel.deviceChk.collect {
                            when(it) {
                                is SplashViewModel.MemberDeviceState.Success -> {
                                    delay(700)
                                    goPassCode()
                                }
                                is SplashViewModel.MemberDeviceState.Failure -> {
                                    val statusCode = extractStatusCode(it.message)

                                    when(statusCode) {
                                        406 -> {
                                            App()
                                        }
                                        else -> {
                                            PrefsHelper.removeKey("inputPinNumber")
                                            PrefsHelper.removeKey("memberId")
                                            PrefsHelper.removeKey("isLogin")
                                            startActivity(NewPassCodeActivity.getIntroActivity(this@SplashActivity))
                                            finish()
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    } catch (exception : Exception) {
                        exception.printStackTrace()
                    }
                }
            }
            else {
                App()
                val intent = Intent(this@SplashActivity, IntroActivity::class.java).apply {
                    putExtra("Step", "1")
                }
                startActivity(intent)
                overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
                finish()
            }
        }
    }


    private fun requestUpdate(info: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(info, viewModel.updateType, this, viewModel.updateCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    // FbToken Event
    private fun updateViewOnEvent(
        event: EventState
    ) {
        when (event) {
            is EventState.SetInit -> {}

            is EventState.GetSuccess -> {
                val eventFbToken = event.fbToken
//                LogUtil.e("eventFbToken :$eventFbToken")
            }
        }
    }

    private fun goPassCode() {
        binding.loadingLayout.visibility = View.GONE
        startActivity(getPassCodeActivity(this@SplashActivity))
        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
        finish()
    }

    // Google Analytics
    private fun viewLogEvent(screenName: String) {
        val bundle = Bundle().apply {
            putString("screen_name", getActivityName())
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    private fun getActivityName(): String {
        return this::class.simpleName ?: TAG
    }

    private fun extractStatusCode(errorMessage: String): Int {
        val regex = Regex("""(\d{3})""")
        val matchResult = regex.find(errorMessage)
        return matchResult?.value?.toInt() ?: -1
    }

    companion object {
        private const val TAG = "SplashActivity"
        fun getIntent(context: Context): Intent = Intent(context, SplashActivity::class.java)

        fun getPassCodeActivity(context: Context) : Intent {
            val intent = Intent(context, NewPassCodeActivity::class.java).apply {
                putExtra("Step", "2")
            }
            return intent

        }
    }

}

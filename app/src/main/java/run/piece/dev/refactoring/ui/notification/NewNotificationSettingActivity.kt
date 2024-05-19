package run.piece.dev.refactoring.ui.notification

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityNewNotificationSettingBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.ui.consent.NewConsentActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.extension.SnackBarCommon
import run.piece.dev.widget.utils.AlarmDlgListener
import run.piece.dev.widget.utils.DialogManager
import run.piece.dev.widget.utils.NetworkConnection

@AndroidEntryPoint
class NewNotificationSettingActivity : AppCompatActivity(R.layout.activity_new_notification_setting) {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: ActivityNewNotificationSettingBinding
    private val viewModel: NewNotificationSettingViewModel by viewModels()

    lateinit var alarmDlgListener: AlarmDlgListener

    val backPressedUtil =  BackPressedUtil()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewNotificationSettingBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.activity = this@NewNotificationSettingActivity
        setContentView(binding.root)

        App()

        coroutineScope = lifecycleScope

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this@NewNotificationSettingActivity) { isConnected ->
            if (!isConnected) startActivity(Intent(this, NetworkActivity::class.java))
        }

        // 알림 설정 다이얼로그 리스너 초기화
        alarmDlgListener = object : AlarmDlgListener {
            override fun openOptionsClicked() {
                // 알림 설정 화면으로 이동
                val intent = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        Intent().apply {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, this@NewNotificationSettingActivity.packageName)
                        }
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        Intent().apply {
                            action = "android.settings.APP_NOTIFICATION_SETTINGS"
                            putExtra("app_package", this@NewNotificationSettingActivity.packageName)
                            putExtra("app_uid", this@NewNotificationSettingActivity.applicationInfo?.uid)
                        }
                    }
                    else -> {
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            addCategory(Intent.CATEGORY_DEFAULT)
                            data = Uri.parse("package:" + this@NewNotificationSettingActivity.packageName)
                        }
                    }
                }
                this@NewNotificationSettingActivity.startActivity(intent)
            }
            override fun offCloseClicked() {}
        }

        binding.apply {
            backLayout.onThrottleClick {
                BackPressedUtil().activityFinish(this@NewNotificationSettingActivity,this@NewNotificationSettingActivity)
            }
        }

        // 네트워크 연결 상태 확인 및 처리
        coroutineScope.launch {
            launch(Dispatchers.Main) {

            }

            // 회원 데이터 및 동의 리스트 비동기로 가져오기
            launch(Dispatchers.IO) {
                viewModel.getMemberData()
                viewModel.getConsentMemberTermsList()
            }

            // 회원 정보 수집 및 처리
            launch(Dispatchers.Main) {
                viewModel.memberInfo.collect { vo ->
                    when (vo) {
                        is NewNotificationSettingViewModel.MemberInfoState.Success -> {
                            // 회원 정보에서 알림 설정 상태 반영
                            when(vo.memberVo.notification.isNotice) {
                                "Y" -> {
                                    binding.infoNotificationSwitch.isChecked = true
                                }
                                "N" -> {
                                    binding.infoNotificationSwitch.isChecked = false
                                }
                            }

                            when(vo.memberVo.notification.isAd) {
                                "Y" -> {
                                    binding.adNotificationSwitch.isChecked = true
                                }
                                "N" -> {
                                    binding.adNotificationSwitch.isChecked = false
                                }
                            }

                            viewModel.isNotice = if (binding.infoNotificationSwitch.isChecked ) "Y" else "N"
                            viewModel.isAd = if (binding.adNotificationSwitch.isChecked ) "Y" else "N"
                        }

                        is NewNotificationSettingViewModel.MemberInfoState.Failure -> {
                            // 회원 정보 로딩 실패 시 오류 화면으로 이동
//                            startActivity(ErrorActivity.getIntent(this@NewNotificationSettingActivity))
                        }
                        else -> {}
                    }
                }
            }

            launch(Dispatchers.Main) {
                viewModel.consentMemberTerms.collect { data ->
                    when (data) {
                        is NewNotificationSettingViewModel.ConsentMemberTermsState.Success -> {
                            if (data.termsMemberVo.selective.consent.isNotEmpty()) {
                                viewModel.adUsageConsent = data.termsMemberVo.selective.consent[0].isAgreement
                            }

                            if (viewModel.adUsageConsent == "Y") {
                                binding.adNotificationSwitch.isChecked = true
                            }
                        }

                        is NewNotificationSettingViewModel.ConsentMemberTermsState.Failure -> {
//                            startActivity(ErrorActivity.getIntent(this@NewNotificationSettingActivity))
                        }
                        else -> {}
                    }
                }
            }

            // 회원 알림 설정 정보 수집 및 처리
            launch(Dispatchers.Main) {
                viewModel.memberNotification.collect { data ->
                    when(data) {
                        is NewNotificationSettingViewModel.MemberNotificationState.Succes -> {}
                        is NewNotificationSettingViewModel.MemberNotificationState.Failure -> {
//                            startActivity(ErrorActivity.getIntent(this@NewNotificationSettingActivity))
                        }
                        else -> {}
                    }
                }
            }

            binding.infoNotificationSwitch.onThrottleClick { it as SwitchCompat
                if (!NotificationManagerCompat.from(this@NewNotificationSettingActivity).areNotificationsEnabled()) {
                    DialogManager.openAlarmDlg(this@NewNotificationSettingActivity, this@NewNotificationSettingActivity, alarmDlgListener)
                    binding.infoNotificationSwitch.isChecked = !it.isChecked
                } else {
                    if (it.isChecked) {
                        viewModel.putMemberNotification(isNotice = "Y", isAd = viewModel.isAd)
                        SnackBarCommon(binding.root, "정보성 알림 수신에 동의했어요.", "정보성 알림 수신 동의").show(8)
                        PrefsHelper.write("isNotice", "Y")
                    } else {
                        AppConfirmDF.newInstance(
                            "정보성 알림 수신 해제",
                            "알림을 해제하면 중요한 내용을 받지 못할 수도 있어요.",
                            false,
                            positiveStrRes = R.string.unlock_txt,
                            positiveAction = {
                                viewModel.putMemberNotification(isNotice = "N", isAd = viewModel.isAd)
                                SnackBarCommon(binding.root, "정보성 알림 수신을 해제했어요.", "정보성 알림 수신 동의").show(8)
                                PrefsHelper.write("isNotice", "N")
                                binding.infoNotificationSwitch.isChecked = false
                            },
                            negativeStrRes = R.string.dismiss,
                            negativeAction = {
                                binding.infoNotificationSwitch.isChecked = true
                            },
                            dismissAction = {},
                            backgroundDrawable = R.drawable.btn_round_ff7878
                        ).show(supportFragmentManager, "정보성 알림 수신 해제")
                    }
                }
            }

            //체크 1: 기기 알람 설정 On/Off
            //체크 2 : adUsageConsent 선택동의 여부
            //체크 3 : 팝업 노출
            binding.adNotificationSwitch.onThrottleClick { it as SwitchCompat
                if (!NotificationManagerCompat.from(this@NewNotificationSettingActivity).areNotificationsEnabled()) {
                    DialogManager.openAlarmDlg(this@NewNotificationSettingActivity, this@NewNotificationSettingActivity, alarmDlgListener)
                    binding.adNotificationSwitch.isChecked = !it.isChecked //토글 상태 복원
                } else {

                    if (viewModel.adUsageConsent == "Y") { //정상
                        if (it.isChecked) {
                            viewModel.putMemberNotification(isNotice = viewModel.isNotice, isAd = "Y")
                            SnackBarCommon(binding.root, "광고성 알림 수신에 동의했어요.", "광고성 알림 수신에 동의").show(8)
                            PrefsHelper.write("isAd", "Y")
                        } else {
                            viewModel.putMemberNotification(isNotice = viewModel.isNotice, isAd = "N")
                            SnackBarCommon(binding.root, "광고성 알림 수신을 해제했어요.", "광고성 알림 수신에 동의").show(8)
                            PrefsHelper.write("isAd", "N")
                        }

                    } else { // 미동의
                        binding.adNotificationSwitch.isChecked = !it.isChecked //토글 상태 복원

                        AppConfirmDF.newInstance(
                            "약관 미동의",
                            "약관에 먼저 동의하셔야 소식을 받아보실 수 있어요.",
                            false,
                            R.string.go_consent_txt,
                            positiveAction = {
                                startActivity(NewConsentActivity.getIntent(this@NewNotificationSettingActivity))
                                BackPressedUtil().activityCreate(this@NewNotificationSettingActivity,this@NewNotificationSettingActivity)
                            },
                            R.string.dismiss,
                            negativeAction = {

                            },
                            dismissAction = {}
                        ).show(supportFragmentManager, "약관 미동의")
                    }
                }
            }

            backPressedUtil.activityCreate(this@NewNotificationSettingActivity,this@NewNotificationSettingActivity)
            backPressedUtil.systemBackPressed(this@NewNotificationSettingActivity,this@NewNotificationSettingActivity)
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }
    }

    override fun onResume() {
        super.onResume()
        // 기기 알림이 꺼져있으면 알림 설정 다이얼로그 표시
        if (!NotificationManagerCompat.from(this@NewNotificationSettingActivity).areNotificationsEnabled()) {
            DialogManager.openAlarmDlg(this@NewNotificationSettingActivity, this@NewNotificationSettingActivity, alarmDlgListener)
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.getConsentMemberTermsList()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // CoroutineScope를 사용한 작업이 취소되도록 설정
        coroutineScope.cancel()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, NewNotificationSettingActivity::class.java)
            return intent
        }
    }
}
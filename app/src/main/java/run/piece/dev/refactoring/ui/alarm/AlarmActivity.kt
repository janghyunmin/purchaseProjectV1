package run.piece.dev.refactoring.ui.alarm

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityAlarmBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.AlarmDlgListener
import run.piece.dev.widget.utils.DialogManager
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.alarm.model.AlarmItemVo

@AndroidEntryPoint
class AlarmActivity : BaseActivity<ActivityAlarmBinding>(R.layout.activity_alarm) {
    // 알림 및 혜택 조회 ViewModel
    private val viewModel: AlarmViewModel by viewModels()
    private lateinit var coroutineScope: CoroutineScope

    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        coroutineScope = lifecycleScope

        window?.apply {
            // 캡쳐방지 Kotlin Ver
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)

            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            alarmViewModel = viewModel
            lifecycleOwner = this@AlarmActivity

            coroutineScope.launch {
                this@AlarmActivity.viewModel.alarmList.collect {
                    when (it) {
                        is AlarmViewModel.AlarmGetState.Success -> {
                            val alarmAdapter = AlarmAdapter(this@AlarmActivity)

                            alarmRv.apply {
                                layoutManager = LinearLayoutManager(this@AlarmActivity, LinearLayoutManager.VERTICAL, false)
                                adapter = alarmAdapter
                            }

                            val alarmItemVoList = ArrayList<AlarmItemVo>()
                            val totalAlarmItemVoList = ArrayList<AlarmItemVo>()

                            it.alarmList.forEachIndexed { index, alarmItemVo ->
                                // 현재 알람 아이템을 리스트에 추가
                                alarmItemVoList.add(alarmItemVo)

                                // 마지막 요소인지 확인
                                val isLastElement = index == it.alarmList.size - 1

                                // 다음 요소의 createdAt 값과 현재 요소의 createdAt 값이 다른지 확인
                                val isDifferentCreatedAt = !isLastElement && it.alarmList[index].createdAt != it.alarmList[index + 1].createdAt

                                // 다음 createdAt 값이 다르거나 마지막 요소에 도달했을 때 처리
                                if (isDifferentCreatedAt || isLastElement) {
                                    // 마지막 요소의 createdAt 값이 현재 그룹에 속하는 경우에도 추가
                                    if (isLastElement && it.alarmList[index].createdAt == it.alarmList[it.alarmList.size -1].createdAt) {
                                        alarmItemVoList.add(it.alarmList[it.alarmList.size -1])
                                    }

                                    totalAlarmItemVoList.add(viewModel.getCreatedAtAlarmItemVo(it.alarmList[index].createdAt)) // 날짜
                                    totalAlarmItemVoList.addAll(alarmItemVoList) // 하위 아이템들

                                    // 알람 아이템 리스트 비우기
                                    alarmItemVoList.clear()
                                }
                            }

                            alarmAdapter.submitList(totalAlarmItemVoList)

                            if (alarmAdapter.currentList.size == 0) {
                                if (it.type == "NTT01") {
                                    noticeLayout.visibility = View.VISIBLE
                                    eventLayout.visibility = View.GONE
                                    alarmRv.visibility = View.GONE
                                } else if (it.type == "NTT02") {
                                    noticeLayout.visibility = View.GONE
                                    eventLayout.visibility = View.VISIBLE
                                    alarmRv.visibility = View.GONE
                                }
                            } else {
                                noticeLayout.visibility = View.GONE
                                eventLayout.visibility = View.GONE
                                alarmRv.visibility = View.VISIBLE
                            }

                        }
                        is AlarmViewModel.AlarmGetState.Failure -> {}
                        else -> {}
                    }
                }
            }

            val alarmDlgListener: AlarmDlgListener =
                object : AlarmDlgListener {
                    @RequiresApi(Build.VERSION_CODES.R)
                    override fun openOptionsClicked() {
                        val intent = when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                                Intent().apply {
                                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                    putExtra(Settings.EXTRA_APP_PACKAGE, this@AlarmActivity.packageName)
                                }

                            }
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                                Intent().apply {
                                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                    putExtra("app_package", this@AlarmActivity.packageName)
                                    putExtra("app_uid", this@AlarmActivity.applicationInfo?.uid)
                                }
                            }
                            else -> {
                                Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    addCategory(Intent.CATEGORY_DEFAULT)
                                    data = Uri.parse("package:" + this@AlarmActivity.packageName)
                                }
                            }
                        }
                        this@AlarmActivity.startActivity(intent)
                    }

                    override fun offCloseClicked() {

                    }
                }


            var notiChk = NotificationManagerCompat.from(this@AlarmActivity).areNotificationsEnabled()

            when (notiChk) {
                true -> {

                }
                false -> {
                    DialogManager.openAlarmDlg(this@AlarmActivity, this@AlarmActivity, alg_listener = alarmDlgListener)
                }
            }

            closeBtn.onThrottleClick {
                BackPressedUtil().activityFinish(this@AlarmActivity,this@AlarmActivity)
            }

            // 비 로그인 일때
            if (PrefsHelper.read("isLogin", "").equals("")) {
                binding.noticeLayout.visibility = View.VISIBLE

                // 알림 / 혜택 OnClick tab 기능 - jhm 2022/10/16

                binding.noticeTitle.onThrottleClick {
                    binding.benefitTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_dadce3))
                    binding.noticeTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_131313))

                    binding.noticeLayout.visibility = View.VISIBLE
                    binding.eventLayout.visibility = View.GONE
                    binding.alarmRv.visibility = View.GONE
                }

                binding.benefitTitle.onThrottleClick {
                    binding.benefitTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_131313))
                    binding.noticeTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_dadce3))

                    binding.noticeLayout.visibility = View.GONE
                    binding.eventLayout.visibility = View.VISIBLE
                    binding.alarmRv.visibility = View.GONE
                }
            }
            // 로그인 상태일때
            else {
                viewModel.putAlarm()
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getAlarm("NTT01")
                }

                PrefsHelper.write("noti", "N")

                // 알림 / 혜택 OnClick tab 기능 - jhm 2022/10/16
                binding.noticeTitle.onThrottleClick {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.getAlarm("NTT01")
                    }

                    binding.benefitTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_dadce3))
                    binding.noticeTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_131313))
                }

                binding.benefitTitle.onThrottleClick {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.getAlarm("NTT02")
                    }

                    binding.benefitTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_131313))
                    binding.noticeTitle.setTextColor(this@AlarmActivity.getColor(R.color.c_dadce3))
                }

            }
        }

        BackPressedUtil().activityCreate(this@AlarmActivity,this@AlarmActivity)
        BackPressedUtil().systemBackPressed(this@AlarmActivity,this@AlarmActivity)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
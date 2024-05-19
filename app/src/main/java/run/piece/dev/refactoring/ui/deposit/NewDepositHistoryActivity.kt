package run.piece.dev.refactoring.ui.deposit

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityNewDepositHistoryBinding
import run.piece.dev.refactoring.ui.deposit.fragment.HistoryAllFragment
import run.piece.dev.refactoring.ui.deposit.fragment.HistoryDepositFragment
import run.piece.dev.refactoring.ui.deposit.fragment.HistoryWithDrawFragment
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.bank.BankSelectActivity
import run.piece.domain.refactoring.deposit.model.HistoryItemVo

@AndroidEntryPoint
class NewDepositHistoryActivity : AppCompatActivity(R.layout.activity_new_deposit_history) {
    private lateinit var binding: ActivityNewDepositHistoryBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel: DepositViewModel by viewModels()
    private var displayType: String = ""
    private var searchDvn: String = "ALL"
    private var vranNo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewDepositHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.lifecycleOwner = this@NewDepositHistoryActivity
        binding.activity = this@NewDepositHistoryActivity
        binding.viewModel = viewModel
        coroutineScope = lifecycleScope

        window.apply {
            // 캡쳐방지 Kotlin Ver
            addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }
        binding.apply {

            when (getDisplayType(resources.displayMetrics)) {
                // 폴드 펼침
                "FOLD_DISPLAY_EXPAND" -> {
                    historyTab.isTabIndicatorFullWidth = false
                }

                else -> {
                    historyTab.isTabIndicatorFullWidth = true
                }
            }

            intent?.let {
                vranNo = it.getStringExtra("vranNo") ?: ""
            }

            backIvLayout.onThrottleClick {
                BackPressedUtil().activityFinish(this@NewDepositHistoryActivity, this@NewDepositHistoryActivity)
            }

            // onResume() 메서드에서 초기화 작업을 수행합니다.
            initialize()

            historyTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    handleTabSelection(tab)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })

        }

        BackPressedUtil().activityCreate(this@NewDepositHistoryActivity, this@NewDepositHistoryActivity)
        BackPressedUtil().systemBackPressed(this@NewDepositHistoryActivity, this@NewDepositHistoryActivity)
    }

    // 초기화 메서드에서 네트워크 요청을 병렬로 처리합니다.
    private fun initialize() {
        val networkJob = coroutineScope.launch(Dispatchers.IO) {
            viewModel.getHistory("v0.0.2", searchDvn, 1)
        }

        val displayTypeJob = coroutineScope.launch(Dispatchers.Default) {
            displayType = getDisplayType(resources.displayMetrics)
        }

        // 네트워크 요청 및 디스플레이 타입 측정이 모두 완료되면 UI를 업데이트합니다.
        coroutineScope.launch(Dispatchers.Main) {
            networkJob.join()
            displayTypeJob.join()

            this@NewDepositHistoryActivity.viewModel.historyList.collect { vo ->
                when (vo) {
                    is DepositViewModel.DepositHistoryState.Success -> {
                        LogUtil.v("거래내역 조회 성공 ${vo.data}")

                        updateUI(searchDvn, vo.data)
                    }

                    is DepositViewModel.DepositHistoryState.Failure -> {
                        LogUtil.v("거래내역 조회 실패 ${vo.message}")
                        binding.loading.visibility = View.GONE
                    }

                    else -> {
                        LogUtil.v("거래내역 조회 로딩 $vo")
                        binding.loading.visibility = View.VISIBLE
                    }
                }
            }



        }
    }

    private fun updateUI(type: String, historyItemVo: List<HistoryItemVo>) {
        binding.apply {
            // historyTab 설정 등의 UI 업데이트 작업을 수행합니다.

            val fragments = supportFragmentManager.fragments
            fragments.forEach { supportFragmentManager.beginTransaction().hide(it).commit() }

            when (type) {
                "ALL" -> {
                    coroutineScope.launch {
                        delay(300)
                        binding.loading.visibility = View.GONE

                        supportFragmentManager.commitNow {
                            setReorderingAllowed(true)
                            replace(R.id.history_all_fragment, HistoryAllFragment.newInstance(searchDvn,historyItemVo, vranNo))
                        }
                    }
                }

                "DEPOSIT" -> {
                    coroutineScope.launch {
                        delay(300)
                        binding.loading.visibility = View.GONE
                        supportFragmentManager.commitNow {
                            setReorderingAllowed(true)
                            replace(R.id.history_all_fragment, HistoryDepositFragment.newInstance(searchDvn,historyItemVo, vranNo))
                        }
                    }

                }

                "WITHDRAW" -> {
                    coroutineScope.launch {
                        delay(300)
                        binding.loading.visibility = View.GONE
                        supportFragmentManager.commitNow {
                            setReorderingAllowed(true)
                            replace(R.id.history_all_fragment, HistoryWithDrawFragment.newInstance(searchDvn,historyItemVo, vranNo))
                        }
                    }
                }
            }

        }
    }

    // 탭 선택 시 발생하는 이벤트를 처리하는 메서드를 개선합니다.
    private fun handleTabSelection(tab: TabLayout.Tab?) {
        when (tab?.position) {
            0 -> searchDvn = "ALL"
            1 -> searchDvn = "DEPOSIT"
            2 -> searchDvn = "WITHDRAW"
        }
        // 네트워크 요청을 병렬로 처리합니다.
        coroutineScope.launch(Dispatchers.IO) {
            viewModel.getHistory("v0.0.2", searchDvn, 1)
        }
    }

    // onDestroy() 메서드에서 CoroutineScope를 취소합니다.
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun getDisplayType(displayMetrics: DisplayMetrics): String {
        return when {
            displayMetrics.widthPixels > 1600 -> {
                "FOLD_DISPLAY_EXPAND"
            }

            displayMetrics.widthPixels < 980 -> {
                "FOLD_DISPLAY_COLLAPSE"
            }

            else -> {
                "BASIC_DISPLAY"
            }
        }
    }

    companion object {
        fun createNhAccount(context: Context, vranNo: String): Intent {
            val intent = Intent(context, BankSelectActivity::class.java)
            intent.putExtra("vranNo", vranNo)
            return intent
        }
    }
}

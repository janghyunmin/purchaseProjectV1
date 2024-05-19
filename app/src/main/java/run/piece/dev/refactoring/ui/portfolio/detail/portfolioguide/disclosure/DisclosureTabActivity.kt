package run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide.disclosure

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
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
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityDisclosureTabAllBinding
import run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata.DisclosureInvestmentFragment
import run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata.DisclosureManagementFragment
import run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata.DisclosureSearchActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.board.model.InvestmentDisclosureItemVo
import run.piece.domain.refactoring.board.model.ManagementDisclosureItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioBoardVo

@AndroidEntryPoint
class DisclosureTabActivity : AppCompatActivity(R.layout.activity_disclosure_tab_all) {
    private lateinit var binding: ActivityDisclosureTabAllBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel: DisclosureTabViewModel by viewModels()
    private var topTitle: String = "경영공시"
    private var displayType: String = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisclosureTabAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.lifecycleOwner = this@DisclosureTabActivity
        binding.activity = this@DisclosureTabActivity
        binding.vm = viewModel

        val networkConnection = NetworkConnection(this@DisclosureTabActivity)
        networkConnection.observe(this@DisclosureTabActivity) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this@DisclosureTabActivity, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        coroutineScope = lifecycleScope
        intent?.let {
            portfolioId = it.getStringExtra("portfolioId").toString()
        }

        binding.apply {
            window.apply {
                // 캡쳐방지 Kotlin Ver
                addFlags(WindowManager.LayoutParams.FLAG_SECURE);

                //상태바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }

            when(getDisplayType(resources.displayMetrics)) {
                // 폴드 펼침
                "FOLD_DISPLAY_EXPAND" -> {
                    binding.tabLayout.isTabIndicatorFullWidth = false
                }
                else -> {
                    binding.tabLayout.isTabIndicatorFullWidth = true
                }
            }

            backBtn.onThrottleClick {
                BackPressedUtil().activityFinish(this@DisclosureTabActivity,this@DisclosureTabActivity)
            }

            searchTouchLayout.onThrottleClick {
                startActivity(getSearchActivity(this@DisclosureTabActivity, portfolioId, topTitle))
            }

            CoroutineScope(Dispatchers.IO).launch {
                this@DisclosureTabActivity.viewModel.getBoard("경영공시", portfolioId = portfolioId, "",1,1)
            }


            CoroutineScope(Dispatchers.Main).launch {
                this@DisclosureTabActivity.viewModel.disclosureList.collect { vo ->
                    when (vo) {
                        is DisclosureTabViewModel.DisclosureListState.Success -> {

                            binding.tabLayout.getTabAt(0)?.select()
                            initView("경영공시",vo.disclosure.managementDisclosure.disclosure, vo.disclosure.investmentDisclosure.disclosure)

                            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onTabSelected(tab: TabLayout.Tab?) {
                                    when (tab?.position) {
                                        0 -> {
                                            binding.tabLayout.getTabAt(0)?.select()
                                            initView("경영공시",vo.disclosure.managementDisclosure.disclosure, vo.disclosure.investmentDisclosure.disclosure)
                                        }

                                        1 -> {
                                            binding.tabLayout.getTabAt(1)?.select()
                                            initView("투자공시",vo.disclosure.managementDisclosure.disclosure, vo.disclosure.investmentDisclosure.disclosure)
                                        }
                                    }
                                }

                                override fun onTabReselected(tab: TabLayout.Tab?) {}
                                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                            })
                        }

                        is DisclosureTabViewModel.DisclosureListState.Failure -> {
                            Log.v("공시 Fail : ", vo.message)
                        }

                        else -> {
                            Log.v("경영 공시 Loading : ", "$vo")
                        }
                    }
                }
            }

            backBtn.onThrottleClick {
                BackPressedUtil().activityFinish(this@DisclosureTabActivity,this@DisclosureTabActivity)
            }
        }


        BackPressedUtil().activityCreate(this@DisclosureTabActivity,this@DisclosureTabActivity)
        BackPressedUtil().systemBackPressed(this@DisclosureTabActivity,this@DisclosureTabActivity)
    }

    private fun initView(type: String, managementVo: List<ManagementDisclosureItemVo>, investmentVo: List<InvestmentDisclosureItemVo>) {
        val fragments = supportFragmentManager.fragments
        fragments.forEach { supportFragmentManager.beginTransaction().hide(it).commit() }

        when(type) {
            "경영공시" -> {
                supportFragmentManager.commitNow {
                    setReorderingAllowed(true)
                    replace(R.id.management, DisclosureManagementFragment.newInstance(managementVo, portfolioId,"N",""))
                }
            }
            "투자공시" -> {
                supportFragmentManager.commitNow {
                    setReorderingAllowed(true)
                    replace(R.id.investment, DisclosureInvestmentFragment.newInstance(investmentVo , portfolioId,"N",""))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun getDisplayType(displayMetrics: DisplayMetrics): String {
        return when {
            displayMetrics.widthPixels > 1600 -> {
                displayType = "FOLD_DISPLAY_EXPAND"
                displayType
            }

            displayMetrics.widthPixels < 980 -> {
                displayType = "FOLD_DISPLAY_COLLAPSE"
                displayType
            }

            else -> {
                displayType = "BASIC_DISPLAY"
                displayType
            }
        }
    }


    companion object {

        private var portfolioId: String = ""

        fun getIntent(context: Context, data: PortfolioBoardVo, portfolioId: String): Intent {
            val intent = Intent(context, DisclosureTabActivity::class.java)
            intent.putExtra("data", data)
            intent.putExtra("portfolioId", portfolioId)
            return intent
        }

        fun getIntent(context: Context,portfolioId: String): Intent {
            val intent = Intent(context, DisclosureTabActivity::class.java)
            intent.putExtra("portfolioId",portfolioId)
            return intent
        }

        // 검색 화면 이동
        fun getSearchActivity(context: Context, portfolioId: String, topTitle: String): Intent {
            val intent = Intent(context, DisclosureSearchActivity::class.java)
            intent.putExtra("topTitle", topTitle)
            intent.putExtra("portfolioId",portfolioId)
            return intent
        }
    }
}
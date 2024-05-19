package run.piece.dev.refactoring.ui.newinvestment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityInvestmentLoadingBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.investment.InvestMentPostState
import run.piece.dev.refactoring.ui.investment.InvestMentViewModel
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.view.fragment.FragmentHome
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.investment.model.InvestMentVo
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel

@AndroidEntryPoint
class InvestMentLoadingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvestmentLoadingBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel by viewModels<InvestMentViewModel>()
    private val dataStoreViewModel by viewModels<DataNexusViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                startActivity(FragmentHome.getNetWorkChkActivity(this))
            }
        }

        binding.lifecycleOwner = this@InvestMentLoadingActivity
        binding.activity = this@InvestMentLoadingActivity
        binding.viewModel = viewModel
        binding.dataStoreViewModel = dataStoreViewModel

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(Dispatchers.IO) {
                    try {
                        delay(1000)
                        val investScore = intent.getIntExtra("score",0).plus(50)
                        val investModel = InvestBodyModel(investScore, dataStoreViewModel.getVulnerable()) // 운영
//                        val investModel = InvestBodyModel(-30) // 점수 파라미터 hardcoding
                        viewModel.postInvestMent(investModel)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    try {
                        this@InvestMentLoadingActivity.viewModel.investMentResult.collect {
                            when(it) {
                                is InvestMentPostState.Success -> {
                                    LogUtil.e("투자 성향 분석 요청 성공 :${it.investMentVo.count}")

                                    val data = it.investMentVo
                                    dataStoreViewModel.putInvestResult(it.investMentVo.result)
                                    dataStoreViewModel.putInvestFinalScore(it.investMentVo.score)
                                    startActivity(getInvestMentResultActivity(this@InvestMentLoadingActivity,data))
                                    finish()
                                }
                                is InvestMentPostState.Failure -> {
                                    LogUtil.e("투자 성향 분석 요청 Fail { message: ${it.message}}")
                                    LogUtil.e("투자 성향 분석 요청 Fail errorVo : ${it.errorVo}")
                                    finish()
                                }
                                is InvestMentPostState.InvestException -> {
                                    LogUtil.e("투자 성향 Exception : ${it.errorVo.message}")
                                    App.EventBus.post("INVEST_ERROR")
                                    finish()
                                }
                                is InvestMentPostState.BaseException -> {
                                    LogUtil.e("투자 성향 Base Exception : ${it.baseVo?.message}")
                                    LogUtil.e("투자 성향 Base Exception : ${it.message}")
                                    App.EventBus.post("INVEST_ERROR")
                                    finish()
                                }
                                else -> {
                                    LogUtil.e("투자 성향 분석 요청 Loading .. $it")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        LogUtil.e("투자 성향 분석 요청 Main Exception : ${e.message}")
                        finish()
                    }
                }
            }
        }


        addOnBackPressedCallback()

        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
    }
    private fun addOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
    }

    companion object {
        fun getInvestMentResultActivity(context: Context, investMentVo: InvestMentVo): Intent {
            val intent = Intent(context, InvestMentResultActivity::class.java)
            intent.putExtra("data",investMentVo)
            return intent
        }
    }
}
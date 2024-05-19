package run.piece.dev.refactoring.ui.newinvestment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityInvestmentResultBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.investment.InvestMentViewModel
import run.piece.dev.refactoring.ui.investment.SsnState
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.fragment.FragmentHome
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.investment.model.InvestMentVo
import java.text.SimpleDateFormat
import java.util.Date


@AndroidEntryPoint
class InvestMentResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvestmentResultBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel by viewModels<InvestMentViewModel>()
    private val dataStoreViewModel by viewModels<DataNexusViewModel>()
    private var displayType: String = ""
    private var tempCount = 3 // 투자 성향 분석 카운트 횟수

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                startActivity(FragmentHome.getNetWorkChkActivity(this))
            }
        }

        binding.lifecycleOwner = this@InvestMentResultActivity
        binding.activity = this@InvestMentResultActivity
        binding.viewModel = viewModel
        binding.dataStoreViewModel = dataStoreViewModel

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(coroutineScope.coroutineContext + Dispatchers.Main) {

                    binding.retryBtn.bringToFront()
                    binding.goProduct.bringToFront()

                    binding.btnLayout.alpha = 0f
                    binding.rootLayout.alpha = 0f
                    binding.rootLayout.translationY = -10f // Initial position

                    ObjectAnimator.ofFloat(binding.btnLayout, View.ALPHA, 0f, 1f).apply {
                        duration = 300 // Adjust the duration as needed
                        interpolator = AccelerateDecelerateInterpolator()
                        start()
                    }

                    ObjectAnimator.ofFloat(binding.rootLayout, View.ALPHA, 0f, 1f).apply {
                        duration = 1300 // Adjust the duration as needed
                        interpolator = AccelerateDecelerateInterpolator()
                        start()
                    }

                    ObjectAnimator.ofFloat(binding.rootLayout, View.TRANSLATION_Y, 90f, 0f).apply {
                        duration = 1300 // Adjust the duration as needed
                        interpolator = AccelerateDecelerateInterpolator()
                        start()
                    }

                    // display width 에 따른 tool_tip_layout 위치 변경
                    getDisplayType(resources.displayMetrics)
                    intent?.let {
                        val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            it.getParcelableExtra("data", InvestMentVo::class.java)
                        } else {
                            it.getParcelableExtra("data")
                        }

                        binding.apply {
                            resultTitleTv.text = String.format(
                                this@InvestMentResultActivity.getString(R.string.investment_result_top_title),
                                data?.name,
                                data?.result
                            )

                            when (data?.result) {
                                getString(R.string.invest_result_1) -> {
                                    updateTextColor(resultTitleTv, R.color.c_f95622, data.name, data.result)
                                    Glide.with(this@InvestMentResultActivity).load(R.drawable.icon_x40_fire).into(smallIv)
                                }

                                getString(R.string.invest_result_2) -> {
                                    updateTextColor(resultTitleTv, R.color.c_ff8f3d, data.name, data.result)
                                    Glide.with(this@InvestMentResultActivity).load(R.drawable.icon_x40_electric).into(smallIv)
                                }

                                getString(R.string.invest_result_3) -> {
                                    updateTextColor(resultTitleTv, R.color.c_03a0a2, data.name, data.result)
                                    Glide.with(this@InvestMentResultActivity).load(R.drawable.icon_x40_clova).into(smallIv)
                                }

                                getString(R.string.invest_result_4) -> {
                                    updateTextColor(resultTitleTv, R.color.c_43b55c, data.name, data.result)
                                    Glide.with(this@InvestMentResultActivity).load(R.drawable.icon_x40_apple).into(smallIv)
                                }

                                getString(R.string.invest_result_5) -> {
                                    updateTextColor(resultTitleTv, R.color.c_368ded, data.name, data.result)
                                    Glide.with(this@InvestMentResultActivity).load(R.drawable.icon_x40_water).into(smallIv)
                                }
                            }

                            // 투자 성향 분석 결과 이미지
                            Glide.with(this@InvestMentResultActivity).load(data?.resultImagePath).into(resultIv)


                            // %1$s은 이런 투자 성향을 가져요
                            descriptionTitleTv.text = String.format(this@InvestMentResultActivity.getString(R.string.investment_result_description_title), data?.result)

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                descriptionContentTv.text = Html.fromHtml(data?.description,Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                @Suppress("DEPRECATION")
                                descriptionContentTv.text = Html.fromHtml(data?.description)
                            }


                            // 이런 금융 상품에 관심이 많아요 ( 공격 투자형 && 적극 투자형 두가지만 화면 출력 )
                            if (data?.interestProductDescription.isNullOrEmpty()) {
                                interestProductDescriptionLayout.visibility = View.GONE
                            } else {
                                interestProductDescriptionLayout.visibility = View.VISIBLE
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    productContentTv.text = Html.fromHtml(data?.interestProductDescription.default(), Html.FROM_HTML_MODE_LEGACY)
                                } else {
                                    @Suppress("DEPRECATION")
                                    productContentTv.text = Html.fromHtml(data?.interestProductDescription.default())
                                }

                            }


                            // 취약 투자자 최종 결과값 : "Y" : "N"
                            this@InvestMentResultActivity.dataStoreViewModel.putFinalVulnerable(data?.isVulnerableInvestors.default())


                            var nowDate: String = "yyyyMMdd".getTimeNow()

                            // 데이터에 저장된 날짜와 오늘 날짜가 같으면
                            if(data?.createdAt == nowDate) {
                                if (data.count <= 2) {
                                    retryCountTv.text = String.format(this@InvestMentResultActivity.getString(R.string.investment_retry_count_text), tempCount - data.count)
                                    retryBtn.setTextColor(ContextCompat.getColor(this@InvestMentResultActivity, R.color.c_10cfc9))
                                    retryBtn.background = ContextCompat.getDrawable(this@InvestMentResultActivity, R.drawable.layout_round_10cfc9_10dp)
                                    binding.retryBtn.onThrottleClick {
                                        //startActivity(getInvestRetryActivity(this@InvestMentResultActivity, dataStoreViewModel!!.getName()))
                                        dataStoreViewModel?.let { model ->
                                            startActivity(InvestmentIntroActivity.getIntent(this@InvestMentResultActivity, model.getName()))
                                        } ?: run {
                                            startActivity(InvestmentIntroActivity.getIntent(this@InvestMentResultActivity, ""))
                                        }
                                    }
                                } else {
                                    retryCountTv.text = getString(R.string.investment_retry_not_available_text)
                                    LogUtil.e("툴팁 타입 : $displayType")
                                    when (displayType) {
                                        // 기본 디스플레이
                                        "BASIC_DISPLAY" -> {
                                            LogUtil.e("ddd : ${basicToolTipLeftMargins()}")
                                            val widthValue = ConstraintLayout.LayoutParams.WRAP_CONTENT
                                            val heightValue = ConstraintLayout.LayoutParams.WRAP_CONTENT
                                            val layoutParams = ConstraintLayout.LayoutParams(
                                                widthValue,
                                                heightValue
                                            ).apply {
                                                setMargins(
                                                    basicToolTipLeftMargins(),
                                                    0,
                                                    0,
                                                    0
                                                )
                                                topToTop = toolTipTopToTop()
                                                bottomToTop = toolTipBottomToTopOf()
                                                startToStart = toolTipStartStart()
                                                endToEnd = toolTipEndToEnd()
                                            }

                                            binding.toolTipLayout.layoutParams = layoutParams
                                        }
                                        // 폴드 펼침
                                        "FOLD_DISPLAY_EXPAND" -> {}
                                        // 폴드 접힘
                                        "FOLD_DISPLAY_COLLAPSE" -> {
                                            val widthValue = ConstraintLayout.LayoutParams.WRAP_CONTENT
                                            val heightValue = ConstraintLayout.LayoutParams.WRAP_CONTENT
                                            val layoutParams = ConstraintLayout.LayoutParams(
                                                widthValue,
                                                heightValue
                                            ).apply {
                                                setMargins(
                                                    toolTipLeftMargins(),
                                                    0,
                                                    0,
                                                    0
                                                )
                                                topToTop = toolTipTopToTop()
                                                bottomToTop = toolTipBottomToTopOf()
                                                startToStart = toolTipStartStart()
                                                endToEnd = toolTipEndToEnd()
                                            }

                                            binding.toolTipLayout.layoutParams = layoutParams

                                        }
                                    }
                                    retryBtn.setTextColor(ContextCompat.getColor(this@InvestMentResultActivity, R.color.c_dadce3))
                                    retryBtn.background = ContextCompat.getDrawable(this@InvestMentResultActivity, R.drawable.layout_round_border_f2f3f4_10dp)
                                    binding.retryBtn.onThrottleClick { LogUtil.e("분석 가능한 횟수 모두 소진") }
                                }
                            }

                            // 만약, 오늘 날짜와 서버에서 주는 날짜가 다르면 "나의 투자성향은?" & 성향분석 UI를 출력한다.
                            else {
                                retryCountTv.text = String.format(this@InvestMentResultActivity.getString(R.string.investment_retry_count_text), tempCount)
                                retryBtn.setTextColor(ContextCompat.getColor(this@InvestMentResultActivity, R.color.c_10cfc9))
                                retryBtn.background = ContextCompat.getDrawable(this@InvestMentResultActivity, R.drawable.layout_round_10cfc9_10dp)

                                binding.retryBtn.onThrottleClick {
                                    //startActivity(getInvestRetryActivity(this@InvestMentResultActivity, dataStoreViewModel!!.getName()))
                                    dataStoreViewModel?.let { model ->
                                        startActivity(InvestmentIntroActivity.getIntent(this@InvestMentResultActivity, model.getName()))
                                    } ?: run {
                                        startActivity(InvestmentIntroActivity.getIntent(this@InvestMentResultActivity, ""))
                                    }
                                }
                            }
                        }
                    }


                    launch(Dispatchers.IO) {
                        viewModel.ssnCheck()
                    }
                    launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                        try {
                            this@InvestMentResultActivity.viewModel.ssnChk.collect { ssnData ->
                                when (ssnData) {
                                    is SsnState.Success -> {
                                        ssnYn = ssnData.isSuccess.ssnYn
                                        LogUtil.e("회원 실명 인증 조회 성공 : ${ssnData.isSuccess.ssnYn}")
                                    }

                                    is SsnState.Failure -> {
                                        LogUtil.e("회원 실명 인증 조회 실패 : ${ssnData.message}")
                                    }

                                    else -> {
                                        LogUtil.e("회원 실명 인증 조회 Loading : $ssnData")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            LogUtil.e("회원 실명인증 Exception: ${e.message}")
                        }
                    }

                    binding.goProduct.onThrottleClick {
                        LogUtil.e("메인으로 이동")
                        startActivity(getMainActivity(this@InvestMentResultActivity, ssnYn))
                        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
                        finishAffinity()
                    }
                }
            }
        }

        // 뒤로가기 Callback
        addOnBackPressedCallback()


        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
    }




    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
    }

    private fun addOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(getMainActivity(this@InvestMentResultActivity, ssnYn))
                finishAffinity()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
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

    private fun basicToolTipLeftMargins(): Int {
        return viewModel.dpToPixels(16f)
    }

    private fun toolTipLeftMargins(): Int {
        return viewModel.dpToPixels(32f)
    }

    private fun toolTipTopToTop(): Int {
        return ConstraintLayout.LayoutParams.UNSET
    }

    private fun toolTipStartStart(): Int {
        with(binding) {
            return retryBtn.id
        }
    }

    private fun toolTipEndToEnd(): Int {
        with(binding){
            return retryBtn.id
        }
    }

    private fun toolTipBottomToTopOf(): Int {
        with(binding){
            return retryBtn.id
        }
    }

    @SuppressLint("StringFormatMatches")
    fun updateTextColor(textView: AppCompatTextView, colorResourceId: Int, name: String, result: String) {
        val fullText = getString(R.string.investment_result_top_title, name, result)

        // Create a SpannableString to apply color to data?.result
        val spannable = SpannableString(fullText)
        val textColor = ContextCompat.getColor(this@InvestMentResultActivity, colorResourceId)

        // Calculate the start and end indices for data?.result
        val startIndex = fullText.indexOf(result)
        val endIndex = startIndex + (result.length ?: 0)

        // Set the color to the specified portion
        spannable.setSpan(ForegroundColorSpan(textColor), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the formatted text with colored data?.result to the TextView
        textView.text = spannable
    }

    @SuppressLint("SimpleDateFormat")
    fun String.getTimeNow(): String {
        return try {
            val date = Date(System.currentTimeMillis())
            val simpleDateFormat = SimpleDateFormat(this)
            simpleDateFormat.format(date)
        } catch (e: Exception) {
            LogUtil.e("message : ${e.message}")
            ""
        }
    }


    companion object {
        var ssnYn: String = ""
        fun getMainActivity(context: Context, ssnYn: String): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("ssnYn", ssnYn)
            intent.putExtra("backStack","N")
            return intent
        }
    }

}

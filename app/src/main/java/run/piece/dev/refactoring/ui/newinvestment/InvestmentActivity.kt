package run.piece.dev.refactoring.ui.newinvestment

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.ActivityInvestmentBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.newinvestment.InvestMentLoadingActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.utils.ToggleAnimation
import run.piece.domain.refactoring.investment.model.InvestmentQuestionVo

@AndroidEntryPoint
class InvestmentActivity : AppCompatActivity() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: ActivityInvestmentBinding

    private val viewModel: InvestmentViewModel by viewModels()

    private val dataStoreViewModel by viewModels<DataNexusViewModel>()

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //취약 투자자 유의사항 설명 확인서 화면 종료시
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@InvestmentActivity
            activity = this@InvestmentActivity
            vm = viewModel
        }

        coroutineScope = lifecycleScope

        coroutineScope.launch {
            launch(coroutineScope.coroutineContext + Dispatchers.IO) {
                viewModel.getInvestmentList()
            }.join()

            /**
             * case nomal 취약투자자인 경우
             * case survey 취약투자자가 아닌 경우
             **/
            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                viewModel.investMentGetList.collect {
                    when (it) {
                        is InvestmentViewModel.InvestmentGetState.Success -> {
                            when(viewModel.startType) {
                                "nomal" -> {
                                    viewModel.totalSize = it.investmentQuestionVo.size //11개

                                    viewModel.questionList = it.investmentQuestionVo
                                    viewModel.progressCount = viewModel.questionList.size - 1 // 10개
                                }
                                "survey" -> {
                                    viewModel.totalSize = it.investmentQuestionVo.size // 10개

                                    viewModel.questionList = it.investmentQuestionVo.filter { vo ->
                                        vo.displayOrder != 1
                                    }
                                    viewModel.progressCount = viewModel.questionList.size // 10개
                                }
                            }

                            viewModel.requestIndex = 0

                            settingView(vo = viewModel.questionList[viewModel.requestIndex], isBack = false)
                        }

                        is InvestmentViewModel.InvestmentGetState.Failure -> {}

                        else -> {}
                    }

                    binding.loading.visibility = when (it) {
                        is InvestmentViewModel.InvestmentGetState.Success -> View.GONE
                        is InvestmentViewModel.InvestmentGetState.Failure -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                viewModel.investmentPost.collect {
                    when (it) {
                        is InvestmentViewModel.InvestMentPostState.Success -> {

                        }

                        is InvestmentViewModel.InvestMentPostState.Failure -> {}

                        else -> {}
                    }
                }
            }
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showClosePopup()
            }
        })

        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
    }

    private fun settingView(vo: InvestmentQuestionVo, isBack: Boolean = false) {
        if (vo.displayOrder == 1) {
            //취약 투자자 설문인 경우...
            binding.iconLayout.visibility = View.VISIBLE
            binding.investmentRv.visibility = View.GONE
            showButton("yesNo", true)
            showButton("next", false)
            showButton("result", false)
        } else if(vo.displayOrder == viewModel.totalSize) {
            //마지막 설문일 경우...
            binding.iconLayout.visibility = View.GONE
            binding.investmentRv.visibility = View.VISIBLE
            showButton("result", true)
            showButton("yesNo", false)
            showButton("next", false)

        } else {
            //취약 투자자 설문이 아닌 경우...
            binding.iconLayout.visibility = View.GONE
            binding.investmentRv.visibility = View.VISIBLE
            showButton("yesNo", false)
            showButton("result", false)

            if (vo.isMultiple == "Y") {
                if (vo.answers.filter { it.isSelected }.isNotEmpty()) {
                    binding.nextTv.setBackgroundColor(ContextCompat.getColor(this@InvestmentActivity, R.color.p500_10CFC9))
                    binding.nextTv.setTextColor(ContextCompat.getColor(this@InvestmentActivity, R.color.white))
                    binding.nextCv.isClickable = true
                    showButton("next", true) //선택된 아이템이 있으면 버튼 활성화
                } else {
                    binding.nextTv.setBackgroundColor(ContextCompat.getColor(this@InvestmentActivity, R.color.g400_DADCE3))
                    binding.nextTv.setTextColor(ContextCompat.getColor(this@InvestmentActivity, R.color.white))
                    binding.nextCv.isClickable = false
                    showButton("next", true) //선택된 아이템이 없으면 버튼 비활성화
                }
            } else {
                binding.nextTv.setBackgroundColor(ContextCompat.getColor(this@InvestmentActivity, R.color.p500_10CFC9))
                binding.nextTv.setTextColor(ContextCompat.getColor(this@InvestmentActivity, R.color.white))
                binding.nextCv.isClickable = true

                if (vo.answers.filter { it.isSelected }.isNotEmpty()) {
                    showButton("next", true) //선택된 아이템이 있으면 show
                } else {
                    showButton("next", false) //선택된 아이템이 없으면 hide
                }
            }
        }

        ghostingAnim(vo, isBack)

        when(viewModel.startType) {
            "nomal" -> {
                if (viewModel.requestIndex > 0) binding.progressBar.visibility = View.VISIBLE
                else  binding.progressBar.visibility = View.GONE
            }
            "survey" -> { // 바로 시작...
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        //공지 데이터가 있는 경우
        binding.warningGroup.visibility = if (vo.description.isBlank()) View.GONE else View.VISIBLE
        binding.warningTv.text = vo.description

        //다중 선택
        if (vo.isMultiple == "Y") {
            when(viewModel.startType) {
                "nomal", "survey" -> {
                    val progressUnit = if (viewModel.startType == "survey") {
                        100 / viewModel.progressCount * (viewModel.requestIndex + 1)
                    } else {
                        100 / viewModel.progressCount * viewModel.requestIndex
                    }

                    ObjectAnimator.ofInt(binding.progressBar, "progress", binding.progressBar.progress, progressUnit).apply {
                        duration = 200
                        start()
                    }

                    binding.investmentRv.apply {
                        adapter = InvestmentRvAdapter(this@InvestmentActivity, viewModel, callBackEvent = { status, layoutPosition, data, adapter, answerTv, arrowIv, cardView ->
                            when(status) {
                                "bind" -> {}
                                "click" -> {
                                    //다중선택의 경우 선택한 아이템 중에 제일 큰 값을 사용헤야함
                                    val isSelected = !data.isSelected
                                    data.isSelected = isSelected

                                    if (data.isSelected) {
                                        adapter.investMentTextAnim(answerTv)
                                        ToggleAnimation.alphaAnimIv(context = context, arrowIv, "VISIBLE")

                                        arrowIv.visibility = View.VISIBLE
                                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                                        cardView.strokeWidth = viewModel.dpToPixel(2)
                                    } else {
                                        adapter.investMentTextAnimHide(answerTv)
                                        ToggleAnimation.alphaAnimIv(context = context, arrowIv, "GONE")

                                        arrowIv.visibility = View.GONE
                                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g200_F2F3F4))
                                        cardView.strokeWidth = viewModel.dpToPixel(0)
                                    }

                                    if (vo.answers.filter { it.isSelected }.isNotEmpty()) {
                                        binding.nextTv.setBackgroundColor(ContextCompat.getColor(this@InvestmentActivity, R.color.p500_10CFC9))
                                        binding.nextTv.setTextColor(ContextCompat.getColor(this@InvestmentActivity, R.color.white))
                                        binding.nextCv.isClickable = true
                                    } else {
                                        binding.nextTv.setBackgroundColor(ContextCompat.getColor(this@InvestmentActivity, R.color.g400_DADCE3))
                                        binding.nextTv.setTextColor(ContextCompat.getColor(this@InvestmentActivity, R.color.white))
                                        binding.nextCv.isClickable = false
                                    }
                                }
                            }
                        })
                        (adapter as InvestmentRvAdapter).submitList(vo.answers)
                        itemAnimator = null
                    }
                }
            }

            //단일 선택
        } else {
            when(viewModel.startType) {
                "nomal", "survey" -> {
                    val progressUnit = if (viewModel.startType == "survey") {
                        100 / viewModel.progressCount * (viewModel.requestIndex + 1)
                    } else {
                        100 / viewModel.progressCount * viewModel.requestIndex
                    }

                    ObjectAnimator.ofInt(binding.progressBar, "progress", binding.progressBar.progress, progressUnit).apply {
                        duration = 200
                        start()
                    }

                    binding.investmentRv.apply {
                        adapter = InvestmentRvAdapter(this@InvestmentActivity, viewModel, callBackEvent = { status, layoutPosition, data, adapter, answerTv, arrowIv, cardView ->
                            when(status) {
                                "bind" -> {}
                                "click" -> {
                                    if (getSingleAnswerPosition() == - 1) {
                                        //최초 클릭 button of
                                        val isSelected = !data.isSelected
                                        data.isSelected = isSelected

                                        adapter.investMentTextAnim(answerTv)
                                        ToggleAnimation.alphaAnimIv(context = context, arrowIv, "VISIBLE")

                                        arrowIv.visibility = View.VISIBLE
                                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                                        cardView.strokeWidth = viewModel.dpToPixel(2)

                                        setSingleAnswerPosition(layoutPosition)

                                        coroutineScope.launch(Dispatchers.Main) {
                                            delay(400)
                                            nextPage()
                                        }
                                    } else {
                                        //다시 클릭 button on
                                        if (getSingleAnswerPosition() != layoutPosition) {
                                            //클릭되지 않은 포지션
                                            val isSelected = !data.isSelected
                                            data.isSelected = isSelected

                                            adapter.investMentTextAnim(answerTv)
                                            ToggleAnimation.alphaAnimIv(context = context, arrowIv, "GONE")

                                            arrowIv.visibility = View.GONE
                                            cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g200_F2F3F4))
                                            cardView.strokeWidth = viewModel.dpToPixel(0)

                                            val viewHolder = binding.investmentRv.findViewHolderForAdapterPosition(getSingleAnswerPosition())
                                            val view = viewHolder?.itemView?.findViewById<AppCompatTextView>(R.id.answer_tv)

                                            view?.let {
                                                setSingleDisableEtcPosition(adapter, view, getSingleAnswerPosition(), layoutPosition)
                                                adapter.notifyDataSetChanged()
                                            }
                                        } else {
                                            //이미 클릭된 포지션 - 아무것도 실행하지 않는다.
                                        }
                                    }
                                }
                            }
                        })
                        (adapter as InvestmentRvAdapter).submitList(vo.answers)
                        itemAnimator = null
                    }
                }
            }
        }
    }

    fun showClosePopup() {
        AppConfirmDF.newInstance(
            getString(R.string.investment_modal_title),
            getString(R.string.investment_modal_content),
            false,
            positiveStrRes = R.string.investment_modal_btn_continue_title,
            positiveAction = {
            },
            negativeStrRes = R.string.investment_modal_btn_stop_title,
            negativeAction = {
                startActivity(Intent(this@InvestmentActivity, MainActivity::class.java))
                finishAffinity()
            },
            dismissAction = {},
        ).show(supportFragmentManager, "investmentClose")
    }


    /**
     * case yesNo : 예/아니오 버튼
     * case next : 다음 버튼
     * case result : 분석 결과 보기 버튼
     **/
    private fun showButton(type: String, status: Boolean) {
        when(type) {
            "yesNo" -> {
                if (status) binding.okCancelGroup.visibility = View.VISIBLE
                else binding.okCancelGroup.visibility = View.GONE
            }
            "next" -> {
                if (status) binding.nextCv.visibility = View.VISIBLE
                else binding.nextCv.visibility = View.GONE

                if (status) {
                    ObjectAnimator.ofFloat(binding.nextCv, "translationY", 0F, - viewModel.dpToPixel(48).toFloat()).apply {
                        duration = 500L
                    }.start()
                }
            }
            "result" -> {
                if (status) binding.resultCv.visibility = View.VISIBLE
                else binding.resultCv.visibility = View.GONE

                if (status) {
                    ObjectAnimator.ofFloat(binding.resultCv, "translationY", 0F, - viewModel.dpToPixel(48).toFloat()).apply {
                        duration = 500L
                    }.start()
                }
            }
        }
    }

    // 선택한 answer의 포지션을 저장
    private fun setSingleAnswerPosition(position: Int) {
        viewModel.questionList[viewModel.requestIndex].answerPosition = position
    }

    // 선택했던 answer의 포지션을 반환
    private fun getSingleAnswerPosition(): Int = viewModel.questionList[viewModel.requestIndex].answerPosition

    // 전달 받은 포지션을 제외한 모든 포지션을 false로 변경하고
    // 전달 받은 포지션을 저장 및 포지션을 true로 변경
    fun setSingleDisableEtcPosition(adapter: InvestmentRvAdapter, textView:AppCompatTextView, selectedPosition: Int, newPosition: Int) {
        viewModel.questionList[viewModel.requestIndex].answers.forEachIndexed { index, vo ->
            if (index != newPosition) {
                if (index == selectedPosition) {
                    adapter.investMentTextAnimHide(textView)
                }
                viewModel.questionList[viewModel.requestIndex].answers[index].isSelected = false
            } else {
                viewModel.questionList[viewModel.requestIndex].answerPosition = newPosition
                viewModel.questionList[viewModel.requestIndex].answers[index].isSelected = true
            }
        }
    }

    fun goSubSurvey() {
        //서브 질문으로 이동
        resultLauncher.launch(InvestmentSubSurveyActivity.getIntent(this@InvestmentActivity))
        dataStoreViewModel.putVulnerable("Y")
    }

    fun startInvestment() {
        viewModel.requestIndex = 1
        settingView(vo = viewModel.questionList[viewModel.requestIndex], isBack = false)
        dataStoreViewModel.putVulnerable("N")
    }

    fun goBackStack() {
        when(viewModel.startType) {
            "nomal" -> { //취약투자자-아니오
                if (viewModel.requestIndex == 0) {
                    finish()
                } else if (viewModel.requestIndex >= 1) {
                    viewModel.requestIndex -= 1

                    settingView(vo = viewModel.questionList[viewModel.requestIndex], isBack = true)
                }
            }
            "survey" -> { // 취약투자자-예
                if (viewModel.requestIndex == 0) {
                    finish()
                } else if (viewModel.requestIndex >= 1) {
                    viewModel.requestIndex -= 1

                    settingView(vo = viewModel.questionList[viewModel.requestIndex], isBack = true)
                }
            }
        }
    }

    fun nextPage() {
        when(viewModel.requestIndex) {
            in 0 until viewModel.questionList.size - 1 -> {
                if (viewModel.requestIndex <= viewModel.questionList.size - 1) {
                    viewModel.requestIndex += 1
                }
                settingView(vo = viewModel.questionList[viewModel.requestIndex], isBack = false)
            }
            else -> {
                //마지막 페이지
                binding.resultTv.setBackgroundColor(ContextCompat.getColor(this@InvestmentActivity, R.color.p500_10CFC9))
                binding.resultTv.setTextColor(ContextCompat.getColor(this@InvestmentActivity, R.color.white))

                binding.resultCv.onThrottleClick ({
                    Log.d("투자성향분석_클릭된_아이템_정보_최종", "${viewModel.getTotalScore()}")
                    dataStoreViewModel.putInvestScore(viewModel.getTotalScore()) // 내부 저장소 저장
                    val intent = Intent(this@InvestmentActivity, InvestMentLoadingActivity::class.java)
                    intent.putExtra("score", viewModel.getTotalScore())
                    startActivity(intent)
                }, 1000)
            }
        }
    }

    //잔상 애니메이션
    private fun ghostingAnim(vo : InvestmentQuestionVo, isBack: Boolean) {
        binding.questionTitleTv.text = vo.question

        if (viewModel.requestIndex >= 1) {
            if (!isBack) {
                binding.questionTitleTv.alpha = 1F
                binding.questionTitleBackTv.alpha = 0F

                binding.investmentRv.alpha = 0.2F

                binding.questionTitleTv.text = viewModel.questionList[viewModel.requestIndex - 1].question
                binding.questionTitleBackTv.text = viewModel.questionList[viewModel.requestIndex].question

                binding.questionTitleBackTv.animate().alpha(1f).setDuration(500).start()
                binding.questionTitleTv.animate().alpha(0f).setDuration(500).start()

                binding.investmentRv.animate().alpha(1f).setDuration(800).start()
            } else {
                binding.questionTitleTv.alpha = 0F
                binding.questionTitleBackTv.alpha = 1F

                // 뒤로 진행되는 상태
                binding.questionTitleTv.text = viewModel.questionList[viewModel.requestIndex].question
                binding.questionTitleBackTv.text = viewModel.questionList[viewModel.requestIndex + 1].question

                binding.questionTitleTv.animate().alpha(1f).setDuration(500).start()
                binding.questionTitleBackTv.animate().alpha(0f).setDuration(500).start()
            }
        } else {
            binding.questionTitleTv.alpha = 1F
            binding.questionTitleBackTv.alpha = 0F

            binding.questionTitleTv.text = viewModel.questionList[viewModel.requestIndex].question
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
    }
    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object {
        fun getIntent(context: Context, type: String): Intent {
            val intent = Intent(context, InvestmentActivity::class.java)
            intent.putExtra("type", type)
            return intent
        }
    }
}
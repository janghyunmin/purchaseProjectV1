package run.piece.dev.refactoring.ui.question

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityQuestionBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.common.vo.CommonFaqVo

@AndroidEntryPoint
class QuestionActivity : AppCompatActivity(R.layout.activity_question) {
    private lateinit var binding: ActivityQuestionBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel: QuestionViewModel by viewModels()
    private lateinit var questionRvAdapter: QuestionRvAdapter
    private val tabItemList = ArrayList<CommonFaqVo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionRvAdapter = QuestionRvAdapter(this@QuestionActivity, viewModel)

        App()

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            tabItemList.clear()
            coroutineScope = lifecycleScope
            questionRv.apply {
                layoutManager = LinearLayoutManager(this@QuestionActivity, RecyclerView.VERTICAL, false)
                adapter = questionRvAdapter
                itemAnimator = null
            }

            coroutineScope.launch(Dispatchers.IO) {
                viewModel.getFaqTabList()
                getQuestionList(boardCategory = "")
            }

            coroutineScope.launch(Dispatchers.Main) {
                viewModel.faqTabList.collect { vo ->
                    when(vo) {
                        is QuestionViewModel.FaqTabListState.Success -> {

                            tabItemList.add(
                                CommonFaqVo(
                                    "",
                                    "BRT03",
                                    "전체",
                                    "0"
                                )
                            )

                            vo.faqTabList.forEach { tabItem ->
                                tabItemList.add(
                                    CommonFaqVo(
                                        tabItem.codeId,
                                        tabItem.upperCodeId,
                                        tabItem.codeName,
                                        tabItem.displayOrder
                                    )
                                )
                            }

                            tabItemList.forEach { item ->
                                binding.tabs.addTab(binding.tabs.newTab().setText(item.codeName))
                            }
                        }
                        is QuestionViewModel.FaqTabListState.Failure -> {

                        }
                        else -> {}
                    }
                }
            }




            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if(tab != null) {
                        tabItemList.forEach {
                            Log.v("tabItemList : " ,"${it.codeName}")
                            if(it.codeName == tab.contentDescription) {
                                Log.v("자주 묻는 질문 탭 클릭 ", tab.contentDescription.toString())
                                viewModel.tabType = it.codeName
                                getQuestionList(it.codeId)
                            }
                        }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })

            backImg.onThrottleClick {
                BackPressedUtil().activityFinish(this@QuestionActivity,this@QuestionActivity)
            }
        }



        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        BackPressedUtil().activityCreate(this@QuestionActivity,this@QuestionActivity)
        BackPressedUtil().systemBackPressed(this@QuestionActivity,this@QuestionActivity)
    }

    fun getQuestionList(boardCategory: String) {
        coroutineScope.launch(Dispatchers.IO) {
            viewModel.getQuestionList(boardCategory = boardCategory)
        }

        coroutineScope.launch(Dispatchers.Main) {
            viewModel.questionList.collect {
                when (it) {
                    is QuestionViewModel.QuestionListState.Success -> {
                        questionRvAdapter.submitData(it.questionList)
                    }
                    is QuestionViewModel.QuestionListState.Failure -> {}
                    else -> {}
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, QuestionActivity::class.java)
        }
    }
}
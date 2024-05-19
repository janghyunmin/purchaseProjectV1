package run.piece.dev.refactoring.ui.faq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.databinding.ActivityFaqTabBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick

@AndroidEntryPoint
class FaqTabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaqTabBinding
    private val viewModel: FaqTabViewModel by viewModels()

    private lateinit var faqTabRvAdapter: FaqRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        faqTabRvAdapter = FaqRvAdapter(this@FaqTabActivity, viewModel)

        binding.apply {
            activity = this@FaqTabActivity
            vm = viewModel

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> {
                            CoroutineScope(Dispatchers.IO).launch { getFaqList(boardCategory = "") }
                        }
                        1 -> {
                            CoroutineScope(Dispatchers.IO).launch { getFaqList(boardCategory = "BRT0601") }
                        }
                        2 -> {
                            CoroutineScope(Dispatchers.IO).launch { getFaqList(boardCategory = "BRT0602") }
                        }
                        3 -> {
                            CoroutineScope(Dispatchers.IO).launch { getFaqList(boardCategory = "BRT0603") }
                        }
                        4 -> {
                            CoroutineScope(Dispatchers.IO).launch { getFaqList(boardCategory = "BRT0604") }
                        }
                        5 -> {
                            CoroutineScope(Dispatchers.IO).launch { getFaqList(boardCategory = "BRT0605") }
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            faqRv.apply {
                binding.loading.visibility = View.GONE
                layoutManager = LinearLayoutManager(this@FaqTabActivity, RecyclerView.VERTICAL, false)
                adapter = faqTabRvAdapter

                itemAnimator = null
            }

            backBtn.onThrottleClick {
                BackPressedUtil().activityFinish(this@FaqTabActivity,this@FaqTabActivity)
            }
        }

        lifecycleScope.launch {
            launch(Dispatchers.IO) {
                getFaqList(boardCategory = "")
            }
        }

        window?.apply {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        BackPressedUtil().activityCreate(this@FaqTabActivity,this@FaqTabActivity)
        BackPressedUtil().systemBackPressed(this@FaqTabActivity,this@FaqTabActivity)
    }

    fun getFaqList(boardCategory: String) {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getFaqList(boardCategory = boardCategory)
        }
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.faqList.collect {
                when (it) {
                    is FaqTabViewModel.FaqListState.Success -> {
                        faqTabRvAdapter.submitData(it.faqList)
                    }

                    is FaqTabViewModel.FaqListState.Failure -> {

                    }

                    else -> {}
                }
            }
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, FaqTabActivity::class.java)
            return intent
        }
    }
}



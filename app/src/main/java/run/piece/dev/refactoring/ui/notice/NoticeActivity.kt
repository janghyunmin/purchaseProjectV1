package run.piece.dev.refactoring.ui.notice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityNoticeBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection

@AndroidEntryPoint
class NoticeActivity : AppCompatActivity(R.layout.activity_notice){
    private lateinit var binding: ActivityNoticeBinding
    private val viewModel: NoticeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        val noticeRvAdapter = NoticeRvAdapter(this@NoticeActivity, viewModel)

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            backImg.onThrottleClick {
                BackPressedUtil().activityFinish(this@NoticeActivity,this@NoticeActivity)
            }

            noticeRv.apply {
                layoutManager = LinearLayoutManager(this@NoticeActivity, RecyclerView.VERTICAL, false)
                adapter = noticeRvAdapter

                itemAnimator = null
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch(Dispatchers.IO) {
                    viewModel.getNoticeList()
                }

                launch (Dispatchers.Main) {
                    viewModel.noticeList.collect {
                        when (it) {
                            is NoticeViewModel.NoticeListState.Success -> {
                                noticeRvAdapter.submitData(it.noticeList)
                            }

                            is NoticeViewModel.NoticeListState.Failure -> {

                            }

                            else -> {}
                        }
                    }
                }
            }
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE) //캡처 방지
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        BackPressedUtil().activityCreate(this@NoticeActivity,this@NoticeActivity)
        BackPressedUtil().systemBackPressed(this@NoticeActivity,this@NoticeActivity)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, NoticeActivity::class.java)
        }
    }
}
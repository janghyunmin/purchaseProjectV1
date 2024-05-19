package run.piece.dev.refactoring.ui.event

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityEventBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection

// 이벤트 리스트 Activity
@AndroidEntryPoint
class EventActivity : AppCompatActivity(R.layout.activity_event) {
    private lateinit var binding: ActivityEventBinding
    private lateinit var eventRvAdapter: EventRvAdapter
    private val viewModel: EventViewModel by viewModels()

    private lateinit var coroutineScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coroutineScope = lifecycleScope

        eventRvAdapter = EventRvAdapter(this@EventActivity, viewModel)

        App()

        binding.apply {
            backImg.onThrottleClick {
                BackPressedUtil().activityFinish(this@EventActivity,this@EventActivity)
            }

            eventRv.apply {
                layoutManager = LinearLayoutManager(this@EventActivity, RecyclerView.VERTICAL, false)
                adapter = eventRvAdapter

                itemAnimator = null
            }
        }

        lifecycleScope.launch {
            launch(Dispatchers.Main) {
                viewModel.eventList.collect {
                    when(it) {
                        is EventViewModel.EventListState.Success -> {
                            eventRvAdapter.submitData(it.eventList)
                        }
                        is EventViewModel.EventListState.Failure -> {

                        }
                        else -> {}
                    }
                }
            }

            launch(Dispatchers.IO) {
                viewModel.getEventList()
            }
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        BackPressedUtil().activityCreate(this@EventActivity,this@EventActivity)
        BackPressedUtil().systemBackPressed(this@EventActivity,this@EventActivity)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}
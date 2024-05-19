package run.piece.dev.view.common

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.databinding.ActivityNetworkErrorBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.utils.NetworkConnection

@AndroidEntryPoint
class NetworkActivity : AppCompatActivity() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: ActivityNetworkErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.activity = this@NetworkActivity
        binding.lifecycleOwner = this@NetworkActivity


        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }


        coroutineScope = lifecycleScope
        coroutineScope.launch {
            val networkConnection = NetworkConnection(this@NetworkActivity)
            networkConnection.observe(this@NetworkActivity) { isConnected ->
                if (isConnected) {
                    with(binding) {
                        confirmBtn.onThrottleClick { finish() }
                        closeIcon.onThrottleClick { finish() }

                        // API Level.33 이상 OnBackPress
                        val callback = object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {
                                coroutineScope.launch(Dispatchers.Main) {
                                    finish()
                                }
                            }
                        }
                        lifecycleOwner?.let { onBackPressedDispatcher.addCallback(it, callback) }
                    }
                }
                else {
                    with(binding) {
                        confirmBtn.onThrottleClick {  }
                        closeIcon.onThrottleClick {  }

                        // API Level.33 이상 OnBackPress
                        val callback = object : OnBackPressedCallback(true) {
                            override fun handleOnBackPressed() {}
                        }
                        lifecycleOwner?.let { onBackPressedDispatcher.addCallback(it, callback) }
                    }


                }
            }
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
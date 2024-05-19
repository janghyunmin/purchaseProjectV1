package run.piece.dev.refactoring.ui.join

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivitySuccessBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.newinvestment.InvestmentIntroActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity

@AndroidEntryPoint
class NewJoinSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessBinding
    private lateinit var coroutineScope: CoroutineScope
    private val newJoinViewModel by viewModels<NewJoinViewModel>()
    private val dataNexusViewModel by viewModels<DataNexusViewModel>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.lifecycleOwner = this@NewJoinSuccessActivity
        binding.activity = this@NewJoinSuccessActivity
        binding.viewModel = newJoinViewModel
        binding.dataStoreViewModel = dataNexusViewModel


        binding.apply {
            if(!isNetworkConnected(this@NewJoinSuccessActivity)) {
                startActivity(getNetworkActivity(this@NewJoinSuccessActivity))
            }

            window?.apply {
                // 캡쳐방지 Kotlin Ver
                addFlags(WindowManager.LayoutParams.FLAG_SECURE);

                //상태바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch(Dispatchers.IO) {
                    PrefsHelper.write("isLogin","Y")
                    dataNexusViewModel.putIsLoginKey("Y")

                    newJoinViewModel.getMemberData()
                }

                launch(Dispatchers.Main) {
                    Glide.with(this@NewJoinSuccessActivity).load(R.drawable.join_complete_looping).into(binding.lottieLayout)

                    dataNexusViewModel.getName()
                    binding.title.text = "반가워요, ${dataNexusViewModel.getName()} 님"
                    binding.subTitle.text = "이제 ${dataNexusViewModel.getName()} 님만의 조각을 만나보세요."

                    this@NewJoinSuccessActivity.newJoinViewModel.memberInfo.collect {response ->
                        when(response) {
                            is NewJoinViewModel.MemberInfoState.Success -> {

                                dataNexusViewModel.putInvestResult(response.memberVo.preference?.result.default())
                                dataNexusViewModel.putInvestFinalScore(response.memberVo.preference?.score.default())

                                binding.confirmBtn.onThrottleClick {
                                    if(response.memberVo.preference == null) {
                                        //startActivity(getInvestMentIntroActivity(this@NewJoinSuccessActivity, name = dataNexusViewModel.getName()))
                                        startActivity(InvestmentIntroActivity.getIntent(this@NewJoinSuccessActivity, userName = dataNexusViewModel.getName()))
                                        finishAffinity()
                                    } else {

                                        startActivity(getMainActivity(this@NewJoinSuccessActivity))
                                        BackPressedUtil().activityCreate(this@NewJoinSuccessActivity,this@NewJoinSuccessActivity)
                                        finishAffinity()
                                    }
                                }

                            }
                            is NewJoinViewModel.MemberInfoState.Failure -> {
//                                LogUtil.e("회원 정보 조회 Fail : ${response.message}")
                            }
                            else -> {
//                                LogUtil.e("회원 정보 조회 Loading : $response")
                            }
                        }
                    }
                }
            }
        }
        BackPressedUtil().activityCreate(this@NewJoinSuccessActivity,this@NewJoinSuccessActivity)
        BackPressedUtil().systemBackPressed(this@NewJoinSuccessActivity,this@NewJoinSuccessActivity)
    }


    override fun onDestroy() {
        super.onDestroy()
        BackPressedUtil().activityFinish(this@NewJoinSuccessActivity,this@NewJoinSuccessActivity)
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    companion object {
        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }
        // 메인 화면 이동
        fun getMainActivity(context: Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}
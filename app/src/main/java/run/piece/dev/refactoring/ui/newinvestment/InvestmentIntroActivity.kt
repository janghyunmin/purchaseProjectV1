package run.piece.dev.refactoring.ui.newinvestment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.ActivityInvestmentNewIntroBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.member.model.MemberVo

@AndroidEntryPoint
class InvestmentIntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvestmentNewIntroBinding
    private val viewModel: InvestmentIntroViewModel by viewModels()
    private val dataNexusViewModel: DataNexusViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentNewIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {

        }

        binding.apply {
            lifecycleOwner = this@InvestmentIntroActivity
            activity = this@InvestmentIntroActivity

            loadingLv.visibility = View.GONE
            Glide.with(this@InvestmentIntroActivity).load(ContextCompat.getDrawable(this@InvestmentIntroActivity, R.drawable.image_investmentdna_visual)).into(binding.contentIv)
            titleTv.text = String.format(this@InvestmentIntroActivity.getString(R.string.investment_intro_top_title), dataNexusViewModel.getName())

            //memberVo가 있다면 getBackToMainIntent() 호출된 상태...
            viewModel.memberVo?.let { vo ->
                nextBtn.onThrottleClick {
                    startActivity(NewPassCodeActivity.getMainActivity(this@InvestmentIntroActivity, vo))
                    BackPressedUtil().activityCreateFinish(this@InvestmentIntroActivity,this@InvestmentIntroActivity)
                }

                backLayout.onThrottleClick {
                    startActivity(NewPassCodeActivity.getMainActivity(this@InvestmentIntroActivity, vo))
                    BackPressedUtil().activityCreateFinish(this@InvestmentIntroActivity,this@InvestmentIntroActivity)
                }

            } ?: run {
                nextBtn.onThrottleClick {
                    BackPressedUtil().activityCreateFinish(this@InvestmentIntroActivity,this@InvestmentIntroActivity)
                }

                backLayout.onThrottleClick {
                    BackPressedUtil().activityCreateFinish(this@InvestmentIntroActivity,this@InvestmentIntroActivity)
                }
            }
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }
        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
    }

    fun startInvestment() = startActivity(InvestmentActivity.getIntent(this@InvestmentIntroActivity, "nomal"))

    override fun onResume() {
        super.onResume()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
    }

    companion object {
        fun getIntent(context: Context, userName: String): Intent {
            val intent = Intent(context, InvestmentIntroActivity::class.java)
            intent.putExtra("userName", userName)
            return intent
        }

        fun getBackToMainIntent(context: Context, memberVo: MemberVo): Intent {
            val intent = Intent(context, InvestmentIntroActivity::class.java)
            intent.putExtra("memberVo", memberVo)
            intent.putExtra("userName", memberVo.name)
            return intent
        }
    }
}
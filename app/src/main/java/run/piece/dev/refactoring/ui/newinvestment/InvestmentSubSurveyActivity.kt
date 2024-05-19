package run.piece.dev.refactoring.ui.newinvestment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import run.piece.dev.R
import run.piece.dev.databinding.ActivityInvestmentSubSurveyBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.ui.main.MainActivity

class InvestmentSubSurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvestmentSubSurveyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvestmentSubSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@InvestmentSubSurveyActivity
            activity = this@InvestmentSubSurveyActivity
        }

        overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
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
                startActivity(Intent(this@InvestmentSubSurveyActivity, MainActivity::class.java))
                finishAffinity()
            },
            dismissAction = {},
        ).show(supportFragmentManager, "investmentClose")
    }

    fun startInvestment() {
        startActivity(InvestmentActivity.getIntent(this@InvestmentSubSurveyActivity, "survey"))
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, InvestmentSubSurveyActivity::class.java)
        }
    }
}
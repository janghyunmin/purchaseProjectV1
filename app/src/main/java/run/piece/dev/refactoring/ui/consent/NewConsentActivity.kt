package run.piece.dev.refactoring.ui.consent

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityNewConsentBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.dev.refactoring.utils.toDateFormat
import run.piece.dev.widget.extension.SnackBarCommon

@AndroidEntryPoint
class NewConsentActivity : AppCompatActivity(R.layout.activity_new_consent) {
    private lateinit var binding: ActivityNewConsentBinding
    private val viewModel: NewConsentViewModel by viewModels()

    val backPressedUtil = BackPressedUtil()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewConsentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.activity = this@NewConsentActivity
        binding.viewModel = viewModel
        setContentView(binding.root)

        App()

        binding.apply {
            backLayout.onThrottleClick {
                BackPressedUtil().activityFinish(this@NewConsentActivity,this@NewConsentActivity)
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            launch(Dispatchers.Main) {
                viewModel.consentMemberTerms.collect { data ->
                    when (data) {
                        is NewConsentViewModel.ConsentMemberTermsState.Success -> {

                            if (data.termsMemberVo.required.consent.isNotEmpty()) {
                                //필수 동의
                                val mandatoryRvAdapter = TermsMemberRvAdapter(this@NewConsentActivity, webLink = viewModel.getConsentWebLink(), viewModel = viewModel)
                                mandatoryRvAdapter.submitList(data.termsMemberVo.required.consent)

                                binding.mandatoryRv.apply {
                                    layoutManager = LinearLayoutManager(this@NewConsentActivity, LinearLayoutManager.VERTICAL, false)
                                    adapter = mandatoryRvAdapter
                                }

                                if (data.termsMemberVo.required.date.isNotBlank()) binding.agreeDateTv.text = "${data.termsMemberVo.required.date.toBaseDateFormat()} 동의"
                                else binding.agreeDateTv.visibility = View.GONE
                            }

                            if (data.termsMemberVo.selective.consent.isNotEmpty()) {
                                //선택 동의
                                val optionalRvAdapter = TermsMemberRvAdapter(this@NewConsentActivity, viewType = "Toggle", webLink = viewModel.getConsentWebLink(), viewModel = viewModel)
                                optionalRvAdapter.submitList(data.termsMemberVo.selective.consent)

                                binding.selectiveRv.apply {
                                    layoutManager = LinearLayoutManager(this@NewConsentActivity, LinearLayoutManager.VERTICAL, false)
                                    adapter = optionalRvAdapter
                                }
                            }

                            if (data.termsMemberVo.policy.consent.isNotEmpty()) {
                                //방침.절차
                                val policyRvAdapter = TermsMemberRvAdapter(this@NewConsentActivity, webLink = viewModel.getConsentWebLink(), viewModel = viewModel)
                                policyRvAdapter.submitList(data.termsMemberVo.policy.consent)

                                binding.policyRv.apply {
                                    layoutManager = LinearLayoutManager(this@NewConsentActivity, LinearLayoutManager.VERTICAL, false)
                                    adapter = policyRvAdapter
                                }
                            }
                        }

                        is NewConsentViewModel.ConsentMemberTermsState.Failure -> {
                            /*startActivity(ErrorActivity.getIntent(this@NewConsentActivity))*/
                        }
                        else -> {}
                    }
                }
            }

            launch(Dispatchers.IO) {
                viewModel.getConsentMemberTermsList()
            }

            launch(Dispatchers.Main) {
                viewModel.sendConsent.collect { data ->
                    when (data) {
                        is NewConsentViewModel.ConsentSendState.Success -> {}
                        is NewConsentViewModel.ConsentSendState.Failure -> {}
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

        binding.backLayout.onThrottleClick {
            BackPressedUtil().activityFinish(this@NewConsentActivity,this@NewConsentActivity)
        }

        backPressedUtil.activityCreate(this@NewConsentActivity,this@NewConsentActivity)
        backPressedUtil.systemBackPressed(this@NewConsentActivity,this@NewConsentActivity)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    fun showSnackBar(message: String) {
        if (viewModel.selectiveItemDate.isNotEmpty()) {
            SnackBarCommon(binding.root, "${viewModel.selectiveItemDate.toDateFormat()}\n$message", "광고성 정보 활용 및 수신 알림").show(8)
        }
    }

    fun sendConsent(consentCode: String, isAgreement: String, isCancel:((Boolean) -> Unit)? = null) {
        if (isAgreement == "Y") {
            viewModel.sendConsent(consentCode = consentCode, isAgreement = isAgreement)
        } else {
            AppConfirmDF.newInstance(
                "철회 확인",
                "철회하시면 맞춤형 혜택 서비스를 받아 보실 수 없어요.",
                false,
                positiveStrRes = R.string.revoke_txt,
                positiveAction = {
                    viewModel.sendConsent(consentCode = consentCode, isAgreement = isAgreement)
                    viewModel.isShowSnackBar = false
                    isCancel?.invoke(true)
                },
                negativeStrRes = R.string.dismiss,
                negativeAction = {
                    isCancel?.invoke(false)
                },
                dismissAction = {},
                backgroundDrawable = R.drawable.btn_round_ff7878
            ).show(supportFragmentManager, "이메일 등록 완료")
        }
    }

    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, NewConsentActivity::class.java)
            return intent
        }
    }
}
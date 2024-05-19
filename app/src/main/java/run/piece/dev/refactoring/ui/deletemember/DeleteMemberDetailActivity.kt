package run.piece.dev.refactoring.ui.deletemember

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityDeletememberDetailBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.ErrorActivity

@AndroidEntryPoint
class DeleteMemberDetailActivity : AppCompatActivity(R.layout.activity_deletemember_detail) {
    private lateinit var binding: ActivityDeletememberDetailBinding
    var checked: Boolean = false
    val viewModel by viewModels<DeleteMemberDetialViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeletememberDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        lifecycleScope.launch(Dispatchers.Main) {
            launch(Dispatchers.Main) {
                viewModel.memberDelete.collect {
                    when(it) {
                        is DeleteMemberDetialViewModel.MemberDeleteState.Loading -> {
                            binding.loading.visibility = View.VISIBLE
                        }

                        is DeleteMemberDetialViewModel.MemberDeleteState.Success -> {
                            binding.loading.visibility = View.GONE
                            when(it.memberDeleteVo.code) {
                                200 -> {
                                    // 탈퇴 성공시 탈퇴 완료 Activity 이동
                                    val intent = Intent(this@DeleteMemberDetailActivity, DeleteMemberSuccessActivity::class.java)
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finishAffinity()
                                }

                                // 클라이언트 통신은 성공했기 때문에 성공이지만 202, 400의 경우 오류 팝업을 띄웁니다.
                                202, 400 -> {
                                    AppConfirmDF.newInstance(
                                        title = getString(R.string.delete_member_not_cancle_text),
                                        message = getString(R.string.delete_member_popup_notice_text),
                                        cancelable = false,
                                        positiveStrRes = R.string.more_title_9,
                                        positiveAction = {
                                            KakaoSdk.init(this@DeleteMemberDetailActivity,"","",false)
                                            val url = TalkApiClient.instance.addChannelUrl("_XLxjmK") // 카카오톡 채널 추가하기 URL
                                            KakaoCustomTabsClient.openWithDefault(this@DeleteMemberDetailActivity, url) // CustomTabs 로 열기
                                        },
                                        negativeStrRes = R.string.delete_member_cancle_text,
                                        negativeAction = {},
                                        dismissAction = {}
                                    ).show(supportFragmentManager, "deleteMemberFail")
                                }
                            }
                        }

                        is DeleteMemberDetialViewModel.MemberDeleteState.Failure -> {
                            // 에러인 경우
                            binding.loading.visibility = View.GONE
                            startActivity(ErrorActivity.getIntent(this@DeleteMemberDetailActivity))
                        }
                        else -> {}
                    }
                }
            }
        }

        binding.apply {
            // 예치금 출금 신청하기 버튼
            withdrawLayout.onThrottleClick {
                val intent = Intent(this@DeleteMemberDetailActivity, MainActivity::class.java)
                intent.putExtra("requestCode","1000")
                startActivity(intent)
                BackPressedUtil().activityAllFinish(this@DeleteMemberDetailActivity,this@DeleteMemberDetailActivity)

                /*val intent = Intent(this@DeleteMemberDetailActivity, WithdrawalActivity::class.java)
                startActivity(intent)*/
            }

            ownLayout.onThrottleClick {
                val intent = Intent(this@DeleteMemberDetailActivity, MainActivity::class.java)
                intent.putExtra("requestCode","1000")
                startActivity(intent)
                BackPressedUtil().activityAllFinish(this@DeleteMemberDetailActivity,this@DeleteMemberDetailActivity)
            }

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                checked = isChecked
                if (isChecked) {
                    deleteCv.isClickable = true
                    deleteCv.setCardBackgroundColor(ContextCompat.getColor(this@DeleteMemberDetailActivity, R.color.c_FF7878))
                } else {
                    deleteCv.isClickable = false
                    deleteCv.setCardBackgroundColor(ContextCompat.getColor(this@DeleteMemberDetailActivity, R.color.g400_DADCE3))
                }
            }

            binding.backIv.onThrottleClick {
                BackPressedUtil().activityFinish(this@DeleteMemberDetailActivity,this@DeleteMemberDetailActivity)
            }

            cancelCv.onThrottleClick {
                BackPressedUtil().activityFinish(this@DeleteMemberDetailActivity,this@DeleteMemberDetailActivity)
            }

            deleteCv.onThrottleClick {
                if (checked) {
                    AppConfirmDF.newInstance(
                        title = "정말로 탈퇴하시겠습니까?",
                        message = "회원 탈퇴 시 30일간 재가입이 불가능해요.",
                        cancelable = false,
                        positiveStrRes = R.string.delete_text,
                        positiveAction = {
                            viewModel.memberDelete()
                        },
                        negativeStrRes = R.string.cancle,
                        negativeAction = {},
                        backgroundDrawable = R.drawable.btn_round_ff7878,
                        dismissAction = {}
                    ).show(supportFragmentManager, "deleteMember")
                }
            }

            val emailText = "help@buysellstandards.com"
            val textIndex = deleteNoticeTv.text.indexOf(emailText)
            val spannable = SpannableStringBuilder(deleteNoticeTv.text).apply {
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(this@DeleteMemberDetailActivity, R.color.g600_8C919F)),
                    textIndex,
                    textIndex + emailText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(LeadingMarginSpan.Standard(0, 28), 0, length, 0)
            }
            deleteNoticeTv.text = spannable
        }

        window?.apply {
            // 캡쳐방지 Kotlin Ver
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true

        }
        BackPressedUtil().activityCreate(this@DeleteMemberDetailActivity,this@DeleteMemberDetailActivity)
        BackPressedUtil().systemBackPressed(this@DeleteMemberDetailActivity,this@DeleteMemberDetailActivity)
    }

    companion object {
        fun getIntent(context: Context, withdrawalReasonCode: String, withdrawalReasonText: String): Intent {
            val intent = Intent(context, DeleteMemberDetailActivity::class.java)
            intent.putExtra("withdrawalReasonCode", withdrawalReasonCode)
            intent.putExtra("withdrawalReasonText", withdrawalReasonText)
            return intent
        }
    }
}
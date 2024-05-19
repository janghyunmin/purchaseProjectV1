package run.piece.dev.refactoring.ui.deletemember

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doOnTextChanged
import com.android.tools.build.jetifier.core.utils.Log
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityDeletememberBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick

// 회원탈퇴 Activity
@AndroidEntryPoint
class DeleteMemberActivity : AppCompatActivity(R.layout.activity_deletemember) {
    private val MIN_KEYBOARD_HEIGHT_PX = 150

    private lateinit var binding: ActivityDeletememberBinding

    // 회원 탈퇴 요청시 Model 필요 값
    private var withdrawalReasonCode: String? = ""
    private var withdrawalReasonText: String? = ""

    private val windowVisibleDisplayFrame = Rect()
    private var lastVisibleDecorViewHeight: Int = 0

    private val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        window.decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame)
        val visibleDecorViewHeight = windowVisibleDisplayFrame.height()

        if (lastVisibleDecorViewHeight != 0) {
            if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
                val currentKeyboardHeight = window.decorView.height - windowVisibleDisplayFrame.bottom
                binding.nestedScrollView.smoothScrollTo(0, binding.nestedScrollView.height + currentKeyboardHeight)
            }
        }
        // Save current decor view height for the next call.
        lastVisibleDecorViewHeight = visibleDecorViewHeight
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeletememberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.apply {
            confirmBtn.onThrottleClick {
                if (confirmBtn.isSelected) {
                    val intent = DeleteMemberDetailActivity.getIntent(
                        this@DeleteMemberActivity,
                        "$withdrawalReasonCode",
                        "$withdrawalReasonText")
                    startActivity(intent)
                }
            }

            backIv.onThrottleClick {
                BackPressedUtil().activityFinish(this@DeleteMemberActivity,this@DeleteMemberActivity)
            }

            // 사용하지 않는 앱이에요 OnClick
            reasonLayout1.onThrottleClick {
                changeUI("reason1")
            }
            // 수익률 회수기간이 너무 길어요
            reasonLayout2.onThrottleClick {
                changeUI("reason2")
            }
            // 앱에 오류가 많아요
            reasonLayout3.onThrottleClick {
                changeUI("reason3")
            }
            // 앱을 어떻게 쓰는지 모르겠어요
            reasonLayout4.onThrottleClick {
                changeUI("reason4")
            }
            // 비슷한 서비스가 더 좋아요
            reasonLayout5.onThrottleClick {
                changeUI("reason5")
            }
            // 기타
            reasonLayout6.onThrottleClick {
                changeUI("reason6")
            }
        }

        window?.apply {
            // 캡쳐방지 Kotlin Ver
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true

            decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        }

        BackPressedUtil().activityCreate(this@DeleteMemberActivity,this@DeleteMemberActivity)
        BackPressedUtil().systemBackPressed(this@DeleteMemberActivity,this@DeleteMemberActivity)
    }

    private fun changeUI(status: String) {
        when (status) {
            "reason1" -> {
                binding.reasonTitle1.setTextColor(getColor(R.color.p500_10CFC9))
                binding.checkIcon1.visibility = View.VISIBLE

                binding.reasonTitle2.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon2.visibility = View.GONE

                binding.reasonTitle3.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon3.visibility = View.GONE

                binding.reasonTitle4.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon4.visibility = View.GONE

                binding.reasonTitle5.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon5.visibility = View.GONE

                binding.reasonTitle6.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon6.visibility = View.GONE
                binding.editText.visibility = View.GONE

                binding.confirmBtn.isSelected = true

                withdrawalReasonCode = "MWR0101"
                withdrawalReasonText = "사용하지 않는 앱이에요"

                hideKeypad()
            }
            "reason2" -> {
                binding.reasonTitle1.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon1.visibility = View.GONE

                binding.reasonTitle2.setTextColor(getColor(R.color.p500_10CFC9))
                binding.checkIcon2.visibility = View.VISIBLE

                binding.reasonTitle3.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon3.visibility = View.GONE

                binding.reasonTitle4.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon4.visibility = View.GONE

                binding.reasonTitle5.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon5.visibility = View.GONE

                binding.reasonTitle6.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon6.visibility = View.GONE
                binding.editText.visibility = View.GONE

                binding.confirmBtn.isSelected = true

                withdrawalReasonCode = "MWR0102"
                withdrawalReasonText = "수익률 회수기간이 너무 길어요"

                hideKeypad()
            }
            "reason3" -> {
                binding.reasonTitle1.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon1.visibility = View.GONE

                binding.reasonTitle2.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon2.visibility = View.GONE

                binding.reasonTitle3.setTextColor(getColor(R.color.p500_10CFC9))
                binding.checkIcon3.visibility = View.VISIBLE

                binding.reasonTitle4.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon4.visibility = View.GONE

                binding.reasonTitle5.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon5.visibility = View.GONE

                binding.reasonTitle6.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon6.visibility = View.GONE
                binding.editText.visibility = View.GONE

                binding.confirmBtn.isSelected = true

                withdrawalReasonCode = "MWR0103"
                withdrawalReasonText = "앱에 오류가 많아요"

                hideKeypad()
            }
            "reason4" -> {
                binding.reasonTitle1.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon1.visibility = View.GONE

                binding.reasonTitle2.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon2.visibility = View.GONE

                binding.reasonTitle3.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon3.visibility = View.GONE

                binding.reasonTitle4.setTextColor(getColor(R.color.p500_10CFC9))
                binding.checkIcon4.visibility = View.VISIBLE

                binding.reasonTitle5.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon5.visibility = View.GONE

                binding.reasonTitle6.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon6.visibility = View.GONE
                binding.editText.visibility = View.GONE

                binding.confirmBtn.isSelected = true

                withdrawalReasonCode = "MWR0104"
                withdrawalReasonText = "앱을 어떻게 쓰는지 모르겠어요"

                hideKeypad()
            }
            "reason5" -> {
                binding.reasonTitle1.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon1.visibility = View.GONE

                binding.reasonTitle2.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon2.visibility = View.GONE

                binding.reasonTitle3.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon3.visibility = View.GONE

                binding.reasonTitle4.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon4.visibility = View.GONE

                binding.reasonTitle5.setTextColor(getColor(R.color.p500_10CFC9))
                binding.checkIcon5.visibility = View.VISIBLE

                binding.reasonTitle6.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon6.visibility = View.GONE
                binding.editText.visibility = View.GONE

                binding.confirmBtn.isSelected = true

                withdrawalReasonCode = "MWR0105"
                withdrawalReasonText = "비슷한 서비스가 더 좋아요"

                hideKeypad()
            }
            "reason6" -> {
                binding.reasonTitle1.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon1.visibility = View.GONE

                binding.reasonTitle2.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon2.visibility = View.GONE

                binding.reasonTitle3.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon3.visibility = View.GONE

                binding.reasonTitle4.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon4.visibility = View.GONE

                binding.reasonTitle5.setTextColor(getColor(R.color.g600_8C919F))
                binding.checkIcon5.visibility = View.GONE

                binding.reasonTitle6.setTextColor(getColor(R.color.p500_10CFC9))
                binding.checkIcon6.visibility = View.VISIBLE
                binding.editText.visibility = View.VISIBLE

                binding.confirmBtn.isSelected = "${binding.editText.text}".isNotBlank()

                binding.editText.doOnTextChanged { text, start, before, count ->
                    text?.let {
                        binding.confirmBtn.isSelected = it.isNotBlank()
                    }
                }

                withdrawalReasonCode = "MWR0106"
                withdrawalReasonText = "기타"
            }
        }
    }

    private fun hideKeypad() {
        binding.editText.clearFocus()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.editText.windowToken, 0)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, DeleteMemberActivity::class.java)
        }
    }
}
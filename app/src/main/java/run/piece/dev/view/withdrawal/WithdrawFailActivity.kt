package run.piece.dev.view.withdrawal

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.databinding.ActivityWithdrawFailBinding
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection

/**
 *packageName    : com.bsstandard.piece.view.withdrawal
 * fileName       : WithdrawFailActivity
 * author         : piecejhm
 * date           : 2022/10/04
 * description    : 예치금 출금 실패 Activity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/10/04        piecejhm       최초 생성
 */


@AndroidEntryPoint
class WithdrawFailActivity :
    BaseActivity<ActivityWithdrawFailBinding>(R.layout.activity_withdraw_fail) {
    val mContext: Context = this@WithdrawFailActivity

    companion object {
        const val TAG: String = "WithdrawFailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(applicationContext, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            binding.lifecycleOwner = this@WithdrawFailActivity
            // UI Setting 최종 - jhm 2022/09/14
            setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
            setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
            setNaviBarIconColor(true) // 네비게이션 true : 검정색
            setNaviBarBgColor("#ffffff") // 네비게이션 배경색

            // 확인 클릭시 자산 탭 이동 - jhm 2022/10/04
            confirmBtn.onThrottleClick {
                val intent = Intent(mContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("requestCode", "1001")
                setResult(RESULT_OK, intent)
                BackPressedUtil().activityFinish(this@WithdrawFailActivity,this@WithdrawFailActivity)
            }
        }

        BackPressedUtil().activityCreate(this@WithdrawFailActivity,this@WithdrawFailActivity)
        BackPressedUtil().systemBackPressed(this@WithdrawFailActivity,this@WithdrawFailActivity)
    }

    /** Util start **/
    /**
     * 상태바 아이콘 색상 지정
     * @param isBlack true : 검정색 / false : 흰색
     */
    private fun setStatusBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능

            window.insetsController?.let {
                it.setSystemBarsAppearance(
                    if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // minSdk 6.0부터 사용 가능
            window.decorView.systemUiVisibility = if (isBlack) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // 기존 uiVisibility 유지
                window.decorView.systemUiVisibility
            }

        } // end if

    }

    /**
     * 상태바 배경 색상 지정
     * @param colorHexValue #ffffff 색상 값
     */
    private fun setStatusBarBgColor(colorHexValue: String) {

        // 상태바 배경색은 5.0부터 가능하나, 아이콘 색상은 6.0부터 변경 가능
        // -> 아이콘/배경색 모두 바뀌어야 의미가 있으므로 6.0으로 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor(colorHexValue)

        } // end if
    }

    /**
     * 내비바 아이콘 색상 지정
     * @param isBlack true : 검정색 / false : 흰색
     */
    private fun setNaviBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능

            window.insetsController?.let {
                it.setSystemBarsAppearance(
                    if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 아이콘 색상이 8.0부터 가능하므로 커스텀은 동시에 진행해야 하므로 조건 동일 처리.
            window.decorView.systemUiVisibility =
                if (isBlack) {
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                } else {
                    // 기존 uiVisibility 유지
                    // -> 0으로 설정할 경우, 상태바 아이콘 색상 설정 등이 지워지기 때문
                    window.decorView.systemUiVisibility

                } // end if

        } // end if
    }

    /**
     * 내비바 배경 색상 설정
     * @param colorHexValue #ffffff 색상 값
     */
    private fun setNaviBarBgColor(colorHexValue: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 배경색은 8.0부터 지원한다.
            window.navigationBarColor = Color.parseColor(colorHexValue)

        } // end if

    }

}
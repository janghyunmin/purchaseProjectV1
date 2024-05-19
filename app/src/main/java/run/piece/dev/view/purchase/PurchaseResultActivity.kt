package run.piece.dev.view.purchase

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityPurchaseResultBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick

/**
 *packageName    : com.bsstandard.piece.view.purchase
 * fileName       : PurchaseResultActivity
 * author         : piecejhm
 * date           : 2022/10/21
 * description    : 포트폴리오 구매 결과 Activity
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/10/21        piecejhm       최초 생성
 * 2022/12/05        piecejhm       구매 실패일때 문구 및 레이아웃 변경
 */

@AndroidEntryPoint
class PurchaseResultActivity : BaseActivity<ActivityPurchaseResultBinding>(R.layout.activity_purchase_result) {
    val mContext: Context = this@PurchaseResultActivity
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val memberAppVersion: String = PrefsHelper.read("appVersion", "")

    var responseCode: Int = 0
    var message: String = ""
    var subMessage: String = ""
    var ptWithDrawDate: String = ""


    @SuppressLint("SetTextI18n", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        // UI Setting 최종 - jhm 2022/09/14
        setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
        setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
        setNaviBarIconColor(true) // 네비게이션 true : 검정색
        setNaviBarBgColor("#ffffff") // 네비게이션 배경색

        binding.apply {
            lifecycleOwner = this@PurchaseResultActivity
            binding.activity = this@PurchaseResultActivity
        }

        // 캡쳐방지 Kotlin Ver - jhm 2023/03/21
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);


        intent?.let {
            responseCode = it.getIntExtra("responseCode",0)
            message = it.getStringExtra("message").toString().default()
            subMessage = it.getStringExtra("subMessage").toString().default()
            ptWithDrawDate = intent.getStringExtra("ptWithDrawDate").toString().default()
        }


        when(responseCode) {
            200 -> {
                Glide.with(mContext).load(R.raw.purchase_complete_lopping).into(binding.resultGif)
                binding.resultTitleTv.text = message
                binding.resultSubTitleTv.text = subMessage
            }
            202 -> {
                Glide.with(mContext).load(R.drawable.withdraw_fail).into(binding.resultGif)
                binding.resultTitleTv.text = message
                binding.resultSubTitleTv.text = subMessage
            }
            400,500 -> {
                Glide.with(mContext).load(R.drawable.withdraw_fail).into(binding.resultGif)
                binding.resultTitleTv.text = message
                binding.resultSubTitleTv.text = subMessage
            }
        }

        binding.confirmBtn.onThrottleClick {
            BackPressedUtil().activityFinish(this@PurchaseResultActivity,this@PurchaseResultActivity)
        }

        BackPressedUtil().activityCreate(this@PurchaseResultActivity,this@PurchaseResultActivity)
        BackPressedUtil().systemBackPressed(this@PurchaseResultActivity,this@PurchaseResultActivity)
    }


    override fun onDestroy() {
        super.onDestroy()
        BackPressedUtil().activityFinish(this@PurchaseResultActivity,this@PurchaseResultActivity)
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
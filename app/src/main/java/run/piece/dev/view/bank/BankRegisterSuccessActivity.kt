package run.piece.dev.view.bank

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.viewmodel.AccountViewModel
import run.piece.dev.databinding.ActivityAccountSuccessBinding
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection


// 출금계좌 등록 성공
@AndroidEntryPoint
class BankRegisterSuccessActivity :
    BaseActivity<ActivityAccountSuccessBinding>(R.layout.activity_account_success) {
    private lateinit var mavm: AccountViewModel // 회원 계좌 정보 조회 ViewModel - jhm 2022/10/04
    private var disposable: Disposable? = null

    var mContext: Context = this@BankRegisterSuccessActivity
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    // 회원 출금계좌 명 - jhm 2022/10/04
    var bankName: String = ""


    companion object {
        const val TAG: String = "BankRegisterSuccessActivity"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        // UI Setting 최종 - jhm 2022/09/14
        setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
        setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
        setNaviBarIconColor(true) // 네비게이션 true : 검정색
        setNaviBarBgColor("#ffffff") // 네비게이션 배경색

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            lifecycleOwner = this@BankRegisterSuccessActivity
            mavm = ViewModelProvider(this@BankRegisterSuccessActivity)[AccountViewModel::class.java]
            memberAccountVm = mavm

            Glide.with(mContext).load(R.raw.withdraw_complete_lopping).into(withdrawLottieIv)

            // 계좌 정보 - jhm 2022/10/04
            mavm.getAccount(accessToken, deviceId, memberId)

            /**
             * 001 : 한국은행
             * 002 : KDB 산업은행
             * 003 : 기업은행
             * 004 : 국민은행
             * 081 : KEB 하나은행
             * 007 : 수협은행
             * 008 : 수출입은행
             * 011 : NH 농협은행
             * 012 : 지역농축협
             * 020 : 우리은행
             * 021 : 외환은행
             * 023 : SC 제일은행
             * 026 : 신한은행
             * 027 : 한국씨티은행
             * 031 : 대구은행
             * 032 : 부산은행
             * 034 : 광주은행
             * 035 : 제주은행
             * 037 : 전북은행
             * 039 : 경남은행
             * 045 : 새마을금고은행
             * 047 : 신협은행
             * 064 : 산림조합중앙회
             * 071 : 우체국
             * 089 : 케이뱅크
             * 090 : 카카오뱅크
             * 092 : 토스뱅크
             * */

            mavm.accountResponse.observe(this@BankRegisterSuccessActivity, Observer {
                try {
                    when (it.data.bankCode) {
                        "001" -> {}
                        "002" -> { Glide.with(mContext).load(R.drawable.bank02).into(bankIconIv)}
                        "003" -> { Glide.with(mContext).load(R.drawable.bank03).into(bankIconIv)}
                        "004" -> { Glide.with(mContext).load(R.drawable.bank04).into(bankIconIv)}
                        "007" -> { Glide.with(mContext).load(R.drawable.bank07).into(bankIconIv)}
                        "008" -> {}
                        "011" -> { Glide.with(mContext).load(R.drawable.bank11).into(bankIconIv)}
                        "020" -> { Glide.with(mContext).load(R.drawable.bank20).into(bankIconIv) }
                        "023" -> { Glide.with(mContext).load(R.drawable.bank23).into(bankIconIv) }
                        "026" -> { Glide.with(mContext).load(R.drawable.bank26).into(bankIconIv) }
                        "027" -> { Glide.with(mContext).load(R.drawable.bank27).into(bankIconIv) }
                        "031" -> { Glide.with(mContext).load(R.drawable.bank31).into(bankIconIv) }
                        "032" -> { Glide.with(mContext).load(R.drawable.bank32).into(bankIconIv)}
                        "034" -> { Glide.with(mContext).load(R.drawable.bank34).into(bankIconIv) }
                        "035" -> { Glide.with(mContext).load(R.drawable.bank35).into(bankIconIv) }
                        "037" -> { Glide.with(mContext).load(R.drawable.bank37).into(bankIconIv) }
                        "039" -> { Glide.with(mContext).load(R.drawable.bank39).into(bankIconIv) }
                        "045" -> { Glide.with(mContext).load(R.drawable.bank45).into(bankIconIv) }
                        "047" -> { Glide.with(mContext).load(R.drawable.bank47).into(bankIconIv) }
                        "064" -> { Glide.with(mContext).load(R.drawable.bank64).into(bankIconIv) }
                        "071" -> { Glide.with(mContext).load(R.drawable.bank71).into(bankIconIv) }
                        "081" -> { Glide.with(mContext).load(R.drawable.bank05).into(bankIconIv)}
                        "089" -> { Glide.with(mContext).load(R.drawable.bank89).into(bankIconIv) }
                        "090" -> { Glide.with(mContext).load(R.drawable.bank90).into(bankIconIv) }
                        "092" -> { Glide.with(mContext).load(R.drawable.bank92).into(bankIconIv) }
                    }


                    if(intent.getStringExtra("changeAccount") == "N") {
                        titleTv.text = getString(R.string.success_account_text)
                        subTitleTv.text = getString(R.string.success_account_sub_text)
                        accountTitleTv.text = getString(R.string.success_account_sub_text_2)
                    } else {
                        titleTv.text = getString(R.string.success_change_text)
                        subTitleTv.text = getString(R.string.success_change_sub_text)
                        accountTitleTv.text = getString(R.string.success_change_sub_text_2)
                    }
                    bankName = it.data.bankName
                    accountNumberTv.text = it.data.bankName + " " + it.data.accountNo

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })


            // 내지갑 이동
            confirmBtn.onThrottleClick {
                val intent = Intent(mContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("requestCode", "1001")
                setResult(RESULT_OK, intent)
                BackPressedUtil().activityFinish(this@BankRegisterSuccessActivity,this@BankRegisterSuccessActivity)
            }
        }

        BackPressedUtil().activityCreate(this@BankRegisterSuccessActivity,this@BankRegisterSuccessActivity)
        BackPressedUtil().systemBackPressed(this@BankRegisterSuccessActivity,this@BankRegisterSuccessActivity)
    }



    override fun onDestroy() {
        super.onDestroy()
        disposable?.let { disposable!!.dispose() }
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
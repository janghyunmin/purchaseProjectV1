package run.piece.dev.view.withdrawal

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.viewmodel.AccountViewModel
import run.piece.dev.data.viewmodel.GetUserViewModel
import run.piece.dev.databinding.ActivityNextWithdrawBinding
import run.piece.dev.refactoring.ui.deposit.NhWithDrawViewModel
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.setClickEvent
import run.piece.dev.widget.utils.ClickUtil

//내계좌로 출금 상세 Activity
@AndroidEntryPoint
class WithdrawalNextActivity :
    BaseActivity<ActivityNextWithdrawBinding>(R.layout.activity_next_withdraw) {
    private val click by lazy { ClickUtil(this.lifecycle) }
    private lateinit var mvm: GetUserViewModel // 내 정보 조회
    lateinit var mavm: AccountViewModel // 회원 계좌 정보 조회 ViewModel - jhm 2022/10/04

    // NH 출금 신청 요청 API ViewModel
    private val nhWithDrawViewModel: NhWithDrawViewModel by viewModels()

    val mContext: Context = this@WithdrawalNextActivity
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    var returnText: Int = 0

    // 입력한 금액 - jhm 2022/10/04
    var withdrawRequestAmount: String = ""

    // 회원 출금계좌 명 - jhm 2022/10/04
    var bankName: String = ""

    companion object {
        const val TAG: String = "WithdrawalNextActivity"
    }

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        binding.apply {
            binding.lifecycleOwner = this@WithdrawalNextActivity
            // UI Setting 최종 - jhm 2022/09/14
            setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
            setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
            setNaviBarIconColor(true) // 네비게이션 true : 검정색
            setNaviBarBgColor("#ffffff") // 네비게이션 배경색

            binding.loading.visibility = View.GONE
        }
        mvm = ViewModelProvider(this@WithdrawalNextActivity)[GetUserViewModel::class.java]
        mavm = ViewModelProvider(this@WithdrawalNextActivity)[AccountViewModel::class.java]

        binding.memberAccountVm = mavm

        // 캡쳐방지 Kotlin Ver - jhm 2023/03/21
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        withdrawRequestAmount = intent.getStringExtra("withdrawRequestAmount").toString()

        // 출금하려는 금액 - jhm 2022/10/04
        binding.number.text = withdrawRequestAmount + "을 보낼게요"
        // 콤마 제거된 입력값 - jhm 2022/10/07
        var replace = withdrawRequestAmount.replace("[^\\d]".toRegex(), "")

        mvm.getUserData()
        mvm.invstDepsBal.observe(this@WithdrawalNextActivity, Observer {
            returnText = it.toInt().minus(replace.toInt())
            binding.depositNumber.text = "${returnText.toLong().decimalComma()} 원"

        })


        // 계좌 정보 - jhm 2022/10/04
        mavm.getAccount(accessToken, deviceId, memberId)
        mavm.accountResponse.observe(this@WithdrawalNextActivity, Observer {
            try {
                when (it.data.bankCode) {
                    "001" -> {}
                    "002" -> {
                        Glide.with(mContext).load(R.drawable.bank02).into(binding.bankIcon)
                    }

                    "003" -> {
                        Glide.with(mContext).load(R.drawable.bank03).into(binding.bankIcon)
                    }

                    "004" -> {
                        Glide.with(mContext).load(R.drawable.bank04).into(binding.bankIcon)
                    }

                    "081" -> {
                        Glide.with(mContext).load(R.drawable.bank05).into(binding.bankIcon)
                    }

                    "007" -> {
                        Glide.with(mContext).load(R.drawable.bank07).into(binding.bankIcon)
                    }

                    "008" -> {}
                    "011" -> {
                        Glide.with(mContext).load(R.drawable.bank11).into(binding.bankIcon)
                    }

                    "020" -> {
                        Glide.with(mContext).load(R.drawable.bank20).into(binding.bankIcon)
                    }

                    "023" -> {
                        Glide.with(mContext).load(R.drawable.bank23).into(binding.bankIcon)
                    }

                    "026" -> {
                        Glide.with(mContext).load(R.drawable.bank26).into(binding.bankIcon)
                    }

                    "027" -> {
                        Glide.with(mContext).load(R.drawable.bank27).into(binding.bankIcon)
                    }

                    "031" -> {
                        Glide.with(mContext).load(R.drawable.bank31).into(binding.bankIcon)
                    }

                    "032" -> {
                        Glide.with(mContext).load(R.drawable.bank32).into(binding.bankIcon)
                    }

                    "034" -> {
                        Glide.with(mContext).load(R.drawable.bank34).into(binding.bankIcon)
                    }

                    "035" -> {
                        Glide.with(mContext).load(R.drawable.bank35).into(binding.bankIcon)
                    }

                    "037" -> {
                        Glide.with(mContext).load(R.drawable.bank37).into(binding.bankIcon)
                    }

                    "039" -> {
                        Glide.with(mContext).load(R.drawable.bank39).into(binding.bankIcon)
                    }

                    "045" -> {
                        Glide.with(mContext).load(R.drawable.bank45).into(binding.bankIcon)
                    }

                    "047" -> {
                        Glide.with(mContext).load(R.drawable.bank47).into(binding.bankIcon)
                    }

                    "064" -> {
                        Glide.with(mContext).load(R.drawable.bank64).into(binding.bankIcon)
                    }

                    "071" -> {
                        Glide.with(mContext).load(R.drawable.bank71).into(binding.bankIcon)
                    }

                    "089" -> {
                        Glide.with(mContext).load(R.drawable.bank89).into(binding.bankIcon)
                    }

                    "090" -> {
                        Glide.with(mContext).load(R.drawable.bank90).into(binding.bankIcon)
                    }

                    "092" -> {
                        Glide.with(mContext).load(R.drawable.bank92).into(binding.bankIcon)
                    }
                }

                bankName = it.data.bankName
                binding.accountNumber.text = it.data.bankName + " " + it.data.accountNo
            } catch (e: Exception) {

            }
        })

        binding.confirmBtn.setClickEvent(lifecycleScope) {
            binding.loading.visibility = View.VISIBLE
            // 출금 신청시 콤마 제거한 숫자 대입
            this@WithdrawalNextActivity.nhWithDrawViewModel.nhWithDraw(replace)
            this@WithdrawalNextActivity.lifecycleScope.launch {
                this@WithdrawalNextActivity.nhWithDrawViewModel.state.collect { state ->
                    // NH Bank 출금 신청 성공
                    if(state is NhWithDrawViewModel.WithDrawState.Success) {
                        binding.loading.visibility = View.GONE
                        val intent = Intent(mContext, WithdrawSuccessActivity::class.java)
                        intent.putExtra("withdrawRequestAmount", withdrawRequestAmount.replace("[^\\d]".toRegex(), ""))
                        intent.putExtra("bankName", bankName)
                        startActivity(intent)
                        BackPressedUtil().activityFinish(this@WithdrawalNextActivity,this@WithdrawalNextActivity)
                    }

                    // NH Bank 출금 신청 실패
                    else if(state is NhWithDrawViewModel.WithDrawState.Failure) {
                        binding.loading.visibility = View.GONE
                        binding.loading.visibility = View.GONE
                        val intent = Intent(mContext, WithdrawFailActivity::class.java)
                        startActivity(intent)
                        BackPressedUtil().activityFinish(this@WithdrawalNextActivity,this@WithdrawalNextActivity)
                    }
                }
            }
        }

        binding.backImg.onThrottleClick {
            BackPressedUtil().activityFinish(this@WithdrawalNextActivity,this@WithdrawalNextActivity)
        }


        BackPressedUtil().activityCreate(this@WithdrawalNextActivity,this@WithdrawalNextActivity)
        BackPressedUtil().systemBackPressed(this@WithdrawalNextActivity,this@WithdrawalNextActivity)
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
package run.piece.dev.view.bank

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.viewmodel.AccountRegisterViewModel
import run.piece.dev.databinding.ActivityRegisterBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.base.BaseActivity
import run.piece.dev.refactoring.ui.deposit.NhAccountViewModel
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.KeyboardVisibilityUtils
import run.piece.dev.widget.utils.NetworkConnection

//출금계좌 등록 Activity
@AndroidEntryPoint
class BankRegisterActivity :
    BaseActivity<ActivityRegisterBinding, NhAccountViewModel>(R.layout.activity_register) {
    override fun getViewModelClass(): Class<NhAccountViewModel> = NhAccountViewModel::class.java

    private val vm by viewModels<AccountRegisterViewModel>()

    // 계좌등록 API - jhm 2022/10/05
//    private val response: RetrofitService? = NetworkInfo.getRetrofit().create(RetrofitService::class.java)
    private val accessToken: String = PrefsHelper.read("accessToken", "")
    private val deviceId: String = PrefsHelper.read("deviceId", "")
    private val memberId: String = PrefsHelper.read("memberId", "")
    private val context: Context = this@BankRegisterActivity
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils

    // 넘겨받은 은행 정보 - jhm 2022/10/05
    private var bankCode: String? = ""
    private var bankName: String? = ""

    // 계좌 등록 / 변경 에 따른 문구가 변해야해서 변수로 분기처리
    private var vranNo: String = ""

    // 예금주 이름 - jhm 2022/10/05
    private var userName: String = ""
    private var mUserInputAccount: String? = null

    // 키패드에 따른 버튼 변경 변수
    private var keyPadShow: String = ""


    companion object {
        const val TAG: String = "BankRegisterActivity"
    }


    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        // 캡쳐방지 Kotlin Ver - jhm 2023/03/21
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        binding.apply {
            lifecycleOwner = this@BankRegisterActivity
            registerVm = vm

            // UI Setting 최종 - jhm 2022/09/14
            setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
            setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
            setNaviBarIconColor(true) // 네비게이션 true : 검정색
            setNaviBarBgColor("#ffffff") // 네비게이션 배경색

            loading.visibility = View.GONE

            val intent = intent
            userName = PrefsHelper.read("name", "")
            bankCode = intent.getStringExtra("bankCode").toString()
            bankName = intent.getStringExtra("bankName").toString()
            vranNo = intent.getStringExtra("vranNo").toString()

            intent.getStringExtra("vranNo")?.let {
                if (it.isEmpty()) {
                    topTitle.text = getString(R.string.account_text)
                    title.text = getString(R.string.account_register_text)
                    confirmBtn.text = getString(R.string.account_register_title)
                } else {
                    topTitle.text = getString(R.string.account_change_text)
                    title.text = getString(R.string.account_register_change_text)
                    confirmBtn.text = getString(R.string.account_register_change_title_txt)
                }
            }

            bankTitle.text = bankName
            name.text = userName


            // 키패드 제어
            keyboardVisibilityUtils =
                KeyboardVisibilityUtils(window, onShowKeyboard = { keyboardHeight ->
                    svRoot.run {
                        smoothScrollTo(scrollX, scrollY + keyboardHeight)
                    }
                })
            val accountNoObserver = Observer<String> { inputAccountNO ->
                mUserInputAccount = inputAccountNO
            }
            root.rootView.viewTreeObserver.addOnGlobalLayoutListener {
                val rec = Rect()
                root.rootView.getWindowVisibleDisplayFrame(rec)

                //finding screen height
                val screenHeight = root.rootView.rootView.height

                //finding keyboard height
                val keypadHeight = screenHeight - rec.bottom
                val btnParam = confirmBtn.layoutParams as ViewGroup.MarginLayoutParams

                if (keypadHeight > screenHeight * 0.15) {
                    confirmBtn.text = getString(R.string.confirm)
                    btnParam.marginStart = 0
                    btnParam.marginEnd = 0
                    confirmBtn.layoutParams = btnParam
                    confirmBtn.background = null
                    confirmBtn.setBackgroundColor(getColor(R.color.c_10cfc9))
                    keyPadShow = "Y"

                } else {
                    intent.getStringExtra("vranNo")?.let {
                        if (it.isEmpty()) {
                            confirmBtn.text = getString(R.string.account_register_title)
                        } else {
                            confirmBtn.text = getString(R.string.account_register_change_title_txt)
                        }
                    }
                    btnParam.marginStart = 32
                    btnParam.marginEnd = 32
                    confirmBtn.layoutParams = btnParam
                    confirmBtn.background = context.getDrawable(R.drawable.j_selector)
                    keyPadShow = "N"
                }
            }

            // 계좌번호 EditText 클릭시 부모 title 색상 변경 - jhm 2022/10/05
            accountNumEdit.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    accountNumText.setTextColor(context.getColor(R.color.c_4a4d55))
                } else {
                    accountNumText.setTextColor(context.getColor(R.color.c_b8bcc8))
                }
            }

            vm.getAccountNum().observe(this@BankRegisterActivity, accountNoObserver)
            vm.getAccountNum().observe(this@BankRegisterActivity, Observer {
                mUserInputAccount = it

                if (accountNumEdit.text.toString().isEmpty()) {
                    confirmBtn.isSelected = true
                    confirmBtn.onThrottleClick {
                        val mInputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                        mInputMethodManager.hideSoftInputFromWindow(
                            confirmBtn.windowToken,
                            0
                        )
                    }
                } else {
                    confirmBtn.isSelected = true
                    confirmBtn.onThrottleClick {
                        when (keyPadShow) {
                            "Y" -> {
                                // 키패드가 올라와있으므로 키패드를 닫습니다.
                                val mInputMethodManager =
                                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                                mInputMethodManager.hideSoftInputFromWindow(
                                    confirmBtn.windowToken,
                                    0
                                )
                            }

                            "N" -> {
                                // 키패드가 내려가있습니다.
                                binding.loading.visibility = View.VISIBLE

                                if(vranNo == "" || vranNo.isNullOrEmpty()) {
                                    createVarnAccount()
                                } else {
                                    changeAccount()
                                }
                            }
                        }
                    }
                }
            })

            /**
             * 001 : 한국은행
             * 002 : KDB 산업은행
             * 003 : 기업은행
             * 004 : 국민은행
             * 081 : KEB 하나은행
             * 007 : 수협은행
             * 008 : 수출입은행
             * 011 : NH 농협은행
             * 020 : 우리은행
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
            try {
                when (bankCode) {
                    "001" -> {}
                    "002" -> {
                        Glide.with(context).load(R.drawable.bank02).into(binding.bankIcon)
                    }

                    "003" -> {
                        Glide.with(context).load(R.drawable.bank03).into(binding.bankIcon)
                    }

                    "004" -> {
                        Glide.with(context).load(R.drawable.bank04).into(binding.bankIcon)
                    }

                    "081" -> {
                        Glide.with(context).load(R.drawable.bank05).into(binding.bankIcon)
                    }

                    "007" -> {
                        Glide.with(context).load(R.drawable.bank07).into(binding.bankIcon)
                    }

                    "008" -> {}
                    "011" -> {
                        Glide.with(context).load(R.drawable.bank11).into(binding.bankIcon)
                    }

                    "020" -> {
                        Glide.with(context).load(R.drawable.bank20).into(binding.bankIcon)
                    }

                    "023" -> {
                        Glide.with(context).load(R.drawable.bank23).into(binding.bankIcon)
                    }

                    "026" -> {
                        Glide.with(context).load(R.drawable.bank26).into(binding.bankIcon)
                    }

                    "027" -> {
                        Glide.with(context).load(R.drawable.bank27).into(binding.bankIcon)
                    }

                    "031" -> {
                        Glide.with(context).load(R.drawable.bank31).into(binding.bankIcon)
                    }

                    "032" -> {
                        Glide.with(context).load(R.drawable.bank32).into(binding.bankIcon)
                    }

                    "034" -> {
                        Glide.with(context).load(R.drawable.bank34).into(binding.bankIcon)
                    }

                    "035" -> {
                        Glide.with(context).load(R.drawable.bank35).into(binding.bankIcon)
                    }

                    "037" -> {
                        Glide.with(context).load(R.drawable.bank37).into(binding.bankIcon)
                    }

                    "039" -> {
                        Glide.with(context).load(R.drawable.bank39).into(binding.bankIcon)
                    }

                    "045" -> {
                        Glide.with(context).load(R.drawable.bank45).into(binding.bankIcon)
                    }

                    "047" -> {
                        Glide.with(context).load(R.drawable.bank47).into(binding.bankIcon)
                    }

                    "064" -> {
                        Glide.with(context).load(R.drawable.bank64).into(binding.bankIcon)
                    }

                    "071" -> {
                        Glide.with(context).load(R.drawable.bank71).into(binding.bankIcon)
                    }

                    "089" -> {
                        Glide.with(context).load(R.drawable.bank89).into(binding.bankIcon)
                    }

                    "090" -> {
                        Glide.with(context).load(R.drawable.bank90).into(binding.bankIcon)
                    }

                    "092" -> {
                        Glide.with(context).load(R.drawable.bank92).into(binding.bankIcon)
                    }
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
            }


            // 변경 버튼 OnClick - jhm 2022/10/05
            selectBtn.setOnClickListener {
                val intent = Intent(context, BankSelectActivity::class.java)
                intent.putExtra("vranNo", vranNo)
                startActivity(intent)
            }

            backImgIv.setOnClickListener {
                BackPressedUtil().activityFinish(this@BankRegisterActivity,this@BankRegisterActivity)
            }
        }

        BackPressedUtil().activityCreate(this@BankRegisterActivity,this@BankRegisterActivity)
        BackPressedUtil().systemBackPressed(this@BankRegisterActivity,this@BankRegisterActivity)
    }


    // 연동계좌 , 가상계좌 없을때 등록 프로세스
    private fun createVarnAccount() {
      this@BankRegisterActivity.viewModel.createNhBankAccount(PrefsHelper.read("name",""), bankCode,binding.accountNumEdit.text.toString())
//        this@BankRegisterActivity.viewModel.createNhBankAccount(
//            "모새건",
//            "011",
//            "3010351015711"
//        )
        binding.loading.visibility = View.VISIBLE
        this@BankRegisterActivity.lifecycleScope.launch {
            this@BankRegisterActivity.viewModel.state.collect { state ->
                // 연동계좌 및 가상계좌 Loading
                if (state is NhAccountViewModel.State.Loading) {
                    binding.loading.visibility = View.VISIBLE
                }

                // 연동계좌 및 가상계좌 등록 성공
                else if (state is NhAccountViewModel.State.Success) {
                    binding.loading.visibility = View.GONE
                    val intent = Intent(context, BankRegisterSuccessActivity::class.java)
                    intent.putExtra("vranNo", state.isSuccess.data.toString())
                    intent.putExtra("changeAccount", "N")
                    startActivity(intent)
                    finish()
                }

                // 연동계좌 및 가상계좌 등록 실패
                else if (state is NhAccountViewModel.State.Failure) {
                    binding.loading.visibility = View.GONE

                    val appConfirmDF = AppConfirmDF.newInstance(
                        getString(R.string.nh_charge_fail_register_title_txt),
                        getString(R.string.nh_charge_fail_register_content_txt),
                        false,
                        R.string.confirm,
                        positiveAction = {},
                        dismissAction = {}
                    )
                    appConfirmDF.show(
                        supportFragmentManager,
                        "NhAccountFail"
                    )
                }
            }
        }

    }

    private fun changeAccount() {
      this@BankRegisterActivity.viewModel.changeAccount(PrefsHelper.read("name",""), bankCode,binding.accountNumEdit.text.toString())
//        this@BankRegisterActivity.viewModel.changeAccount(
//            "모새건",
//            "011",
//            "3010351015711"
//        )
        binding.loading.visibility = View.VISIBLE
        this@BankRegisterActivity.lifecycleScope.launch {
            this@BankRegisterActivity.viewModel.state.collect { state ->
                // 계좌 변경 Loading
                if (state is NhAccountViewModel.State.Loading) {
                    binding.loading.visibility = View.VISIBLE
                }

                // 계좌 변경 성공
                else if (state is NhAccountViewModel.State.Success) {
                    binding.loading.visibility = View.GONE
                    val intent = Intent(context, BankRegisterSuccessActivity::class.java)
                    intent.putExtra("changeAccount", "Y")
                    startActivity(intent)
                    finish()
                }

                // 계좌 변경 실패
                else if (state is NhAccountViewModel.State.Failure) {
                    binding.loading.visibility = View.GONE
                    val appConfirmDF = AppConfirmDF.newInstance(
                        getString(R.string.bank_register_fail_title_txt),
                        getString(R.string.bank_register_fail_sub_title_txt),
                        false,
                        R.string.alert_title_1,
                        positiveAction = {},
                        dismissAction = {}
                    )
                    appConfirmDF.show(
                        supportFragmentManager,
                        "NhAccountFail"
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        keyboardVisibilityUtils.detachKeyboardListeners()
        super.onDestroy()
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
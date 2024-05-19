package run.piece.dev.view.withdrawal

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.viewmodel.AccountViewModel
import run.piece.dev.data.viewmodel.GetUserViewModel
import run.piece.dev.databinding.ActivityMyaccountWithdrawBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.NewVibratorUtil
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.onThrottleClick
import java.text.DecimalFormat

// 내 계좌로 출금하기 Activity
@AndroidEntryPoint
class WithdrawalActivity :
    BaseActivity<ActivityMyaccountWithdrawBinding>(R.layout.activity_myaccount_withdraw) {
    private lateinit var mvm: GetUserViewModel // 내 정보 조회
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    // last live amount - jhm 2022/10/06
    private val sb = StringBuilder()

    // 내 계좌로 출금하기 liveText - jhm 2022/10/21
    private var liveText: MutableLiveData<Int> = MutableLiveData()
    var money: Int = 0
    private var depositBalance: Int = 0
//    private var depositBalance: Int = 1000000

    companion object {
        const val TAG: String = "WithdrawalActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        mvm = ViewModelProvider(this@WithdrawalActivity)[GetUserViewModel::class.java]
//        val dvm: DepositBalanceViewModel = ViewModelProvider(this@WithdrawalActivity)[DepositBalanceViewModel::class.java] // 출금 가능 금액 ViewModel
        val mavm: AccountViewModel =
            ViewModelProvider(this@WithdrawalActivity)[AccountViewModel::class.java] // 회원 계좌 정보 조회 ViewModel


        binding.apply {
            binding.lifecycleOwner = this@WithdrawalActivity
            // UI Setting 최종 - jhm 2022/09/14
            setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
            setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
            setNaviBarIconColor(true) // 네비게이션 true : 검정색
            setNaviBarBgColor("#ffffff") // 네비게이션 배경색
        }

        binding.memberAccountVm = mavm

        // 캡쳐방지 Kotlin Ver - jhm 2023/03/21
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);


        // 출금 가능 금액 - jhm 2022/09/30
        mvm.getUserData()
        mvm.invstDepsBal.observe(this@WithdrawalActivity, Observer {
            if (it.toInt() == 0) {
                depositBalance = 0
                binding.depositNumberTv.text = "0 원"
                binding.confirmBtn.isSelected = false
            } else {
                depositBalance = it.toInt()
                binding.depositNumberTv.text = it.toLong().decimalComma() + "원"
            }
        })

        // 출금 가능 금액 OnClick - jhm 2022/11/15
        binding.depositLayout.setOnClickListener {
            NewVibratorUtil().run {
                init(this@WithdrawalActivity)
                oneShot(100,40)
            }
            if (depositBalance != 0) {
                val decimal = DecimalFormat("###,###")
                var depositText: String = ""
                depositText = decimal.format(depositBalance)
                money = depositBalance
                liveText.value = money
                binding.numberTv.text = "$depositText 원"
                binding.confirmBtn.isSelected = true
            }
        }


        // 계좌 정보 - jhm 2022/10/04
        mavm.getAccount(accessToken, deviceId, memberId)
        mavm.accountResponse.observe(this@WithdrawalActivity, Observer {
            try {
                when (it.data.bankCode) {
                    "001" -> {}
                    "002" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank02)
                            .into(binding.bankIconIv)
                    }

                    "003" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank03)
                            .into(binding.bankIconIv)
                    }

                    "004" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank04)
                            .into(binding.bankIconIv)
                    }

                    "081" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank05)
                            .into(binding.bankIconIv)
                    }

                    "007" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank07)
                            .into(binding.bankIconIv)
                    }

                    "008" -> {}
                    "011" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank11)
                            .into(binding.bankIconIv)
                    }

                    "020" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank20)
                            .into(binding.bankIconIv)
                    }

                    "023" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank23)
                            .into(binding.bankIconIv)
                    }

                    "026" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank26)
                            .into(binding.bankIconIv)
                    }

                    "027" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank27)
                            .into(binding.bankIconIv)
                    }

                    "031" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank31)
                            .into(binding.bankIconIv)
                    }

                    "032" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank32)
                            .into(binding.bankIconIv)
                    }

                    "034" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank34)
                            .into(binding.bankIconIv)
                    }

                    "035" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank35)
                            .into(binding.bankIconIv)
                    }

                    "037" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank37)
                            .into(binding.bankIconIv)
                    }

                    "039" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank39)
                            .into(binding.bankIconIv)
                    }

                    "045" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank45)
                            .into(binding.bankIconIv)
                    }

                    "047" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank47)
                            .into(binding.bankIconIv)
                    }

                    "064" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank64)
                            .into(binding.bankIconIv)
                    }

                    "071" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank71)
                            .into(binding.bankIconIv)
                    }

                    "089" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank89)
                            .into(binding.bankIconIv)
                    }

                    "090" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank90)
                            .into(binding.bankIconIv)
                    }

                    "092" -> {
                        Glide.with(this@WithdrawalActivity).load(R.drawable.bank92)
                            .into(binding.bankIconIv)
                    }
                }

                var decryption = "**********"
                var encryption: String = ""
                if (it.data.accountNo.length < 5) {
                    encryption = it.data.accountNo
                    binding.accountNumberTv.text = it.data.bankName + " " + encryption
                } else {
                    encryption = it.data.accountNo.substring(
                        it.data.accountNo.length - 4,
                        it.data.accountNo.length
                    )
                    binding.accountNumberTv.text = decryption + encryption
                }



                binding.numberTv.text = ""
                binding.numberTv.hint = "얼마를 보낼까요?"
                binding.confirmBtn.isSelected = false


            } catch (e: Exception) {
            }
        })


        var depositText: String = ""
        liveText.observe(this@WithdrawalActivity, Observer {
            if (it == 0) {

                sb.setLength(0) // string builder 초기화 - jhm 2022/10/21
                money = 0 // 입력값 초기화 - jhm 2022/10/21
                binding.numberTv.text = ""
                binding.numberTv.hint = "얼마를 보낼까요?"
                binding.confirmBtn.isSelected = false

            } else {
                depositText = makeComma(it.toString())
                binding.numberTv.text = "$depositText 원"
                binding.confirmBtn.isSelected = it.toInt() <= depositBalance

            }
        })


        /**
         * 1~9 키패드 OnClick
         * **/
        Handler(Looper.getMainLooper()).postDelayed({
            runOnUiThread {
                binding.code1.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("1").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("1").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code2.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("2").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("2").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code3.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("3").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("3").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code4.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("4").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("4").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code5.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("5").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("5").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code6.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("6").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("6").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code7.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("7").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("7").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code8.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("8").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("8").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code9.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("9").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("9").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }
                binding.code0.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    if (depositBalance == 0) {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("0").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    } else {
                        if (money.toString().length < 9) {
                            money = money.toString().plus("0").toInt()
                            liveText.value = money
                        } else {
                            money = money.toString().toInt()
                            liveText.value = money
                        }
                    }
                }

                // 1자리씩 삭제 - jhm 2022/10/21
                binding.clear.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    removeNumber()
                }

                // 초기화 버튼 - jhm 2022/10/21
                binding.allClear.setOnClickListener {
                    NewVibratorUtil().run {
                        init(this@WithdrawalActivity)
                        oneShot(100,40)
                    }
                    sb.setLength(0) // string builder 초기화 - jhm 2022/10/21
                    money = 0
                    binding.numberTv.text = ""
                    binding.numberTv.hint = "얼마를 보낼까요?"
                    binding.confirmBtn.isSelected = false
                }
            }
        }, 400)


        // 확인 버튼 - jhm 2022/10/04
        binding.confirmBtn.onThrottleClick {
            if (binding.confirmBtn.isSelected) {
                val intent = Intent(this@WithdrawalActivity, WithdrawalNextActivity::class.java)
                intent.putExtra("withdrawRequestAmount", binding.numberTv.text.toString())
                startActivity(intent)
                BackPressedUtil().activityFinish(this@WithdrawalActivity,this@WithdrawalActivity)
            }
        }

        binding.backImgIv.setOnClickListener {
            BackPressedUtil().activityFinish(this@WithdrawalActivity,this@WithdrawalActivity)
        }

        BackPressedUtil().activityCreate(this@WithdrawalActivity,this@WithdrawalActivity)
        BackPressedUtil().systemBackPressed(this@WithdrawalActivity,this@WithdrawalActivity)
    }

    // 뒤에서 부터 1자리씩 지우기 로직 - jhm 2022/10/21
    @SuppressLint("SetTextI18n")
    private fun removeNumber() {
        var temp: String = money.toString()
        if (temp.isEmpty()) {
            sb.setLength(0) // string builder 초기화 - jhm 2022/10/21
            money = 0
            liveText.value = money
            liveText.postValue(money)
            binding.numberTv.text = ""
            binding.numberTv.hint = "얼마를 보낼까요?"
            binding.confirmBtn.isSelected = false

        } else {
            try {
                money = removeLastNchars(temp, 1).toString().toInt()
                liveText.value = money
                liveText.postValue(money)
            } catch (ex: Exception) {
                ex.printStackTrace()
                sb.setLength(0) // string builder 초기화 - jhm 2022/10/21
                money = 0
                liveText.value = money
                liveText.postValue(money)
            }
        }
    }

    // 마지막 입력값 제거 - jhm 2022/10/21
    fun removeLastNchars(str: String?, n: Int): String? {
        return if (str == null || str.length < n) {
            str
        } else str.substring(0, str.length - n)
    }


    fun makeComma(price: String): String {
        //소숫점이 존재하거나 천 단위 이하일 경우 생략
        if (price.contains(".") || price.length < 4) {
            return price
        }
        val formatter = DecimalFormat("###,###")
        return formatter.format(price.toLong())
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
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
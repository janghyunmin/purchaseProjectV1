package run.piece.dev.refactoring.ui.passcode

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityNewPasscodeBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.join.NewJoinActivity
import run.piece.dev.refactoring.ui.join.NewJoinSuccessActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.main.MainViewModel
import run.piece.dev.refactoring.ui.newinvestment.InvestmentIntroActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.NewVibratorUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.view.authentication.AuthenticationActivity
import run.piece.dev.view.common.ErrorActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.view.purchase.PurchaseResultActivity
import run.piece.dev.widget.utils.DialogManager
import run.piece.domain.refactoring.member.model.MemberPinModel
import run.piece.domain.refactoring.member.model.MemberVo
import java.util.concurrent.Executor

@AndroidEntryPoint
class NewPassCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPasscodeBinding
    private lateinit var coroutineScope: CoroutineScope
    private val newPassCodeViewModel by viewModels<NewPassCodeViewModel>()
    private val dataNexusViewModel by viewModels<DataNexusViewModel>()
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    private var keyPadBtn: Array<AppCompatButton?> = arrayOfNulls<AppCompatButton>(12)
    private var passWord: String = ""
    private var oldPwd = ""
    private val stringBuilder = StringBuilder()
    var status: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.lifecycleOwner = this@NewPassCodeActivity
        binding.activity = this@NewPassCodeActivity
        binding.viewModel = newPassCodeViewModel
        binding.dataStoreViewModel = dataNexusViewModel
        binding.apply {
            if (!isNetworkConnected(this@NewPassCodeActivity)) {
                startActivity(getNetworkActivity(this@NewPassCodeActivity))
            }
            window.apply {
                // 캡쳐방지 Kotlin Ver
                addFlags(WindowManager.LayoutParams.FLAG_SECURE);

                //상태바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }

            loading.visibility = View.GONE
        }

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                executor = ContextCompat.getMainExecutor(this@NewPassCodeActivity)
                launch(Dispatchers.IO) {
                    newPassCodeViewModel.memberDeviceChk()
                    when (newPassCodeViewModel.launchStep()) {
                        PassCodeState.FIRST.id, PassCodeState.REAUTH.id -> {}
                        PassCodeState.PURCHASE.id -> {
                            newPassCodeViewModel.ssnCheck()

                            if (newPassCodeViewModel.isFido == "Y") {
                                CoroutineScope(Dispatchers.Main).launch {
                                    this@NewPassCodeActivity.newPassCodeViewModel.authenticateUsingBiometrics(
                                        status = PassCodeState.LOGIN,
                                        activity = this@NewPassCodeActivity,
                                        context = this@NewPassCodeActivity,
                                        bioLayout = binding.bioLayout,
                                        bioChkIv = binding.bioChkIv,
                                        titleView = binding.bioTitleTv,
                                        retryIv = binding.bioRetryIv
                                    )

                                    this@NewPassCodeActivity.newPassCodeViewModel.bioIsLoginState.collect { state ->
                                        biometricPrompt = BiometricPrompt(this@NewPassCodeActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                super.onAuthenticationError(errorCode, errString)
                                            }

                                            override fun onAuthenticationFailed() {
                                                super.onAuthenticationFailed()
                                            }

                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                super.onAuthenticationSucceeded(result)
                                                purchaseBridge()
                                            }
                                        })
                                    }
                                }
                            }
                        }

                        PassCodeState.CLEAR.id -> {}
                        else -> {
                            newPassCodeViewModel.ssnCheck()
                            if (newPassCodeViewModel.isFido == "Y") {
                                CoroutineScope(Dispatchers.Main).launch {
                                    this@NewPassCodeActivity.newPassCodeViewModel.authenticateUsingBiometrics(
                                        status = PassCodeState.LOGIN,
                                        activity = this@NewPassCodeActivity,
                                        context = this@NewPassCodeActivity,
                                        bioLayout = binding.bioLayout,
                                        bioChkIv = binding.bioChkIv,
                                        titleView = binding.bioTitleTv,
                                        retryIv = binding.bioRetryIv
                                    )

                                    this@NewPassCodeActivity.newPassCodeViewModel.bioIsLoginState.collect { state ->
                                        biometricPrompt = BiometricPrompt(this@NewPassCodeActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                super.onAuthenticationError(errorCode, errString)
                                            }

                                            override fun onAuthenticationFailed() {
                                                super.onAuthenticationFailed()
                                            }

                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                super.onAuthenticationSucceeded(result)
                                                binding.loading.visibility = View.VISIBLE
                                                ssnBridge()
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                launch(Dispatchers.Main) {
                    try {
                        this@NewPassCodeActivity.newPassCodeViewModel.deviceChk.collect {
                            when (it) {
                                is MainViewModel.MemberDeviceState.Success -> {
//                                    LogUtil.e("=== MemberDeviceChk Success === ${it.isSuccess}")
                                }

                                is MainViewModel.MemberDeviceState.Failure -> {
//                                    LogUtil.e("=== MemberDeviceChk Failure === ${it.message}")

                                    // Response Code 406 일때 ( 서버 점검 )
                                    // else 다른 기기 로그아웃 알림 처리
                                    // 로그아웃 처리 안되게 해야함

                                    val statusCode = extractStatusCode(it.message)

                                    if(statusCode != 406) {
                                        PrefsHelper.removeKey("inputPinNumber")
                                        PrefsHelper.removeKey("memberId")
                                        PrefsHelper.removeKey("isLogin")
                                        startActivity(getIntroActivity(this@NewPassCodeActivity))
                                        finish()
                                    }
                                }

                                else -> {
//                                    LogUtil.e("=== MemberDeviceChk More === $it")
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }



                launch(Dispatchers.Main) {
                    // 초기 View 설정
                    initView(newPassCodeViewModel.launchStep())
                    keyPadBtn[0] = binding.code1
                    keyPadBtn[1] = binding.code2
                    keyPadBtn[2] = binding.code3
                    keyPadBtn[3] = binding.code4
                    keyPadBtn[4] = binding.code5
                    keyPadBtn[5] = binding.code6
                    keyPadBtn[6] = binding.code7
                    keyPadBtn[7] = binding.code8
                    keyPadBtn[8] = binding.code9
                    keyPadBtn[9] = binding.allClear
                    keyPadBtn[10] = binding.code0
                    keyPadBtn[11] = binding.clear

                    for (index in 0..11) {
                        keyPadBtn[index]?.setOnClickListener {
                            NewVibratorUtil().run {
                                init(this@NewPassCodeActivity)
                                oneShot(100, 100)
                            }
                            val btn: AppCompatButton = it as AppCompatButton
                            val btnText: String = btn.text.toString()
                            try {
                                if (btnText == "초기화") {
                                    reset("Y")
                                } else if (btnText == "") {
                                    passWord = stringBuilder.delete(passWord.length - 1, passWord.length).toString()
                                    markChk(passWord = passWord)

                                } else {
                                    if (passWord.length < 6) {
                                        passWord = stringBuilder.append(btn.text.toString()).toString()
                                        screenStep(newPassCodeViewModel.launchStep(), inputPassWord = passWord, status = status)
                                        markChk(passWord = passWord)
                                    }
                                }
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        BackPressedUtil().activityCreate(this@NewPassCodeActivity,this@NewPassCodeActivity)
        BackPressedUtil().systemBackPressed(this@NewPassCodeActivity,this@NewPassCodeActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        BackPressedUtil().activityFinish(this@NewPassCodeActivity,this@NewPassCodeActivity)
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // 초기화
    private fun reset(colorYn: String) {
        // reset
        when (colorYn) {
            "Y" -> {
                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))
            }
        }
        passWord = ""
        stringBuilder.setLength(0)
        markChk(passWord = "")
    }


    // 입력 또는 삭제에 따른 마크 체크
    private fun markChk(passWord: String) {
        CoroutineScope(Dispatchers.Main).launch {
            newPassCodeViewModel.markChange(
                passWord = passWord,
                binding.mark1Iv,
                binding.mark2Iv,
                binding.mark3Iv,
                binding.mark4Iv,
                binding.mark5Iv,
                binding.mark6Iv
            )
        }
    }

    private fun initView(launchStep: Int) {
        when (launchStep) {
            PassCodeState.FIRST.id -> {
                newPassCodeViewModel.btnInitView(this@NewPassCodeActivity, PassCodeState.FIRST, binding.backIv, binding.bioLayout, binding.bioChkIv, binding.bioTitleTv, binding.bioRetryIv)
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.FIRST)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, "", PassCodeState.FIRST, oneMoreChk = false, equalsChk = false, "", "")
            }

            PassCodeState.LOGIN.id -> {
                newPassCodeViewModel.btnInitView(this@NewPassCodeActivity, PassCodeState.LOGIN, binding.backIv, binding.bioLayout, binding.bioChkIv, binding.bioTitleTv, binding.bioRetryIv)
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.LOGIN)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, "", PassCodeState.LOGIN, oneMoreChk = false, equalsChk = false, "", "")

            }

            PassCodeState.CHANGE.id -> {
                newPassCodeViewModel.btnInitView(this@NewPassCodeActivity, PassCodeState.CHANGE, binding.backIv, binding.bioLayout, binding.bioChkIv, binding.bioTitleTv, binding.bioRetryIv)
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.CHANGE)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, "", PassCodeState.CHANGE, oneMoreChk = false, equalsChk = false, "", "")
            }

            PassCodeState.PURCHASE.id -> {
                newPassCodeViewModel.btnInitView(this@NewPassCodeActivity, PassCodeState.PURCHASE, binding.backIv, binding.bioLayout, binding.bioChkIv, binding.bioTitleTv, binding.bioRetryIv)
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.PURCHASE)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, "", PassCodeState.PURCHASE, oneMoreChk = false, equalsChk = false, "", "")
            }

            PassCodeState.CLEAR.id -> {
                newPassCodeViewModel.btnInitView(this@NewPassCodeActivity, PassCodeState.CLEAR, binding.backIv, binding.bioLayout, binding.bioChkIv, binding.bioTitleTv, binding.bioRetryIv)
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.CLEAR)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, "", PassCodeState.CLEAR, oneMoreChk = false, equalsChk = false, "", "first")

            }

            PassCodeState.REAUTH.id -> {
                newPassCodeViewModel.btnInitView(this@NewPassCodeActivity, PassCodeState.REAUTH, binding.backIv, binding.bioLayout, binding.bioChkIv, binding.bioTitleTv, binding.bioRetryIv)
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.REAUTH)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, "", PassCodeState.REAUTH, oneMoreChk = false, equalsChk = false, "", "")
            }
        }
    }

    // 간편비밀번호 화면 진입 케이스에 따른 분기 로직
    private fun screenStep(launchStep: Int, inputPassWord: String, status: Boolean) {
        when (launchStep) {
            PassCodeState.FIRST.id, PassCodeState.REAUTH.id -> {
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.FIRST)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = false, "", "")
                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))

                if (inputPassWord.length > 2) {
                    if (newPassCodeViewModel.repeatVerifyNumber(passWord = inputPassWord) ||
                        newPassCodeViewModel.continuousVerifyNumber(passWord = inputPassWord) ||
                        newPassCodeViewModel.birthVerifyNumber(passWord = inputPassWord) ||
                        newPassCodeViewModel.phoneVerifyNumber(passWord = inputPassWord)
                    ) {
                        // 검증 실패시 초기화
                        reset("N")
                        binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                        binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = false, "", "")

                    } else {
                        // 정규식 통과 다음 로직 진행
                        if (inputPassWord.length == 6) {
                            if (oldPwd.isEmpty()) {
                                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = true, equalsChk = false, "", "")
                                oldPwd = inputPassWord
                                reset("Y")
                                newPassCodeViewModel.padShuffle()
                            } else {
                                if (oldPwd == inputPassWord) {

                                    CoroutineScope(Dispatchers.IO).launch {
                                        this@NewPassCodeActivity.newPassCodeViewModel.joinPost(inputPassWord)
                                    }

                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            binding.loading.visibility = View.VISIBLE
                                            this@NewPassCodeActivity.newPassCodeViewModel.joinResponse.collect {
                                                when (it) {
                                                    is NewPassCodeViewModel.JoinState.Success -> {
                                                        PrefsHelper.write("accessToken", it.isSuccess.accessToken)
                                                        PrefsHelper.write("memberId", it.isSuccess.memberId)
                                                        PrefsHelper.write("refreshToken", it.isSuccess.refreshToken)
                                                        PrefsHelper.write("inputPinNumber", inputPassWord)
                                                        PrefsHelper.write("passCodeModal", "N")


                                                        dataNexusViewModel.putName(this@NewPassCodeActivity.newPassCodeViewModel.getName())
                                                        dataNexusViewModel.putPassWord(inputPassWord)
                                                        dataNexusViewModel.putPassWordModal("N")
                                                        dataNexusViewModel.putAccessToken(it.isSuccess.accessToken)
                                                        dataNexusViewModel.putExpiredAt(it.isSuccess.expiredAt)
                                                        dataNexusViewModel.putMemberId(it.isSuccess.memberId)
                                                        dataNexusViewModel.putRefreshToken(it.isSuccess.refreshToken)

                                                        startActivity(getJoinSuccessActivity(this@NewPassCodeActivity))
                                                        finishAffinity()
                                                        binding.loading.visibility = View.GONE

                                                    }

                                                    is NewPassCodeViewModel.JoinState.Failure -> {
                                                        binding.loading.visibility = View.GONE
                                                    }

                                                    else -> {
                                                        binding.loading.visibility = View.GONE
                                                    }
                                                }
                                            }
                                        } catch (exception: Exception) {
                                            binding.loading.visibility = View.GONE
                                            exception.printStackTrace()
                                        }
                                    }
                                } else {
                                    oldPwd = ""
                                    reset("N")
                                    newPassCodeViewModel.padShuffle()
                                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                    binding.subTitleTv.text =
                                        newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = true, "", "")
                                }
                            }
                        }
                    }
                }
            }

            PassCodeState.LOGIN.id -> {
                PrefsHelper.write("name", dataNexusViewModel.getName())

                var isDialogVisible = false
                if (inputPassWord.length == 6) {
                    CoroutineScope(Dispatchers.IO).launch {
                        this@NewPassCodeActivity.newPassCodeViewModel.getAccessTokenChk(grantType = "client_credentials")
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        binding.loading.visibility = View.VISIBLE

                        this@NewPassCodeActivity.newPassCodeViewModel.tokenChk.collect { response ->
                            when (response) {
                                is NewPassCodeViewModel.AccessTokenState.Success -> {

                                    launch(Dispatchers.IO) {
                                        this@NewPassCodeActivity.newPassCodeViewModel.getAuthPin(pinNumber = inputPassWord)
                                    }

                                    this@NewPassCodeActivity.newPassCodeViewModel.pinChk.collect { authData ->
                                        when (authData) {
                                            is NewPassCodeViewModel.AuthPinState.Success -> {
                                                LogUtil.e("=== 핀번호 검증 Success === ${authData.data.memberId}")

                                                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.LOGIN)
                                                binding.subTitleTv.text =
                                                    newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.LOGIN, oneMoreChk = false, equalsChk = false, "", "")
                                                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))


                                                launch(Dispatchers.Main) {
                                                    // 기존 고객 판별 ( "N : 기존 고객 아님" , "Y: 기존 고객임" )
                                                    if (authData.data.isExistMember == "N") {
                                                        when (binding.bioTitleTv.text) {
                                                            getString(R.string.face_title_retry) -> {}
                                                            else -> {
                                                                LogUtil.e("여기 Prefs Fido : ${PrefsHelper.read("isFido", "N")}")
                                                                LogUtil.e("여기 DataStore Fido : ${this@NewPassCodeActivity.dataNexusViewModel.getIsFido()}")
                                                                if (!this@NewPassCodeActivity.newPassCodeViewModel.bioChkSelector(
                                                                        activity = this@NewPassCodeActivity,
                                                                        bioChkIv = binding.bioChkIv,
                                                                        titleView = binding.bioTitleTv,
                                                                        retryIv = binding.bioRetryIv,
                                                                        chk = binding.bioChkIv.isSelected
                                                                    )
                                                                ) {
                                                                    PrefsHelper.write("isFido", "N")
                                                                    this@NewPassCodeActivity.dataNexusViewModel.putIsFido("N")
                                                                } else {
                                                                    PrefsHelper.write("isFido", "Y")
                                                                    this@NewPassCodeActivity.dataNexusViewModel.putIsFido("Y")
                                                                }
                                                            }
                                                        }
                                                        ssnBridge()

                                                    } else {
                                                        // 기존 고객이며 간편비밀번호 변경 유/무 체크 (null or "" : 변경 전)
                                                        if (authData.data.passwordUpdatedAt.isEmpty()) {
                                                            // 보안 강화 안내를 통하여 비밀번호 변경 처리
                                                            LogUtil.e("기고객이고 비밀번호 변경 전입니다.")

                                                            if (!isDialogVisible) {
                                                                isDialogVisible = true

                                                                AppConfirmDF.newInstance(
                                                                    getString(R.string.security_confirm_title),
                                                                    getString(R.string.security_confirm_content),
                                                                    false,
                                                                    positiveStrRes = R.string.security_confirm_ok_title,
                                                                    positiveAction = {
                                                                        startActivity(getNewJoinActivity(this@NewPassCodeActivity))
                                                                    },
                                                                    negativeStrRes = R.string.security_confirm_cancel_title,
                                                                    negativeAction = {
                                                                        ssnBridge()
                                                                    },
                                                                    dismissAction = { }
                                                                ).show(supportFragmentManager, "기고객 안내")
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            is NewPassCodeViewModel.AuthPinState.AuthPinException -> {
                                                LogUtil.e("=== 핀번호 검증 AuthPinException === ${authData.authPinVo.passwordIncorrectCount}")

                                                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                                                    binding.loading.visibility = View.GONE
                                                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                                    reset("N")

                                                    var count = authData.authPinVo.passwordIncorrectCount
                                                    this@NewPassCodeActivity.newPassCodeViewModel.incorrectCount(count = count)

                                                    // 5회 미만으로 틀렸을때
                                                    if (count.toInt() <= 4) {
                                                        launch(Dispatchers.IO) {
                                                            PrefsHelper.write("passCodeModal", "N")
                                                            this@NewPassCodeActivity.dataNexusViewModel.putPassWordModal("N")
                                                        }
                                                        binding.subTitleTv.text =
                                                            newPassCodeViewModel.subTitleTxt(
                                                                this@NewPassCodeActivity,
                                                                inputPassWord,
                                                                PassCodeState.LOGIN,
                                                                oneMoreChk = false,
                                                                equalsChk = true,
                                                                count,
                                                                ""
                                                            )
                                                    } else {
                                                        isDialogVisible = false
                                                        launch(Dispatchers.IO) {
                                                            PrefsHelper.write("passCodeModal", "Y")
                                                            this@NewPassCodeActivity.dataNexusViewModel.putPassWordModal("Y")
                                                        }

                                                        launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                                                            binding.subTitleTv.text = "비밀번호가 맞지 않아요 (5/5)"
                                                            binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))

                                                            if (!isDialogVisible) {
                                                                isDialogVisible = true

                                                                AppConfirmDF.newInstance(
                                                                    getString(R.string.reset_pwd_title),
                                                                    getString(R.string.reset_pwd_content),
                                                                    false,
                                                                    positiveStrRes = R.string.reset_pwd_btn_title,
                                                                    positiveAction = {
                                                                        startActivity(getNewJoinActivity(this@NewPassCodeActivity))
                                                                    },
                                                                    negativeStrRes = R.string.dismiss,
                                                                    negativeAction = {
                                                                        reset("N")
                                                                    },
                                                                    dismissAction = {}
                                                                ).show(supportFragmentManager, "비밀번호 5회 불일치")
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            is NewPassCodeViewModel.AuthPinState.Failure -> {
                                                binding.loading.visibility = View.GONE
                                                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                                reset("N")
                                            }

                                            else -> {
                                                LogUtil.e("=== 핀번호 검증 Loading ===")
                                                isDialogVisible = true
                                            }
                                        }
                                    }

                                }

                                is NewPassCodeViewModel.AccessTokenState.Failure -> {
                                    LogUtil.e("=== 토큰 검증 Fail === ${response.message}")
                                }

                                else -> {
                                    LogUtil.e("=== 토큰 검증 Loading ===")
                                }
                            }
                        }
                    }

                } else {
                    binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.LOGIN)
                    binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.LOGIN, oneMoreChk = false, equalsChk = false, "", "")
                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))
                }
            }

            PassCodeState.CHANGE.id -> {
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.CHANGE)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.CHANGE, oneMoreChk = false, equalsChk = false, "", "")
                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))
                if (inputPassWord.length > 2) {
                    if (newPassCodeViewModel.repeatVerifyNumber(passWord = inputPassWord) ||
                        newPassCodeViewModel.continuousVerifyNumber(passWord = inputPassWord) ||
                        newPassCodeViewModel.birthVerifyNumber(passWord = inputPassWord) ||
                        newPassCodeViewModel.phoneVerifyNumber(passWord = inputPassWord)
                    ) {
                        // 검증 실패시 초기화
                        reset("N")
                        binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                        binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = false, "", "")

                    } else {
                        // 정규식 통과 다음 로직 진행
                        if (inputPassWord.length == 6) {
                            if (oldPwd.isEmpty()) {
                                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = true, equalsChk = false, "", "")
                                oldPwd = inputPassWord
                                reset("Y")
                                newPassCodeViewModel.padShuffle()
                            } else {
                                if (oldPwd == inputPassWord) {
                                    LogUtil.e("입력한 두 비밀번호 같음")
                                    val memberPinModel = MemberPinModel(newPassCodeViewModel.memberId, inputPassWord)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        this@NewPassCodeActivity.newPassCodeViewModel.putAuthPin(memberPinModel = memberPinModel)
                                    }

                                    CoroutineScope(Dispatchers.Main).launch {
                                        try {
                                            binding.loading.visibility = View.VISIBLE
                                            this@NewPassCodeActivity.newPassCodeViewModel.pinUpdateChk.collect {
                                                when (it) {
                                                    is NewPassCodeViewModel.AuthPinPutState.Success -> {
                                                        LogUtil.e("=== PinUpdate Success === ")
                                                        PrefsHelper.write("passCodeModal", "N")
                                                        dataNexusViewModel.putPassWordModal("N")
                                                        ssnBridge()
                                                    }

                                                    is NewPassCodeViewModel.AuthPinPutState.Failure -> {
                                                        binding.loading.visibility = View.GONE
                                                        LogUtil.e("=== PinUpdate Failure === ${it.message}")
                                                    }

                                                    else -> {
                                                        binding.loading.visibility = View.GONE
                                                        LogUtil.e("=== PinUpdate Loading === $it")
                                                    }
                                                }
                                            }
                                        } catch (ex: Exception) {
                                            binding.loading.visibility = View.GONE
                                            ex.printStackTrace()
                                            LogUtil.e("간편비밀번호 변경 실패")
                                        }
                                    }
                                } else {
                                    LogUtil.e("입력한 두 비밀번호 다름")
                                    oldPwd = ""
                                    reset("N")
                                    newPassCodeViewModel.padShuffle()
                                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                    binding.subTitleTv.text =
                                        newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = true, "", "")
                                }
                            }
                        }
                    }
                }
            }

            PassCodeState.PURCHASE.id -> {
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.PURCHASE)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.PURCHASE, oneMoreChk = false, equalsChk = false, "", "")
                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))

                when (binding.bioTitleTv.text) {
                    getString(R.string.face_title_retry) -> {}
                    else -> {
                        if (!this@NewPassCodeActivity.newPassCodeViewModel.bioChkSelector(
                                activity = this@NewPassCodeActivity,
                                bioChkIv = binding.bioChkIv,
                                titleView = binding.bioTitleTv,
                                retryIv = binding.bioRetryIv,
                                chk = binding.bioChkIv.isSelected
                            )
                        ) {
                            PrefsHelper.write("isFido", "N")
                            this@NewPassCodeActivity.dataNexusViewModel.putIsFido("N")
                        } else {
                            PrefsHelper.write("isFido", "Y")
                            this@NewPassCodeActivity.dataNexusViewModel.putIsFido("Y")
                        }
                    }
                }


                if (newPassCodeViewModel.isFido == "Y") {
                    CoroutineScope(Dispatchers.Main).launch {
                        this@NewPassCodeActivity.newPassCodeViewModel.authenticateUsingBiometrics(
                            status = PassCodeState.LOGIN,
                            activity = this@NewPassCodeActivity,
                            context = this@NewPassCodeActivity,
                            bioLayout = binding.bioLayout,
                            bioChkIv = binding.bioChkIv,
                            titleView = binding.bioTitleTv,
                            retryIv = binding.bioRetryIv
                        )

                        this@NewPassCodeActivity.newPassCodeViewModel.bioIsLoginState.collect { state ->
                            biometricPrompt = BiometricPrompt(this@NewPassCodeActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    purchaseBridge()
                                }
                            })
                        }
                    }
                }

                var isDialogVisible = false

                if (inputPassWord.length == 6) {
                    newPassCodeViewModel.localCount()

                    coroutineScope.launch {
                        binding.loading.visibility = View.GONE
//                        binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                        reset("N")

                        this@NewPassCodeActivity.newPassCodeViewModel.incorrectCount(count = newPassCodeViewModel.getLocalCount().toString())

                        if (inputPassWord != newPassCodeViewModel.inputPinNumber) {
                            // 5회 미만으로 틀렸을때
                            if (newPassCodeViewModel.getLocalCount() <= 4) {
                                launch(Dispatchers.IO) {
                                    PrefsHelper.write("passCodeModal", "N")
                                    this@NewPassCodeActivity.dataNexusViewModel.putPassWordModal("N")
                                }
                                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                binding.subTitleTv.text =
                                    newPassCodeViewModel.subTitleTxt(
                                        this@NewPassCodeActivity,
                                        inputPassWord,
                                        PassCodeState.LOGIN,
                                        oneMoreChk = false,
                                        equalsChk = true,
                                        newPassCodeViewModel.getLocalCount().toString(),
                                        ""
                                    )
                            } else {
                                isDialogVisible = false
                                launch(Dispatchers.IO) {
                                    PrefsHelper.write("passCodeModal", "Y")
                                    this@NewPassCodeActivity.dataNexusViewModel.putPassWordModal("Y")
                                }

                                launch(Dispatchers.Main) {
                                    binding.subTitleTv.text = "비밀번호가 맞지 않아요 (5/5)"
                                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))

                                    if (!isDialogVisible) {
                                        isDialogVisible = true

                                        AppConfirmDF.newInstance(
                                            getString(R.string.reset_pwd_title),
                                            getString(R.string.reset_pwd_content),
                                            false,
                                            positiveStrRes = R.string.reset_pwd_btn_title,
                                            positiveAction = {
                                                startActivity(getNewJoinActivity(this@NewPassCodeActivity))
                                            },
                                            negativeStrRes = R.string.dismiss,
                                            negativeAction = {
                                                reset("N")
                                            },
                                            dismissAction = {}
                                        ).show(supportFragmentManager, "비밀번호 5회 불일치")
                                    }
                                }
                            }
                        } else {
                            LogUtil.e("청약 신청 프로세스 시작")
                            purchaseBridge()
                        }
                    }
                } else {
                    binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.LOGIN)
                    binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.LOGIN, oneMoreChk = false, equalsChk = false, "", "")
                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))
                }

            }


            PassCodeState.CLEAR.id -> {
                binding.topTitleTv.text = newPassCodeViewModel.topTitleTxt(this@NewPassCodeActivity, PassCodeState.CLEAR)
                binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.CLEAR, oneMoreChk = false, equalsChk = false, "", "first")
                binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_757983))

                if (!this@NewPassCodeActivity.status) {
                    if (passWord.length == 6) {
                        if (inputPassWord != newPassCodeViewModel.inputPinNumber) {
                            reset("N")
                            binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                            binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.CLEAR, oneMoreChk = false, equalsChk = true, "", "first")
                        } else {
                            binding.subTitleTv.text = newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.CLEAR, oneMoreChk = true, equalsChk = false, "", "second")
                            reset("Y")
                            this@NewPassCodeActivity.status = true
                        }
                    }
                } else {
                    if (inputPassWord.length > 2) {
                        if (newPassCodeViewModel.repeatVerifyNumber(passWord = inputPassWord) ||
                            newPassCodeViewModel.continuousVerifyNumber(passWord = inputPassWord) ||
                            newPassCodeViewModel.birthVerifyNumber(passWord = inputPassWord) ||
                            newPassCodeViewModel.phoneVerifyNumber(passWord = inputPassWord)
                        ) {
                            // 검증 실패시 초기화
                            reset("N")
                            binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                            binding.subTitleTv.text =
                                newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = false, "", "second")

                        } else {
                            if (inputPassWord.length == 6) {
                                if (inputPassWord == dataNexusViewModel.getPassWord()) {
                                    reset("N")
                                    binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                    binding.subTitleTv.text =
                                        newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.CLEAR, oneMoreChk = true, equalsChk = true, "", "second")
                                } else {
                                    // 정규식 통과 다음 로직 진행
                                    if (oldPwd.isEmpty()) {
                                        binding.subTitleTv.text =
                                            newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = true, equalsChk = false, "", "third")
                                        oldPwd = inputPassWord
                                        reset("Y")
                                    } else {
                                        if (oldPwd == inputPassWord) {
                                            val memberPinModel = MemberPinModel(newPassCodeViewModel.memberId, inputPassWord)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                this@NewPassCodeActivity.newPassCodeViewModel.putAuthPin(memberPinModel = memberPinModel)
                                            }

                                            CoroutineScope(Dispatchers.Main).launch {
                                                try {
                                                    binding.loading.visibility = View.VISIBLE
                                                    this@NewPassCodeActivity.newPassCodeViewModel.pinUpdateChk.collect {
                                                        when (it) {
                                                            is NewPassCodeViewModel.AuthPinPutState.Success -> {
                                                                LogUtil.e("=== PinUpdate Success === ")
                                                                PrefsHelper.write("inputPinNumber", inputPassWord)
                                                                PrefsHelper.write("passCodeModal", "N")
                                                                dataNexusViewModel.putPassWordModal("N")
                                                                dataNexusViewModel.putPassWord(inputPassWord)


                                                                DialogManager.openDialog(
                                                                    this@NewPassCodeActivity,
                                                                    getString(R.string.reset_success_pwd_title),
                                                                    getString(R.string.reset_success_pwd_content),
                                                                    getString(R.string.reset_success_pwd_btn_title),
                                                                    this@NewPassCodeActivity
                                                                )
                                                            }

                                                            is NewPassCodeViewModel.AuthPinPutState.Failure -> {
                                                                binding.loading.visibility = View.GONE
                                                                LogUtil.e("=== PinUpdate Failure === ${it.message}")
                                                            }

                                                            else -> {
                                                                binding.loading.visibility = View.GONE
                                                                LogUtil.e("=== PinUpdate Loading === $it")
                                                            }
                                                        }
                                                    }
                                                } catch (ex: Exception) {
                                                    binding.loading.visibility = View.GONE
                                                    ex.printStackTrace()
                                                    LogUtil.e("간편비밀번호 변경 실패")
                                                }
                                            }
                                        }

                                        // 입력한 두 비밀번호가 다름
                                        else {
                                            LogUtil.e("입력한 두 비밀번호 다름")
                                            oldPwd = ""
                                            reset("N")
                                            binding.subTitleTv.setTextColor(ContextCompat.getColor(this@NewPassCodeActivity, R.color.c_F95D5D))
                                            binding.subTitleTv.text =
                                                newPassCodeViewModel.subTitleTxt(this@NewPassCodeActivity, inputPassWord, PassCodeState.FIRST, oneMoreChk = false, equalsChk = true, "", "second")

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun ssnBridge() {
        var authActivityStarted = false

        CoroutineScope(Dispatchers.Main).launch {
            this@NewPassCodeActivity.newPassCodeViewModel.ssnChk.collect { ssnData ->
                when (ssnData) {
                    is NewPassCodeViewModel.SsnState.Success -> {
                        if (!authActivityStarted) {
                            authActivityStarted = true
                            PrefsHelper.write("passCodeModal", "N")
                            this@NewPassCodeActivity.dataNexusViewModel.putPassWordModal("N")
                            if (ssnData.isSuccess.ssnYn == "N") {
                                PrefsHelper.write("ssnYn", ssnData.isSuccess.ssnYn)
                                PrefsHelper.write("publicKey", ssnData.isSuccess.publicKey)
                                binding.loading.visibility = View.GONE
                                startActivity(getAuthActivity(this@NewPassCodeActivity))
                                finishAffinity()
                            } else {
                                binding.loading.visibility = View.VISIBLE
                                PrefsHelper.write("ssnYn", "Y")
                                // 기존 MainActivity로 보내는 이동 스텍을 투자 성향 분석 완료 여부에 따라 분기 처리
                                // member 조회 데이터에서 preference 데이터로 구분
                                // preference null 일 경우 투자 성향 분석 한번도 진행하지 않은 사람
                                CoroutineScope(Dispatchers.IO).launch {
                                    this@NewPassCodeActivity.newPassCodeViewModel.getMemberData()
                                }

                                CoroutineScope(Dispatchers.Main).launch {
                                    investBridge()
                                }
                            }
                        }

                    }

                    is NewPassCodeViewModel.SsnState.Failure -> {
                        binding.loading.visibility = View.GONE
                    }

                    else -> {
                        binding.loading.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun investBridge() {
        var investActivityStarted = false
        binding.loading.visibility = View.VISIBLE
        coroutineScope.launch(coroutineScope.coroutineContext + Dispatchers.Main) {
            this@NewPassCodeActivity.newPassCodeViewModel.memberInfo.collect {
                when (it) {
                    is NewPassCodeViewModel.MemberInfoState.Success -> {
                        if (!investActivityStarted) {
                            investActivityStarted = true
                            if (it.memberVo.preference == null) { // 배포시 주석 해제
                                LogUtil.e("preference is Null ( 투자 성향 분석 한번도 진행 안한 회원 )")
                                // 투자성향 분석 테스트
                                startActivity(InvestmentIntroActivity.getBackToMainIntent(this@NewPassCodeActivity, memberVo = it.memberVo))
                                BackPressedUtil().activityCreateFinish(this@NewPassCodeActivity,this@NewPassCodeActivity)
                            } else {
                                LogUtil.e("preference is Not Null ( 투자 성향 분석 한번이라도 진행한 회원 )")
                                binding.loading.visibility = View.GONE
                                startActivity(getMainActivity(this@NewPassCodeActivity, it.memberVo))
                                BackPressedUtil().activityCreateFinish(this@NewPassCodeActivity,this@NewPassCodeActivity)
                            }
                            finishAffinity()
                        }
                    }

                    is NewPassCodeViewModel.MemberInfoState.Failure -> {
                        LogUtil.e("회원 정보 조회 실패: ${it.message}")
                        binding.loading.visibility = View.GONE
                    }

                    else -> {
                        LogUtil.e("회원 정보 조회 로딩: $it")
                        binding.loading.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun purchaseBridge() {
        var purchaseActivityStarted = false

        CoroutineScope(Dispatchers.IO).launch {
            newPassCodeViewModel.buyPurchase()
        }

        CoroutineScope(Dispatchers.Main).launch {
            LogUtil.e("intent ptWithDrawDate : ${intent.getStringExtra("ptWithDrawDate")}")

            this@NewPassCodeActivity.newPassCodeViewModel.purchaseChk.collect {
                when (it) {
                    // HTTP Code : 200 ( 청약 신청 성공 )
                    is NewPassCodeViewModel.PurchaseState.Success -> {
                        LogUtil.d("Purchase Success : ${it.responseCode} | ${it.isSuccess?.data}")
                        if (!purchaseActivityStarted) {
                            purchaseActivityStarted = true
                            binding.loading.visibility = View.GONE
                            binding.loading.visibility = View.GONE
                            startActivity(getPurchaseResultActivity(
                                this@NewPassCodeActivity,
                                intent.getStringExtra("ptWithDrawDate").default(),
                                it.responseCode,
                                it.isSuccess?.message.default(),
                                it.isSuccess?.subMessage.default()
                                )
                            )
                            finish()
                        }
                    }
                    is NewPassCodeViewModel.PurchaseState.HttpFailure -> {
                        LogUtil.d("Purchase Fail : ${it.responseCode} | ${it.baseVo?.message}")
                        if (!purchaseActivityStarted) {
                            purchaseActivityStarted = true
                            binding.loading.visibility = View.GONE
                            startActivity(getPurchaseResultActivity(
                                this@NewPassCodeActivity,
                                intent.getStringExtra("ptWithDrawDate").default(),
                                it.responseCode,
                                it.baseVo?.message.default(),
                                it.baseVo?.subMessage.default())
                            )
                            finish()
                        }
                    }

                    // HTTP Code : 500
                    is NewPassCodeViewModel.PurchaseState.BaseFailure -> {
                        if (!purchaseActivityStarted) {
                            purchaseActivityStarted = true
                            binding.loading.visibility = View.GONE
                            startActivity(getPurchaseResultActivity(
                                this@NewPassCodeActivity,
                                intent.getStringExtra("ptWithDrawDate").default(),
                                500,
                               it.baseVo?.message.default(),
                                it.baseVo?.subMessage.default())
                            )
                            finish()
                        }

                    }
                    else -> {
                        binding.loading.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            9999 -> {
                LogUtil.e("여기로 들어왔음 : $data ${PassCodeState.LOGIN}")
            }
        }
    }

    private fun extractStatusCode(errorMessage: String): Int {
        val regex = Regex("""(\d{3})""")
        val matchResult = regex.find(errorMessage)
        return matchResult?.value?.toInt() ?: -1
    }


    companion object {
        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        fun getMainActivity(context: Context, memberVo: MemberVo): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("ssnYn", memberVo.ssn)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return intent
        }

        fun getPassCodeActivity(context: Context): Intent {
            val intent = Intent(context, NewPassCodeActivity::class.java)
            intent.putExtra("Step", "2")
            return Intent(context, NewPassCodeActivity::class.java)
        }

        fun getAuthActivity(context: Context): Intent {
            val intent = Intent(context, AuthenticationActivity::class.java)
            intent.putExtra("viewType", 5000)
            return intent
        }

        // 로그인/서비스 둘러보기 화면 이동
        fun getIntroActivity(context: Context): Intent {
            val intent = Intent(context, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("another", "another")
            return intent
        }

        // 비밀번호 재설정
        fun getNewJoinActivity(context: Context): Intent {
            val intent = Intent(context, NewJoinActivity::class.java)
            intent.putExtra("Step","3")
            return intent
        }

        // 회원가입 성공 화면 이동
        fun getJoinSuccessActivity(context: Context): Intent {
            return Intent(context, NewJoinSuccessActivity::class.java)
        }

        // 청약 신청 결과 화면 이동
        fun getPurchaseResultActivity(context: Context, ptWithDrawDate: String, responseCode: Int, message:String, subMessage: String): Intent {
            val intent = Intent(context, PurchaseResultActivity::class.java)
            intent.putExtra("ptWithDrawDate", ptWithDrawDate)
            intent.putExtra("responseCode", responseCode)
            intent.putExtra("message", message)
            intent.putExtra("subMessage", subMessage)
            return intent
        }

        // 청약 신청 취소 화면 삭제로 인하여 ErrorActivity로 전환
        fun getErrorActivity(context: Context): Intent {
            return Intent(context, ErrorActivity::class.java)
        }
    }
}
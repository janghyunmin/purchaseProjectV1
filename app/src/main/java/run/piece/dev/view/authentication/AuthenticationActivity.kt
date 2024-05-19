package run.piece.dev.view.authentication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.gms.common.util.Base64Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import run.piece.dev.App
import run.piece.dev.BuildConfig
import run.piece.dev.R
import run.piece.dev.data.api.NetworkInfo
import run.piece.dev.data.api.RetrofitService
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.authentication.CallUserNameSsnAuthModel
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.authentication.CallUsernameAuthModel
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.consent.ConsentList
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.BaseDTO
import run.piece.dev.databinding.ActivityAuthenticationBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.passcode.PassCodeViewModel
import run.piece.dev.widget.utils.AuthChkClicked
import run.piece.dev.widget.utils.DialogManager
import run.piece.dev.widget.utils.KeyboardVisibilityUtils
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private val viewModel by viewModels<AuthInputViewModel>()
    private val passCodeViewModel by viewModels<PassCodeViewModel>() // 간편비밀번호 ViewModel - jhm 2023/02/20
    private var imm: InputMethodManager? = null

    // 주민등록번호 실명인증 요청 API - jhm 2022/10/26
    val apiResponse: RetrofitService = NetworkInfo.getRetrofit().create(RetrofitService::class.java)
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    // 첫번째 입력 주민 번호 6자리 ArrayList - jhm 2023/02/21
    private var securityFirstList: ArrayList<String> = ArrayList()
    private var securityFirst: String = ""

    // 두번째 입력 주민 번호 7자리 ArrayList - jhm 2023/02/21
    private var securityLastList: ArrayList<String> = ArrayList()
    private var securityLast: String = ""
    private var mFirst: String = ""
    private var mLast: String = ""
    private var touchStatus: Boolean = false

    companion object {
        private var viewType: Int = 0
        private var passCodeData: Bundle = Bundle()

        // 최초 실명인증일때 넘겨받은 데이터 - jhm 2023/02/22
        private var name: String = ""
        private var cellPhoneNo: String = ""
        private var birthDay: String = ""
        private var gender: String = ""
        private var isFido: String = ""
        private var consentList: ArrayList<ConsentList> = arrayListOf()
        private var ci: String = ""
        private var di: String = ""
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        window.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        binding.apply {
            activity = this@AuthenticationActivity
            lifecycleOwner = this@AuthenticationActivity
            authViewModel = viewModel
            passcode = passCodeViewModel
            imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            keyboardVisibilityUtils =
                KeyboardVisibilityUtils(window, onShowKeyboard = { keyboardHeight ->
                    binding.svRoot.run {
                        smoothScrollTo(scrollX, scrollY + keyboardHeight)
                    }
                })

            intent?.let {
                viewType = it.getIntExtra("viewType",0)
            }

            CoroutineScope(Dispatchers.Main).launch {
                loading.visibility = View.GONE
                guardLayout.visibility = View.GONE

                // 개인정보 처리방침

                noticeCv.setOnClickListener {
                    val webUrl = if (BuildConfig.DEBUG) "http://fdev.piece.la:5503/terms?tab=CON1601" else "https://piece.run/terms?tab=CON1601"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)))
                    noticeTv.setTextColor(ContextCompat.getColor(this@AuthenticationActivity, R.color.g400_DADCE3))
                }

                securityFirstList.clear()
                securityLastList.clear()

                /******************************************* 공통 KeyPad Observer Start **************************************************************/
                first.requestFocus() // 포커스 이동 - jhm 2023/02/20
                first.isCursorVisible = true
                imm?.hideSoftInputFromWindow(first.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)  // 키보드 숨기기 - jhm 2023/02/20


                val userFirstObserver =
                    Observer { userFirstObserver: String ->
                        mFirst = userFirstObserver

                    }
                val userLastObserver =
                    Observer { userLastObserver: String ->
                        mLast = userLastObserver
                    }


                firstKeyPadSetting() // 첫번째 주민등록번호 입력 키패드 순차 번호 지정 - jhm 2023/02/20
                firstNumUI()
                showSoftKeyBoard("first", viewType)

                authViewModel?.getFirst()?.observe(this@AuthenticationActivity, userFirstObserver)
                authViewModel?.getLast()?.observe(this@AuthenticationActivity, userLastObserver)
                authViewModel?.getFirst()?.observe(this@AuthenticationActivity, Observer {
                    mFirst = it

                    if (mFirst.length == 6) {
                        if (touchStatus) {
                            onKeyPadClick("first", viewType)
                            first.requestFocus() // 포커스 이동 - jhm 2023/02/20
                            first.isCursorVisible = true
                            imm?.hideSoftInputFromWindow(first.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)  // 키보드 숨기기 - jhm 2023/02/20
                        } else {
                            onKeyPadClick("last", viewType)
                            last.requestFocus() // 포커스 이동 - jhm 2023/02/20
                            last.isCursorVisible = true
                            imm?.hideSoftInputFromWindow(last.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)  // 키보드 숨기기 - jhm 2023/02/20
                        }
                    } else {
                        onKeyPadClick("first", viewType)
                    }
                })
                authViewModel?.getLast()?.observe(this@AuthenticationActivity, Observer {
                    mLast = it
                    if (mLast.isNotEmpty()) {
                        clearIcon.visibility = View.VISIBLE
                        clearIcon.setOnClickListener {
                            if (mLast.length == 7) {
                                val anim = TranslateAnimation(
                                    0f,
                                    0f,
                                    bottomLayout.height.toFloat(),
                                    0f
                                )
                                anim.duration = 400
                                bottomLayout.animation = anim
                                bottomLayout.visibility = View.VISIBLE
                                guardLayout.visibility = View.VISIBLE
                            }

                            mLast = ""
                            securityLastList.clear()
                            last.setText("")

                            footerLayout.visibility = View.GONE
                        }

                        footerLayout.visibility = View.GONE
                    } else {
                        clearIcon.visibility = View.GONE
                    }
                })

                /******************************************* 공통 KeyPad Observer End **************************************************************/
                when (viewType) {
                    // 3000: 최초 로그인시 실명인증 - jhm 2023/01/18
                    3000 -> {
                        // 최초 로그인시 상단 back img 보이게 처리 - jhm 2023/02/16
                        backImg.visibility = View.VISIBLE
                        backImg.onThrottleClick {
                            BackPressedUtil().activityFinish(this@AuthenticationActivity,this@AuthenticationActivity)
                        }
                        onKeyPadClick("first", viewType)
                        firstNumUI()

                        parentLayout.setOnClickListener {
                            if (guardLayout.visibility == View.VISIBLE) {
                                guardLayout.visibility = View.GONE
                            }
                            val anim = TranslateAnimation(
                                0f,
                                0f,
                                0f,
                                bottomLayout.height.toFloat()
                            )
                            anim.duration = 400
                            bottomLayout.animation = anim
                            bottomLayout.visibility = View.GONE
                            guardLayout.visibility = View.GONE

                            if (mFirst.length == 6 && mLast.length == 7) {
                                footerLayout.visibility = View.VISIBLE
                            }
                        }

                        // 첫번째 주민번호 입력 editText 터치했을때 - jhm 2023/02/21
                        first.setOnTouchListener(OnTouchListener { v, event ->
                            v.setOnClickListener {
                                touchStatus = true
                                firstNumUI()
                                onKeyPadClick("first", viewType)
                            }
                            showSoftKeyBoard("first", viewType) // 프로그램적으로 keyboard 를 띄운다.
                            true // true 를 전달함으로서 시스템이 keyboard 를 띄우지 못하게 한다.
                        })
                        last.setOnTouchListener(OnTouchListener { view, motionEvent ->
                            view.setOnClickListener {
                                lastNumUI(viewType)
                                onKeyPadClick("last", viewType)
                                val anim = TranslateAnimation(
                                    0f,
                                    0f,
                                    0f,
                                    bottomLayout.height.toFloat()
                                )
                                anim.duration = 400
                                bottomLayout.animation = anim
                                bottomLayout.visibility = View.GONE
                                guardLayout.visibility = View.GONE

                            }
                            showSoftKeyBoard("last", viewType)
                            true
                        })

                        last.setOnFocusChangeListener { view, b ->
                            if (b) {
                                guardLayout.visibility = View.VISIBLE
                            }
                        }

                        intent?.let { intent ->
                            val bundle = intent.extras
                            bundle?.let {
                                name = it.getString("name").toString()
                                cellPhoneNo = it.getString("cellPhoneNo").toString()
                                birthDay = it.getString("birthDay").toString()
                                gender = it.getString("gender").toString();
                                isFido = it.getString("isFido", "N").toString();
                                consentList = it.getParcelableArrayList("consentList")!!
                                ci = it.getString("ci").toString();
                                di = it.getString("di").toString();
                            }
                        }
                    }

                    // 생체 등록 여부와 상관없이 미인증 고객 - jhm 2023/02/08
                    5000 -> {
                        backImg.visibility = View.GONE

                        onKeyPadClick("first", viewType)
                        firstNumUI()

                        parentLayout.setOnClickListener {
                            if (bottomLayout.visibility == View.VISIBLE) {

                                val anim = TranslateAnimation(
                                    0f,
                                    0f,
                                    0f,
                                    bottomLayout.height.toFloat()
                                )
                                anim.duration = 400
                                bottomLayout.animation = anim
                                bottomLayout.visibility = View.GONE
                                guardLayout.animation = anim
                                guardLayout.visibility = View.GONE


                                if (mFirst.length == 6 && mLast.length == 7) {

                                    Handler(Looper.getMainLooper()).postDelayed({
                                        footerLayout.visibility = View.VISIBLE
                                    }, 500)

                                }
                            }
                            if (guardLayout.visibility == View.VISIBLE) {
                                guardLayout.visibility = View.GONE
                            }
                        }

                        // 첫번째 주민번호 입력 editText 터치했을때 - jhm 2023/02/21
                        first.setOnTouchListener(OnTouchListener { v, event ->
                            v.setOnClickListener {
                                touchStatus = true
                                firstNumUI()
                                onKeyPadClick("first", viewType)
                            }
                            showSoftKeyBoard("first", viewType) // 프로그램적으로 keyboard 를 띄운다.
                            true // true 를 전달함으로서 시스템이 keyboard 를 띄우지 못하게 한다.
                        })
                        last.setOnTouchListener(OnTouchListener { view, motionEvent ->
                            view.setOnClickListener {
                                lastNumUI(viewType)
                                onKeyPadClick("last", viewType)
                                footerLayout.visibility = View.GONE

                                val anim = TranslateAnimation(
                                    0f,
                                    0f,
                                    0f,
                                    bottomLayout.height.toFloat()
                                )
                                anim.duration = 400
                                bottomLayout.animation = anim
                                bottomLayout.visibility = View.GONE
                                guardLayout.visibility = View.GONE
                            }
                            showSoftKeyBoard("last", viewType)
                            true
                        })

                        last.setOnFocusChangeListener { view, b ->
                            if (b) {
                                lastKeyPadSetting()
                                guardLayout.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }

        BackPressedUtil().activityCreate(this@AuthenticationActivity,this@AuthenticationActivity)
        BackPressedUtil().systemBackPressed(this@AuthenticationActivity,this@AuthenticationActivity)
    }

    // 각 EditText 터치시 아래 로직 실행 - jhm 2023/02/21
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showSoftKeyBoard(status: String, viewType: Int) {
        // 진동 객체 - jhm 2023/02/21
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        when (status) {
            "first" -> {
                binding.passcodeLayout.setBackgroundColor(this@AuthenticationActivity.getColor(R.color.c_ffffff))
                Glide.with(this@AuthenticationActivity).load(R.drawable.keypad_clear_icon_f).into(binding.clearImg)
                binding.guardLayout.visibility = View.GONE
                binding.code0.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code1.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code2.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code3.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code4.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code5.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code6.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code7.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code8.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.code9.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20))
                binding.clearText.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20))

                binding.first.requestFocus() // 포커스 이동 - jhm 2023/02/20
                binding.first.isCursorVisible = true
                imm?.hideSoftInputFromWindow(binding.first.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)  // 키보드 숨기기 - jhm 2023/02/20
                firstKeyPadSetting() // 첫번째 주민등록번호 입력 키패드 번호 지정 - jhm 2023/02/20

                // 초기화 버튼 OnClick - jhm 2023/02/21
                binding.clearText.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    securityFirstList.clear()
                    securityFirst = ""
                    binding.first.setText("")
                }

                // 뒷자리 1자리씩 지우기 - jhm 2023/02/21
                binding.clear.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size != 0)
                        removeNumber(status)
                }

                val anim = TranslateAnimation(0f, 0f, binding.passcodeLayout.height.toFloat(), 0f)
                anim.duration = 400
                binding.bottomLayout.animation = anim
                binding.bottomLayout.visibility = View.VISIBLE
                binding.guardLayout.visibility = View.GONE

                if (mFirst.length == 6) {
                    onKeyPadClick("first", viewType)
                }

                if (binding.passcodeLayout.visibility == View.VISIBLE) {
                    binding.footerLayout.visibility = View.GONE
                }
            }

            "last" -> {
                binding.passcodeLayout.setBackgroundColor(this@AuthenticationActivity.getColor(R.color.c_283a4a))
                Glide.with(this@AuthenticationActivity).load(R.drawable.keypad_clear_icon_l).into(binding.clearImg)
                binding.guardLayout.visibility = View.VISIBLE
                binding.code0.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code1.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code2.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code3.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code4.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code5.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code6.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code7.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code8.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.code9.setTextColor(this@AuthenticationActivity.getColor(R.color.text_alpha_20_white))
                binding.clearText.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))

                binding.last.requestFocus() // 포커스 이동 - jhm 2023/02/20
                binding.last.isCursorVisible = true
                imm?.hideSoftInputFromWindow(binding.last.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)  // 키보드 숨기기 - jhm 2023/02/20

                // 초기화 버튼 OnClick - jhm 2023/02/21
                binding.clearText.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    securityLastList.clear()
                    securityLast = ""
                    binding.last.setText("")
                }

                // 뒷자리 1자리씩 지우기 - jhm 2023/02/21
                binding.clear.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size != 0)
                        removeNumber(status)
                }

                val anim =
                    TranslateAnimation(0f, 0f, binding.passcodeLayout.height.toFloat(), 0f)
                anim.duration = 400
                binding.bottomLayout.animation = anim
                binding.bottomLayout.visibility = View.VISIBLE
                binding.guardLayout.visibility = View.VISIBLE

                if (binding.passcodeLayout.visibility == View.VISIBLE) {
                    binding.footerLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun firstKeyPadSetting() {
        passCodeViewModel.Code0.value = "0"
        passCodeViewModel.Code1.value = "1"
        passCodeViewModel.Code2.value = "2"
        passCodeViewModel.Code3.value = "3"
        passCodeViewModel.Code4.value = "4"
        passCodeViewModel.Code5.value = "5"
        passCodeViewModel.Code6.value = "6"
        passCodeViewModel.Code7.value = "7"
        passCodeViewModel.Code8.value = "8"
        passCodeViewModel.Code9.value = "9"
    }

    private fun lastKeyPadSetting() {
        val padList: MutableList<String?> = ArrayList()
        for (index in 0..9) {
            padList.add(index.toString())
        }
        padList.shuffle()
        passCodeViewModel.code0.value = padList[0]
        passCodeViewModel.code1.value = padList[1]
        passCodeViewModel.code2.value = padList[2]
        passCodeViewModel.code3.value = padList[3]
        passCodeViewModel.code4.value = padList[4]
        passCodeViewModel.code5.value = padList[5]
        passCodeViewModel.code6.value = padList[6]
        passCodeViewModel.code7.value = padList[7]
        passCodeViewModel.code8.value = padList[8]
        passCodeViewModel.code9.value = padList[9]
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun onKeyPadClick(status: String, viewType: Int) {
        // 진동 객체 - jhm 2023/02/21
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        when (status) {
            // 주민번호 앞자리 - jhm 2023/02/21
            "first" -> {
                binding.code0.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code0.text.toString())
                        mFirst = binding.code0.text.toString()
                    }
                    firstNumUI()
                }
                binding.code1.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code1.text.toString())
                        mFirst = binding.code1.text.toString()
                    }
                    firstNumUI()
                }
                binding.code2.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code2.text.toString())
                        mFirst = binding.code2.text.toString()
                    }
                    firstNumUI()
                }

                binding.code3.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code3.text.toString())
                        mFirst = binding.code3.text.toString()
                    }
                    firstNumUI()
                }
                binding.code4.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code4.text.toString())
                        mFirst = binding.code4.text.toString()
                    }
                    firstNumUI()
                }
                binding.code5.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code5.text.toString())
                        mFirst = binding.code5.text.toString()
                    }
                    firstNumUI()
                }
                binding.code6.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code6.text.toString())
                        mFirst = binding.code6.text.toString()
                    }
                    firstNumUI()
                }
                binding.code7.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code7.text.toString())
                        mFirst = binding.code7.text.toString()
                    }
                    firstNumUI()
                }
                binding.code8.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code8.text.toString())
                        mFirst = binding.code8.text.toString()
                    }
                    firstNumUI()
                }
                binding.code9.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size <= 5) {
                        securityFirstList.add(binding.code9.text.toString())
                        mFirst = binding.code9.text.toString()
                    }
                    firstNumUI()
                }

                // 초기화 버튼 OnClick - jhm 2023/02/21
                binding.clearText.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    securityFirstList.clear()
                    securityFirst = ""
                    binding.first.setText("")
                }

                // 뒷자리 1자리씩 지우기 - jhm 2023/02/21
                binding.clear.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityFirstList.size != 0)
                        removeNumber(status)
                }

            }

            // 주민번호 뒷자리 - jhm 2023/02/21
            "last" -> {
                binding.passcodeLayout.setBackgroundColor(this@AuthenticationActivity.getColor(R.color.c_283a4a))
                Glide.with(this@AuthenticationActivity).load(R.drawable.keypad_clear_icon_l).into(binding.clearImg)
                binding.guardLayout.visibility = View.VISIBLE
                binding.code0.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code1.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code2.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code3.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code4.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code5.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code6.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code7.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code8.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.code9.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))
                binding.clearText.setTextColor(this@AuthenticationActivity.getColorStateList(R.color.text_alpha_20_white))


                binding.code0.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code0.text.toString())
                        mLast = binding.code0.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code1.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code1.text.toString())
                        mLast = binding.code1.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code2.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code2.text.toString())
                        mLast = binding.code2.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code3.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code3.text.toString())
                        mLast = binding.code3.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code4.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code4.text.toString())
                        mLast = binding.code4.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code5.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code5.text.toString())
                        mLast = binding.code5.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code6.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code6.text.toString())
                        mLast = binding.code6.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code7.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code7.text.toString())
                        mLast = binding.code7.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code8.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code8.text.toString())
                        mLast = binding.code8.text.toString()
                        lastNumUI(viewType)
                    }
                }
                binding.code9.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    if (securityLastList.size <= 6) {
                        securityLastList.add(binding.code9.text.toString())
                        mLast = binding.code9.text.toString()
                        lastNumUI(viewType)
                    }
                }

                // 초기화 버튼 OnClick - jhm 2023/02/21
                binding.clearText.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    securityLastList.clear()
                    mLast = ""
                    binding.last.setText("")
                }

                // 뒷자리 1자리씩 지우기 - jhm 2023/02/21
                binding.clear.setOnClickListener {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 100));
                    removeNumber(status)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun firstNumUI() {
        try {
            binding.footerLayout.visibility = View.GONE
            binding.first.requestFocus() // 포커스 이동 - jhm 2023/02/20
            binding.first.isCursorVisible = true
            imm?.hideSoftInputFromWindow(binding.first.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)  // 키보드 숨기기 - jhm 2023/02/20

            if (securityFirstList.size == 0) {
                securityFirstList.clear()
                securityFirst = ""
                binding.first.setText("")
            } else if (securityFirstList.size == 1) {
                securityFirst = securityFirstList[0]
                binding.first.setText(securityFirst)
                binding.first.setSelection(binding.first.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else if (securityFirstList.size == 2) {
                securityFirst = securityFirstList[0] + securityFirstList[1]
                binding.first.setText(securityFirst)
                binding.first.setSelection(binding.first.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else if (securityFirstList.size == 3) {
                securityFirst =
                    securityFirstList[0] + securityFirstList[1] + securityFirstList[2]
                binding.first.setText(securityFirst)
                binding.first.setSelection(binding.first.length()) // 커서 위치 이동 - jhm 2023/02/21

            } else if (securityFirstList.size == 4) {
                securityFirst =
                    securityFirstList[0] + securityFirstList[1] + securityFirstList[2] + securityFirstList[3]
                binding.first.setText(securityFirst)
                binding.first.setSelection(binding.first.length()) // 커서 위치 이동 - jhm 2023/02/21

            } else if (securityFirstList.size == 5) {
                securityFirst =
                    securityFirstList[0] + securityFirstList[1] + securityFirstList[2] + securityFirstList[3] + securityFirstList[4]
                binding.first.setText(securityFirst)
                binding.first.setSelection(binding.first.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else {
                securityFirst =
                    securityFirstList[0] + securityFirstList[1] + securityFirstList[2] + securityFirstList[3] + securityFirstList[4] + securityFirstList[5]
                binding.first.setText(securityFirst)
                binding.first.setSelection(binding.first.length()) // 커서 위치 이동 - jhm 2023/02/21
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    // 기회원인지 새로운 회원인지 - jhm 2023/02/22
    @RequiresApi(Build.VERSION_CODES.O)
    private fun lastNumUI(viewType: Int) {
        try {
            if (securityLastList.size == 0) {
                securityLastList.clear()
                securityLast = ""
                binding.last.setText("")
            } else if (securityLastList.size == 1) {
                securityLast = securityLastList[0]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else if (securityLastList.size == 2) {
                securityLast = securityLastList[0] + securityLastList[1]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else if (securityLastList.size == 3) {
                securityLast = securityLastList[0] + securityLastList[1] + securityLastList[2]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21

            } else if (securityLastList.size == 4) {
                securityLast =
                    securityLastList[0] + securityLastList[1] + securityLastList[2] + securityLastList[3]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21

            } else if (securityLastList.size == 5) {
                securityLast =
                    securityLastList[0] + securityLastList[1] + securityLastList[2] + securityLastList[3] + securityLastList[4]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else if (securityLastList.size == 6) {
                securityLast =
                    securityLastList[0] + securityLastList[1] + securityLastList[2] + securityLastList[3] + securityLastList[4] + securityLastList[5]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21
            } else {
                securityLast =
                    securityLastList[0] + securityLastList[1] + securityLastList[2] + securityLastList[3] + securityLastList[4] + securityLastList[5] + securityLastList[6]
                binding.last.setText(securityLast)
                binding.last.setSelection(binding.last.length()) // 커서 위치 이동 - jhm 2023/02/21

                val anim = TranslateAnimation(
                    0f,
                    0f,
                    0f,
                    binding.bottomLayout.height.toFloat()
                )
                anim.duration = 400
                binding.bottomLayout.animation = anim
                binding.bottomLayout.visibility = View.GONE

                CoroutineScope(Dispatchers.Main).launch {
                    binding.footerLayout.visibility = View.VISIBLE

                    binding.authBtn.onThrottleClick {
                        binding.loading.visibility = View.VISIBLE
                        try {
                            // 인증문자 검증 완료 후 받은 publicKey - jhm 2023/02/22
                            var publicTemp: String = PrefsHelper.read("publicKey", "")

                            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
                            val bytePublicKey: ByteArray = Base64.getUrlDecoder().decode(publicTemp)
                            val publicKeySpec = X509EncodedKeySpec(bytePublicKey)
                            val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)

                            // 주민번호 - jhm 2023/02/22
                            var secretNumber: String = mFirst + mLast

                            // 만들어진 공개키 객체로 암호화 설정
                            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

                            val encryptedBytes = cipher.doFinal(secretNumber.toByteArray())
                            var encryptedText = Base64.getUrlEncoder().encodeToString(encryptedBytes)

                            when (viewType) {
                                // 최초 실명인증 - jhm 2023/02/23
                                3000 -> {
                                    val authChkClicked: AuthChkClicked =
                                        object : AuthChkClicked {
                                            override fun algDismiss() {
                                                passCodeData = Bundle()
                                                passCodeData.putString("name", name)
                                                passCodeData.putString("cellPhoneNo", cellPhoneNo)
                                                passCodeData.putString("birthDay", birthDay)
                                                passCodeData.putString("gender", gender)
                                                passCodeData.putString("isFido", isFido)
                                                passCodeData.putString("ci", ci)
                                                passCodeData.putString("di", di)
                                                passCodeData.putString("ssn", encryptedText) // 주민 등록 번호
                                                passCodeData.putParcelableArrayList("consentList", consentList)

                                                val intent = Intent(this@AuthenticationActivity, NewPassCodeActivity::class.java)
                                                intent.putExtra("Step", "1") // 최초일때
                                                intent.putExtras(passCodeData)
                                                startActivity(intent)
                                            }
                                        }

                                    apiResponse.postUserNameSsnAuth(
                                        callUserNameSsnAuthModel = CallUserNameSsnAuthModel(
                                            name,
                                            encryptedText,
                                            "Y",
                                            "Y",
                                            deviceId
                                        )
                                    ).enqueue(object : Callback<BaseDTO> {
                                        override fun onResponse(
                                            call: Call<BaseDTO>,
                                            response: Response<BaseDTO>
                                        ) {
                                            try {
                                                binding.loading.visibility = View.GONE
                                                if (response.code() == 200) {

                                                    DialogManager.openSsnChkDlg(
                                                        this@AuthenticationActivity,
                                                        "인증 완료",
                                                        "실명 인증이 완료되었어요.",
                                                        "확인",
                                                        this@AuthenticationActivity,
                                                        "성공",
                                                        auth_listener = authChkClicked,
                                                    )
                                                } else {
                                                    binding.loading.visibility = View.GONE
                                                    DialogManager.openSsnChkDlg(
                                                        this@AuthenticationActivity,
                                                        "인증 실패",
                                                        "실명 정보를 다시 확인해 주세요.",
                                                        "확인",
                                                        this@AuthenticationActivity,
                                                        "실패",
                                                        auth_listener = authChkClicked
                                                    )
                                                }
                                            } catch (ex: Exception) {
                                                binding.loading.visibility = View.GONE
                                                ex.printStackTrace()
                                                DialogManager.openSsnChkDlg(
                                                    this@AuthenticationActivity,
                                                    "인증 실패",
                                                    "실명 정보를 다시 확인해 주세요.",
                                                    "확인",
                                                    this@AuthenticationActivity,
                                                    "실패",
                                                    auth_listener = authChkClicked
                                                )
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<BaseDTO>,
                                            t: Throwable
                                        ) {
                                            binding.loading.visibility = View.GONE
                                            t.printStackTrace()
                                            DialogManager.openSsnChkDlg(
                                                this@AuthenticationActivity,
                                                "인증 실패",
                                                "실명 정보를 다시 확인해 주세요.",
                                                "확인",
                                                this@AuthenticationActivity,
                                                "실패",
                                                auth_listener = authChkClicked
                                            )
                                        }
                                    })
                                }
                                // 실명인증 안한 기존 회원 실명인증 - jhm 2023/02/23
                                5000 -> {
                                    val authChkClicked: AuthChkClicked =
                                        object : AuthChkClicked {
                                            override fun algDismiss() {
                                                val intent = Intent(
                                                    this@AuthenticationActivity,
                                                    MainActivity::class.java
                                                )
                                                startActivity(intent)
                                                finishAffinity()
                                            }
                                        }

                                    apiResponse.postUserNameAuth(
                                        callUsernameAuthModel = CallUsernameAuthModel(
                                            memberId,
                                            PrefsHelper.read("name",""),
                                            encryptedText,
                                            "Y",
                                            "Y",
                                            deviceId
                                        )
                                    ).enqueue(object : Callback<BaseDTO> {
                                        override fun onResponse(
                                            call: Call<BaseDTO>,
                                            response: Response<BaseDTO>
                                        ) {
                                            try {
                                                binding.loading.visibility = View.GONE
                                                if (response.code() == 200) {
                                                    DialogManager.openSsnChkDlg(
                                                        this@AuthenticationActivity,
                                                        "인증 완료",
                                                        "실명 인증이 완료되었어요.",
                                                        "확인",
                                                        this@AuthenticationActivity,
                                                        "성공",
                                                        auth_listener = authChkClicked,
                                                    )
                                                } else {
                                                    binding.loading.visibility = View.GONE
                                                    DialogManager.openSsnChkDlg(
                                                        this@AuthenticationActivity,
                                                        "인증 실패",
                                                        "실명 정보를 다시 확인해 주세요.",
                                                        "확인",
                                                        this@AuthenticationActivity,
                                                        "실패",
                                                        auth_listener = authChkClicked
                                                    )
                                                }
                                            } catch (ex: Exception) {
                                                binding.loading.visibility = View.GONE
                                                ex.printStackTrace()
                                                DialogManager.openSsnChkDlg(
                                                    this@AuthenticationActivity,
                                                    "인증 실패",
                                                    "실명 정보를 다시 확인해 주세요.",
                                                    "확인",
                                                    this@AuthenticationActivity,
                                                    "실패",
                                                    auth_listener = authChkClicked
                                                )
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<BaseDTO>,
                                            t: Throwable
                                        ) {
                                            binding.loading.visibility = View.GONE
                                            t.printStackTrace()
                                            DialogManager.openSsnChkDlg(
                                                this@AuthenticationActivity,
                                                "인증 실패",
                                                "실명 정보를 다시 확인해 주세요.",
                                                "확인",
                                                this@AuthenticationActivity,
                                                "실패",
                                                auth_listener = authChkClicked
                                            )
                                        }
                                    })
                                }
                            }

                        } catch (ex: Exception) {
                            ex.printStackTrace()

                            val appConfirmDF = AppConfirmDF.newInstance(
                                "인증실패",
                                "실명 정보를 다시 확인해 주세요.",
                                false,
                                R.string.confirm,
                                positiveAction = {},
                                dismissAction = {}
                            )
                            appConfirmDF.show(
                                supportFragmentManager,
                                "AuthenticationFail"
                            )

                            binding.loading.visibility = View.GONE

                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun removeNumber(status: String) {
        when (status) {
            "first" -> {
                try {
                    val index: Int = securityFirstList.size - 1
                    securityFirstList.removeAt(index)
                    firstNumUI()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            "last" -> {
                try {
                    if (securityLastList.size > 0) {
                        val index: Int = securityLastList.size - 1
                        securityLastList.removeAt(index)
                        lastNumUI(viewType)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        keyboardVisibilityUtils.detachKeyboardListeners()
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        when (viewType) {
            3000 -> {
                finish()
            }

            5000 -> {
                finishAffinity()
            }
        }
    }

    // RSA 암호화 - jhm 2023/02/22
    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(input: String, key: PublicKey): String {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypt = cipher.doFinal(input.toByteArray())
        return Base64.getUrlEncoder().encodeToString(encrypt)

        //Base64Utils.encode(encrypt)
    }

    // RSA - jhm 2023/02/22
    fun decrypt(input: String, key: PublicKey): String {
        var byteEncrypt: ByteArray = Base64Utils.decode(input)
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypt = cipher.doFinal(byteEncrypt)
        return String(decrypt)
    }
}
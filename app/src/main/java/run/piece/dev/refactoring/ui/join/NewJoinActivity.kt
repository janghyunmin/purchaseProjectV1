package run.piece.dev.refactoring.ui.join

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityJoinBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.join.dialog.JoinBDF
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.authentication.AuthenticationActivity
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.member.model.Consents
import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.domain.refactoring.member.model.request.PostSmsVerificationModel

@AndroidEntryPoint
class NewJoinActivity : AppCompatActivity() {
    private val viewModel: NewJoinViewModel by viewModels()
    private lateinit var binding: ActivityJoinBinding
    private lateinit var coroutineScope: CoroutineScope

    private lateinit var smsBDF: JoinBDF //문자 인증 팝업
    private lateinit var termsBDF: JoinBDF //약관 동의 팝업
    private val dataStoreViewModel by viewModels<DataNexusViewModel>()
    var selectedValue = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        coroutineScope = lifecycleScope

        binding.apply {
            activity = this@NewJoinActivity
            lifecycleOwner = this@NewJoinActivity
            joinViewModel = viewModel

            manBtn.isSelected = true
            womanBtn.isSelected = false

            backIv.onThrottleClick {
                BackPressedUtil().activityFinish(this@NewJoinActivity, this@NewJoinActivity)
            }
            verification = false
        }

        /*smsAgeFailDF = AppConfirmDF.newInstance(
            title = "가입 제한 안내",
            "외국인은 회원 가입할 수 없어요.",
            false,
            R.string.confirm,
            positiveAction = {},
            dismissAction = {}
        )
        smsAgeFailDF.show(supportFragmentManager, "smsAgeFailDF")*/

        coroutineScope.launch {
            launch(Dispatchers.Main) {
                this@NewJoinActivity.viewModel.viewModelScope.launch {
                    this@NewJoinActivity.viewModel.focusText(this@NewJoinActivity, binding.userNameTv, binding.nameInfoTv)
                    this@NewJoinActivity.viewModel.focusText(this@NewJoinActivity, binding.userBirthTv, binding.birthInfoTv)
                    this@NewJoinActivity.viewModel.focusText(this@NewJoinActivity, binding.phoneNumTv, binding.infoPhoneNumTv)
                    viewModel.userGender = this@NewJoinActivity.viewModel.genderSelect(binding.manBtn.text.toString())

                    this@NewJoinActivity.viewModel.userName.observe(this@NewJoinActivity) {
                        buttonSetting()
                    }

                    this@NewJoinActivity.viewModel.userBirth.observe(this@NewJoinActivity) {
                        buttonSetting()
                    }

                    this@NewJoinActivity.viewModel.userPhone.observe(this@NewJoinActivity) {
                        buttonSetting()
                    }
                }
            }

            // 약관 동의 리스트 수집 및 처리
            launch(Dispatchers.Main) {
                viewModel.consentList.collect { data ->
                    when (data) {
                        is NewJoinViewModel.ConsentState.Success -> {
                            termsBDF = JoinBDF.newTermsInstance(
                                webLink = viewModel.getConsentWebLink(),
                                consentList = data.consentList as ArrayList<ConsentVo>,
                                confirmEvent = { termsOkTv, dataList ->
                                    val tempList: ArrayList<Consents> = ArrayList()

                                    termsOkTv.onThrottleClick {
                                        if (termsOkTv.isSelected) {
                                            dataList.forEach { vo ->
                                                if (vo.consentCode == "CON1501") {
                                                    PrefsHelper.write("CON1501", if (vo.isChecked) "Y" else "N")
                                                    dataStoreViewModel.putCON1501(if (vo.isChecked) "Y" else "N")
                                                    vo.isMandatory = if (vo.isChecked) "Y" else "N"
                                                }

                                                tempList.add(
                                                    Consents(
                                                        consentCode = vo.consentCode,
                                                        isAgreement = if (vo.isChecked) "Y" else "N"
                                                    )
                                                )
                                            }

                                            tempList.forEach {
                                                Log.e("최종데이터", "${tempList.size} -> ${it.consentCode}, ${it.isAgreement}")
                                            }
                                            viewModel.sendConsentItemList = tempList
                                            settingSmsPopup()
                                            termsBDF.dismiss()
                                        }
                                    }
                                }
                            )
                            termsBDF.show(supportFragmentManager, "termsBDF")
                        }

                        is NewJoinViewModel.ConsentState.Failure -> {
//                            startActivity(ErrorActivity.getIntent(this@NewJoinActivity))
                        }

                        else -> {}
                    }
                }
            }

            //문자 전송 결과 수신 1회 호출
            launch(Dispatchers.Main) {
                viewModel.postSmsAuth.collect { data ->
                    when (data) {
                        is NewJoinViewModel.PostSmsAuthState.Success -> { //전송 response

                            when (data.postSmsAuthVo.rsltCd) {
                                // 본인인증 성공
                                "B000" -> {
                                    smsBDF.show(supportFragmentManager, "smsBDF")
                                }
                                // 본인인증 실패
                                else -> {
                                    AppConfirmDF.newInstance(
                                        "인증번호 발송 실패",
                                        "인증번호 발송에 실패했습니다.\n잠시 후 다시 시도해주세요.",
                                        false,
                                        R.string.confirm_text,
                                        positiveAction = {},
                                        dismissAction = {}
                                    ).show(supportFragmentManager, "SmsAuthFail")
                                }
                            }
                        }

                        is NewJoinViewModel.PostSmsAuthState.Failure -> {
                            AppConfirmDF.newInstance(
                                "인증번호 발송 실패",
                                "인증번호 발송에 실패했습니다.\n잠시 후 다시 시도해주세요.",
                                false,
                                R.string.confirm_text,
                                positiveAction = {},
                                dismissAction = {}
                            ).show(supportFragmentManager, "SmsAuthFail")
                        }

                        is NewJoinViewModel.PostSmsAuthState.BaseException -> {
                            AppConfirmDF.newInstance(
                                title = "가입 제한 안내",
                                "만 14세 이상만 회원으로 가입할 수 있어요.",
                                false,
                                R.string.confirm,
                                positiveAction = {},
                                dismissAction = {}
                            ).show(supportFragmentManager, "smsAgeFailDF")
                        }

                        else -> {}
                    }
                }
            }

            //문자 재전송 결과 수신 복수 호출 가능함
            launch(Dispatchers.Main) {
                viewModel.postReSmsAuth.collect { data ->
                    when (data) {
                        is NewJoinViewModel.PostReSmsAuthState.Success -> {}
                        is NewJoinViewModel.PostReSmsAuthState.Failure -> {}
                        else -> {}
                    }
                }
            }

            //문자 검증 결과 수신
            launch(Dispatchers.Main) {
                viewModel.postSmsVerification.collect { data ->
                    when (data) {
                        is NewJoinViewModel.PostSmsVerificationState.Success -> { //검증 response
                            when (data.postSmsVerificationVo.rsltCd) {
                                "B000" -> { //인증완료
                                    if (data.postSmsVerificationVo.publicKey.isEmpty()) { //기존에 인증을 완료한 회원
                                        termsBDF.dismiss()

                                        smsBDF.smsErrorLiveData.value = ""
                                        smsBDF.dismiss()

                                        val bundle = Bundle().apply {
                                            putString("name", viewModel.userName.value.default())
                                            putString("birthDay", viewModel.userBirth.value.default())
                                            putString("cellPhoneNo", viewModel.userPhone.value.default())
                                            putParcelableArrayList("consentList", viewModel.sendConsentItemList as ArrayList<out Parcelable>)
                                            putString("gender", viewModel.userGender)
                                            putString("isFido", "N")

                                            putString("ci", data.postSmsVerificationVo.ci)
                                            putString("di", data.postSmsVerificationVo.di)
                                        }

                                        PrefsHelper.write("name", viewModel.userName.value.default())

                                        Log.v("넘겨받은 Step : ", "${intent.getStringExtra("Step")}")

                                        intent?.let { step ->
                                            var tempStep = step.getStringExtra("Step")
                                            val intent = Intent(this@NewJoinActivity, NewPassCodeActivity::class.java)

                                            when (tempStep) {
                                                // 회원가입
                                                "1" -> {
                                                    intent.putExtra("Step", "1") // 최초일때
                                                }
                                                // 비밀번호 변경
                                                "3" -> {
                                                    intent.putExtra("Step", "3") // 비밀번호 변경
                                                }
                                                // 재인증
                                                "6" -> {
                                                    intent.putExtra("Step", "6") // 재인증
                                                }
                                            }
                                            intent.putExtras(bundle)
                                            startActivity(intent)
                                            finish()
                                        }


                                    } else {
                                        PrefsHelper.write("publicKey", data.postSmsVerificationVo.publicKey)

                                        val bundle = Bundle().apply {
                                            putString("name", viewModel.userName.value.default())
                                            putString("birthDay", viewModel.userBirth.value.default())
                                            putString("cellPhoneNo", viewModel.userPhone.value.default())
                                            putParcelableArrayList("consentList", viewModel.sendConsentItemList as ArrayList<out Parcelable>)
                                            putString("gender", viewModel.userGender)
                                            putString("isFido", "N")

                                            putString("ci", data.postSmsVerificationVo.ci)
                                            putString("di", data.postSmsVerificationVo.di)
                                        }

                                        val intent = Intent(this@NewJoinActivity, AuthenticationActivity::class.java)
                                        intent.putExtras(bundle)
                                        intent.putExtra("viewType", 3000)
                                        startActivity(intent)
                                    }
                                }

                                "B130" -> smsBDF.smsErrorLiveData.value = data.postSmsVerificationVo.rsltMsg //인증번호 오류 입력 건수 초과
                                else -> smsBDF.smsErrorLiveData.value = "유효하지 않은 인증번호에요."
                            }
                        }

                        is NewJoinViewModel.PostSmsVerificationState.Failure -> {
                            launch(Dispatchers.Main) {
                                smsBDF.dismiss()

                                // 탈퇴자 검증
                                try {
                                    AppConfirmDF.newInstance(
                                        "가입 제한 안내",
                                        "탈퇴한 회원은 30일 후 다시 가입할 수 있어요",
                                        false,
                                        R.string.confirm,
                                        strongText = "30일",
                                        positiveAction = {},
                                        dismissAction = {}
                                    ).show(supportFragmentManager, "DeleteMember")

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        is NewJoinViewModel.PostSmsVerificationState.BaseException -> {
                            launch(Dispatchers.Main) {
                                smsBDF.dismiss()

                                AppConfirmDF.newInstance(
                                    "본인 확인 실패",
                                    "본인 명의의 핸드폰으로만 인증이 가능해요.",
                                    false,
                                    R.string.confirm,
                                    positiveAction = {},
                                    dismissAction = {}
                                ).show(supportFragmentManager, "SmsAuthFail")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        window?.apply {
            // 캡쳐방지 Kotlin Ver
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        BackPressedUtil().activityCreate(this@NewJoinActivity, this@NewJoinActivity)
        BackPressedUtil().systemBackPressed(this@NewJoinActivity, this@NewJoinActivity)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        BackPressedUtil().activityFinish(this@NewJoinActivity,this@NewJoinActivity)
    }

    private fun settingSmsPopup() {
        val requestData = PostSmsAuthModel(
            txSeqNo = viewModel.txSeqNo,
            name = viewModel.userName.value.default(),
            birthday = viewModel.userBirth.value.default(),
            sexCd = viewModel.userGender,
            ntvFrnrCd = "L",
            telComCd = viewModel.telComCd,
            telNo = viewModel.userPhone.value.default(),
            agree1 = "Y",
            agree2 = "Y",
            agree3 = "Y",
            agree4 = "Y",
            otpNo = ""
        )

        // 문자 인증 팝업
        smsBDF = JoinBDF.newSMSInstance(
            smsReTryEvent = {
                viewModel.postReSmsAuth(requestData)  //재전송 API 호출
            }, smsVerificationEvent = { data ->

                val postSmsVerificationModel = PostSmsVerificationModel(
                    txSeqNo = viewModel.txSeqNo,
                    telNo = viewModel.userPhone.value.default(),
                    otpNo = data,
                    deviceId = PrefsHelper.read("deviceId", "")
                )
                viewModel.postSmsVerification(postSmsVerificationModel) //검증 API 호출
            })

        viewModel.postSmsAuth(requestData) //전송 API 호출
    }

    private fun buttonSetting() {
        //검증진행 : 생년월일, 핸드폰번호, 이름
        if (!binding.phoneSelect.text.equals(getString(R.string.j_phone_hint)) &&
            viewModel.userName.value.default().isNotEmpty() &&
            viewModel.userBirth.value.default().isNotEmpty() &&
            viewModel.userPhone.value.default().isNotEmpty()
        ) {

            this@NewJoinActivity.viewModel.onInputBirthUI(this@NewJoinActivity, viewModel.userBirth.value.default(), binding.birthError, binding.userBirthTv)
            this@NewJoinActivity.viewModel.onInputPhoneUI(this@NewJoinActivity, viewModel.userPhone.value.default(), binding.phoneNumError, binding.phoneNumTv)
            /*this@NewJoinActivity.viewModel.onInputNameUI(this@NewJoinActivity, viewModel.userName.value.default(), binding.nameError, binding.userNameTv)*/

            //검증진행 : 생년월일, 핸드폰번호
            if (viewModel.birthValid(viewModel.userBirth.value.default()) &&
                viewModel.phoneValid(viewModel.userPhone.value.default())
            /*&& viewModel.nameValid(viewModel.userName.value.default())*/
            ) {
                //검증 성공 !
                binding.verification = true
                binding.confirmBtn.onThrottleClick {
                    viewModel.getConsentList()
                }

            } else {
                //검증 실패 !
                binding.verification = false
                binding.confirmBtn.onThrottleClick {}
            }
        }
    }

    fun phoneSelectEvent() {
        JoinBDF.newPhoneInstance(
            selectValue = { agency, code ->
                binding.phoneSelect.text = agency
                viewModel.telComCd = code
            }
        ).show(supportFragmentManager, "newsAgencyBDF")
    }

    fun manSelectEvent() {
        binding.manBtn.isSelected = true
        binding.womanBtn.isSelected = false
        viewModel.userGender = this@NewJoinActivity.viewModel.genderSelect(binding.manBtn.text.toString())
    }

    fun womanSelectEvent() {
        binding.manBtn.isSelected = false
        binding.womanBtn.isSelected = true
        viewModel.userGender = this@NewJoinActivity.viewModel.genderSelect(binding.womanBtn.text.toString())
    }
}

package run.piece.dev.refactoring.ui.info

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import run.piece.dev.databinding.ActivityNewMyInfoBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.address.NewAddressBtDlg
import run.piece.dev.refactoring.ui.address.NewAddressDetailBtDlg
import run.piece.dev.refactoring.ui.email.dialog.EmailBDF
import run.piece.dev.refactoring.ui.join.NewJoinActivity
import run.piece.dev.refactoring.ui.logout.LogoutActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.member.model.request.UpdateConsentItemModel
import java.util.Locale

@AndroidEntryPoint
class NewMyInfoActivity : AppCompatActivity(R.layout.activity_new_my_info), NewAddressDetailBtDlg.BottomSheetListener {
    private lateinit var coroutineScope: CoroutineScope

    private lateinit var binding: ActivityNewMyInfoBinding
    private val viewModel: NewMyInfoViewModel by viewModels()
    private val dataStoreViewModel by viewModels<DataNexusViewModel>()

    private var addressBtDlg: NewAddressBtDlg? = null
    private lateinit var emailRegisterBDF: EmailBDF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMyInfoBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.activity = this@NewMyInfoActivity
        setContentView(binding.root)

        App()

        coroutineScope = lifecycleScope

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this@NewMyInfoActivity) { isConnected ->
            if (!isConnected) startActivity(Intent(this, NetworkActivity::class.java))
        }

        binding.apply {


            this@NewMyInfoActivity.let {
                intent?.let {
                    //초기 데이터 설정
                    val userName = intent.getStringExtra("userName") ?: ""
                    val birthDay = intent.getStringExtra("birthDay") ?: ""
                    val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
                    val baseAddress = intent.getStringExtra("baseAddress") ?: ""
                    val detailAddress = intent.getStringExtra("detailAddress") ?: ""
                    val email = intent.getStringExtra("email") ?: ""

                    nameContentTv.text = userName

                    if (birthDay.isNotEmpty()) {
                        birthContentTv.text = birthDay.toBaseDateFormat()
                    }

                    if (phoneNumber.isNotEmpty()) {
                        phoneContentTv.text = PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)
                    }

                    baseAddressTv.text = baseAddress // 서울특별시 강북구 삼양로 22길 4
                    detailAddressTv.text = detailAddress // 3층 302호

                    emailTv.text = email // piece@naver.com

                    if (baseAddress.isNotEmpty()) { //주소 등록 완료 회원의 경우
                        addressGroup.visibility = View.VISIBLE
                        noAddressTv.visibility = View.GONE
                        //주소 변경 밑줄
                        val content = SpannableString(getString(R.string.address_change))
                        content.setSpan(UnderlineSpan(), 0, content.length, 0)
                        addressContentTv.text = content
                    } else {
                        addressGroup.visibility = View.GONE
                        noAddressTv.visibility = View.VISIBLE
                    } //주소 미등록 회원의 경우

                    if (email.isNotEmpty()) { //이메일 등록 완료 회원의 경우
                        emailTv.visibility = View.VISIBLE
                        noEmailTv.visibility = View.GONE
                        //이메일 변경 밑줄
                        val content = SpannableString(getString(R.string.email_change))
                        content.setSpan(UnderlineSpan(), 0, content.length, 0)
                        emailContentTv.text = content
                    } else {
                        emailTv.visibility = View.GONE
                        noEmailTv.visibility = View.VISIBLE
                    } //이메일 미등록 회원의 경우
                }
            }
        }

        coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val networkConnection = NetworkConnection(applicationContext)
                networkConnection.observe(this@NewMyInfoActivity) { isConnected ->
                    if (!isConnected) startActivity(Intent(applicationContext, NetworkActivity::class.java))
                }

                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    viewModel.getConsentMemberTermsList()
                }

                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    launch(Dispatchers.Main) {
                        viewModel.memberInfo.collect {
                            when (it) {
                                is NewMyInfoViewModel.MemberInfoState.Success -> {
                                    binding.nameContentTv.text = it.memberVo.name

                                    if (it.memberVo.birthDay.isNotEmpty()) {
                                        binding.birthContentTv.text = it.memberVo.birthDay.toBaseDateFormat()
                                    }

                                    if (it.memberVo.cellPhoneNo.isNotEmpty()) {
                                        binding.phoneContentTv.text = PhoneNumberUtils.formatNumber(it.memberVo.cellPhoneNo, Locale.getDefault().country)
                                    }

                                    binding.baseAddressTv.text = it.memberVo.baseAddress // 서울특별시 강북구 삼양로 22길 4
                                    binding.detailAddressTv.text = it.memberVo.detailAddress // 3층 302호

                                    binding.emailTv.text = it.memberVo.email // piece@naver.com

                                    if (it.memberVo.baseAddress.isNotEmpty()) { //주소 등록 완료 회원의 경우
                                        binding.addressGroup.visibility = View.VISIBLE
                                        binding.noAddressTv.visibility = View.GONE
                                        //주소 변경 밑줄
                                        val content = SpannableString(getString(R.string.address_change))
                                        content.setSpan(UnderlineSpan(), 0, content.length, 0)
                                        binding.addressContentTv.text = content
                                    } else {
                                        binding.addressGroup.visibility = View.GONE
                                        binding.noAddressTv.visibility = View.VISIBLE
                                    } //주소 미등록 회원의 경우

                                    if (it.memberVo.email.isNotEmpty()) { //이메일 등록 완료 회원의 경우
                                        binding.emailTv.visibility = View.VISIBLE
                                        binding.noEmailTv.visibility = View.GONE
                                        //이메일 변경 밑줄
                                        val content = SpannableString(getString(R.string.email_change))
                                        content.setSpan(UnderlineSpan(), 0, content.length, 0)
                                        binding.emailContentTv.text = content
                                    } else {
                                        binding.emailTv.visibility = View.GONE
                                        binding.noEmailTv.visibility = View.VISIBLE
                                    } //이메일 미등록 회원의 경우
                                }

                                is NewMyInfoViewModel.MemberInfoState.Failure -> {
//                                    startActivity(ErrorActivity.getIntent(this@NewMyInfoActivity))
                                }

                                else -> {}
                            }
                        }
                    }

                    launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                        viewModel.consentMemberTerms.collect { data ->
                            when (data) {
                                is NewMyInfoViewModel.ConsentMemberTermsState.Success -> {
                                    if (data.termsMemberVo.required.consent.isNotEmpty()) {
                                        data.termsMemberVo.required.consent.forEach { vo ->
                                            viewModel.sendConsentList.add(UpdateConsentItemModel(memberId = viewModel.memberId, consentCode = vo.consentCode, isAgreement = vo.isAgreement))
                                        }
                                    }
                                    viewModel.sendConsentList.add(
                                        UpdateConsentItemModel(memberId = viewModel.memberId, consentCode = "CON1501", isAgreement = PrefsHelper.read("CON1501", "N"))
                                    )
                                }

                                is NewMyInfoViewModel.ConsentMemberTermsState.Failure -> {
//                                    startActivity(ErrorActivity.getIntent(this@NewMyInfoActivity))
                                }

                                else -> {}
                            }
                        }
                    }

                    launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                        viewModel.putMember.collect { data ->
                            when (data) {
                                is NewMyInfoViewModel.PutMemberState.Success -> {
                                    emailRegisterBDF.dismiss()

                                    PrefsHelper.write("email", data.memberVo.email)
                                    dataStoreViewModel.putEmail(data.memberVo.email)

                                    if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                                        viewModel.getMemberData()
                                    }

                                    AppConfirmDF.newInstance(
                                        "이메일 등록 완료",
                                        "이메일이 성공적으로 등록되었어요.",
                                        false,
                                        R.string.confirm,
                                        positiveAction = {},
                                        dismissAction = {}
                                    ).show(supportFragmentManager, "이메일 등록 완료")
                                }

                                is NewMyInfoViewModel.PutMemberState.Failure -> {
                                    /* AppConfirmDF.newInstance(
                                         "이메일 등록 실패",
                                         "이메일 등록에 실패했어요.",
                                         false,
                                         R.string.confirm,
                                         positiveAction = {},
                                         dismissAction = {}
                                     ).show(supportFragmentManager, "이메일 등록 실패")*/
                                }

                                else -> {}
                            }
                        }
                    }
                }

                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    binding.backLayout.onThrottleClick {
                        BackPressedUtil().activityFinish(this@NewMyInfoActivity, this@NewMyInfoActivity)
                    }
                }

                launch(Dispatchers.Main) {
                    binding.addressContentTv.onThrottleClick {
                        showAddressBottomSheet()
                    }
                }
            }

            if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                viewModel.getMemberData()
            }

            // 화면이 보여질 때의 설정
            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                //상태바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }
        }

        BackPressedUtil().activityCreate(this@NewMyInfoActivity, this@NewMyInfoActivity)
        BackPressedUtil().systemBackPressed(this@NewMyInfoActivity, this@NewMyInfoActivity)
    }


    //    fun showAddressBottomSheet() = MyInfoBottomSheetDialog(this).show(supportFragmentManager, "주소 변경")
    private fun showAddressBottomSheet() {
        addressBtDlg = NewAddressBtDlg(this@NewMyInfoActivity)
        addressBtDlg?.show(supportFragmentManager, "주소 변경")
    }

    fun showEmailBottomSheet() {
        emailRegisterBDF = EmailBDF.newRegisterInstance(
            verificationEvent = { isSuccess, email ->
                if (isSuccess) {
                    viewModel.putMember(email)
                } else {
                    //이메일 검증 실패
                }
            }
        )

        emailRegisterBDF.show(supportFragmentManager, "이메일 등록")
    }

    fun logout() {
        val appConfirmDF =
            AppConfirmDF.newInstance(
                getString(R.string.logout_txt),
                getString(R.string.my_info_logout_popup_content_txt),
                false,
                R.string.logout_txt,
                positiveAction = {
                    val intent = LogoutActivity.getIntent(this@NewMyInfoActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finishAffinity() // 이전 앱 화면 모두 제거 및 앱스택 제거
                },
                R.string.cancle,
                negativeAction = {},
                dismissAction = {}
            )
        appConfirmDF.show(supportFragmentManager, "로그아웃")
    }

    // 내정보 상세 - 재인증 로직
    fun reAuth() {
        startActivity(getNewJoinActivity(context = this@NewMyInfoActivity))
    }

    override fun onResume() {
        super.onResume()
        Log.v("NewMyInfoActivity onResume !","onResume")
        coroutineScope = lifecycleScope
        coroutineScope.launch {
            this@NewMyInfoActivity.viewModel.getMemberData()
        }

        coroutineScope.launch {
            this@NewMyInfoActivity.viewModel.memberInfo.collect { vo ->
                when(vo) {
                    is NewMyInfoViewModel.MemberInfoState.Success -> {
                        binding.baseAddressTv.text = vo.memberVo.baseAddress
                        binding.detailAddressTv.text = vo.memberVo.detailAddress
                    }
                    is NewMyInfoViewModel.MemberInfoState.Failure -> {}
                    else -> {}
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onButtonClick(first: String?, end: String?) {
        binding.baseAddressTv.text = first
        binding.detailAddressTv.text = end
    }

    companion object {
        fun getIntent(context: Context, userName: String, birthDay: String, phoneNumber: String, baseAddress: String, detailAddress: String, email: String): Intent {
            val intent = Intent(context, NewMyInfoActivity::class.java)
            intent.putExtra("userName", userName)
            intent.putExtra("birthDay", birthDay)
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("baseAddress", baseAddress)
            intent.putExtra("detailAddress", detailAddress)
            intent.putExtra("email", email)
            return intent
        }

        // 재인증 화면 이동
        fun getNewJoinActivity(context: Context): Intent {
            val intent = Intent(context, NewJoinActivity::class.java)
            intent.putExtra("Step", "6")
            return intent
        }
    }
}
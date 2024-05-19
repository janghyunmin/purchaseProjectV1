package run.piece.dev.refactoring.ui.certification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityNewCertificationBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.ui.deletemember.DeleteMemberActivity
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.member.model.MemberConsentVo
import java.util.concurrent.Executor

@AndroidEntryPoint
class NewCertificationActivity : AppCompatActivity() {
    private val TAG = NewCertificationActivity::class.java.name
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: ActivityNewCertificationBinding
    private val viewModel: CertificationViewModel by viewModels()

    // 생체 인증 결과를 처리하기 위한 Launcher
    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "registerForActivityResult - result : $result")
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "registerForActivityResult - RESULT_OK")
            authenticateCheck()  //생체 인증 가능 여부확인 다시 호출
        } else {
            Log.d(TAG, "registerForActivityResult - NOT RESULT_OK")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCertificationBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.activity = this
        binding.viewModel = viewModel
        coroutineScope = lifecycleScope

        setContentView(binding.root)

        App()

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this@NewCertificationActivity) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            // 저장된 FIDO 여부에 따라 스위치 초기 상태 설정
            switchView.isChecked = PrefsHelper.read("isFido", "N") != "N"

            // 스위치 클릭 시 상태 팝업 표시
            switchView.onThrottleClick {
                showStatusPopup()
            }
        }

        coroutineScope.launch {
            launch(Dispatchers.IO) {
                viewModel.getMemberData()
            }

            launch(Dispatchers.Main) {
                viewModel.memberInfo.collect {
                    when (it) {
                        is CertificationViewModel.MemberInfoState.Success -> {
                            name = it.memberVo.name
                            birthDay = it.memberVo.birthDay
                            cellPhoneNo = it.memberVo.cellPhoneNo
                            consents = it.memberVo.consents

                            consents.forEach { vo ->
//                                LogUtil.e("consent data : ${vo.consentCode}")
                            }

                        }

                        is CertificationViewModel.MemberInfoState.Failure -> {
//                            LogUtil.e("회원 정보 조회 실패: ${it.message}")
                        }

                        else -> {
//                            LogUtil.e("회원 정보 조회 로딩: $it")
                        }
                    }
                }
            }
        }

        // 화면이 보여질 때의 설정
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }
        binding.backImg.onThrottleClick {
            BackPressedUtil().activityFinish(this@NewCertificationActivity,this@NewCertificationActivity)
        }

        BackPressedUtil().activityCreate(this@NewCertificationActivity,this@NewCertificationActivity)
        BackPressedUtil().systemBackPressed(this@NewCertificationActivity,this@NewCertificationActivity)
    }

    // 비밀번호 변경 화면으로 이동
    fun passwordChande() {
        val intent = Intent(this, NewPassCodeActivity::class.java)
        val bundle = Bundle().apply {
            putString("Step","5")
            putString("name", name)
            putString("birthDay", birthDay)
            putString("cellPhoneNo", cellPhoneNo)
            putParcelableArrayList("consentList", consents as ArrayList<out Parcelable>)
        }

        intent.putExtras(bundle)
        startActivity(intent)
    }

    // 회원 탈퇴 화면으로 이동
    fun deleteMember() = startActivity(Intent(this, DeleteMemberActivity::class.java))

    // 생체 인증 가능 여부 확인 및 처리
    private fun authenticateCheck() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {} //생체 인증 가능
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {} //기기에서 생체 인증을 지원하지 않는 경우
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {} //현재 생체 인증을 사용할 수 없는 경우
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> { //생체 인식 정보가 등록되어 있지 않은 경우
                val appConfirmDF =
                    AppConfirmDF.newInstance(
                        getString(R.string.biometric_certification_go_page_title_txt),
                        getString(R.string.biometric_certification_go_page_content_txt),
                        false,
                        R.string.confirm,
                        positiveAction = {
                            goBiometricSettings()
                        },
                        R.string.cancle,
                        negativeAction = {
                            PrefsHelper.write("isFido", "N")
                            binding.switchView.isChecked = false
                        },
                        dismissAction = {}
                    )

                appConfirmDF.show(supportFragmentManager, "등록 설정화면으로 이동")
            }

            else -> {} //기타 실패
        }

        authenticate()
    }

    // 생체 인증 설정 화면으로 이동
    private fun goBiometricSettings() {
        val enrollIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            // 생체 인증 강도 설정
            putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }
        PrefsHelper.write("isFido", "Y")
        loginLauncher.launch(enrollIntent)
    }

    private fun authenticate() { //생체 인식 인증 실행
        val executor: Executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, authenticationCallback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("PIECE")
            .setSubtitle("생체인증")
            .setNegativeButtonText("취소")
            .build()

        promptInfo.let {
            PrefsHelper.write("isFido", "Y")
            biometricPrompt.authenticate(it)  //인증 실행
        }
    }

    private fun showStatusPopup() {
        if (binding.switchView.isChecked) {

            val appConfirmDF =
                AppConfirmDF.newInstance(
                    getString(R.string.biometric_certification_registration_title_txt),
                    getString(R.string.biometric_certification_registration_content_txt),
                    false,
                    R.string.registration_btn_txt,
                    positiveAction = {
                        binding.switchView.isChecked = true
                        PrefsHelper.write("isFido", "Y")
                        authenticateCheck()
                    },
                    R.string.back_txt,
                    negativeAction = {
                        binding.switchView.isChecked = false
                        PrefsHelper.write("isFido", "N")
                    },
                    dismissAction = {}
                )

            appConfirmDF.show(supportFragmentManager, "생체인증 등록이 필요해요")
        } else {

            val appConfirmDF =
                AppConfirmDF.newInstance(
                    getString(R.string.biometric_certification_cancel_title_txt),
                    getString(R.string.biometric_certification_cancel_content_txt),
                    false,
                    R.string.unlock_txt,
                    positiveAction = {
                        binding.switchView.isChecked = false
                        PrefsHelper.write("isFido", "N")
                    },
                    R.string.cancle,
                    negativeAction = {
                        binding.switchView.isChecked = true
                        PrefsHelper.write("isFido", "Y")
                    },
                    dismissAction = {}
                )

            appConfirmDF.show(supportFragmentManager, "생체인증 해제")
        }
    }

    // 생체 인증 결과에 대한 콜백 처리
    private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            PrefsHelper.write("isFido", "N")
            binding.switchView.isChecked = false
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            PrefsHelper.write("isFido", "Y")
            binding.switchView.isChecked = true
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            PrefsHelper.write("isFido", "N")
            binding.switchView.isChecked = false
        }
    }

    companion object {
        private var name: String = ""
        private var birthDay: String = ""
        private var cellPhoneNo: String = ""
        private var consents: List<MemberConsentVo> = arrayListOf()
        fun getIntent(context: Context): Intent = Intent(context, NewCertificationActivity::class.java)
    }
}
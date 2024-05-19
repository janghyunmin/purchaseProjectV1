package run.piece.dev.data.refactoring.module

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject

class BiometricAuthenticationManager @Inject constructor(
    @ActivityContext private val context: Context
) {

    private val executor: Executor = ContextCompat.getMainExecutor(context)
    private val biometricManager = BiometricManager.from(context)

    fun authenticate(activity: FragmentActivity, callback: BiometricAuthenticationCallback) {
        when (biometricManager.canAuthenticate()) {
            // 생체 인증 사용 가능
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val biometricPrompt = createBiometricPrompt(activity, callback)
                val promptInfo = createBiometricPromptInfo()
                biometricPrompt.authenticate(promptInfo)
            }
            // 기기에서 생체 인증을 지원하지 않는 경우
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                callback.onAuthenticationFailed(errorCode = BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,"기기에 생체 인증을 지원 하지 않습니다.")
            }
            // 현재 생체 인증을 사용할 수 없는 경우
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                callback.onAuthenticationFailed(errorCode = BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,"현재 생체 인증을 사용할 수 없습니다.")
            }
            // 생체인증 정보가 등록되어 있지 않은 경우
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                callback.onAuthenticationFailed(errorCode = BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED, "기기에 생체 인증 정보가 등록되어 있지 않습니다.")
            }
        }
    }

    private fun createBiometricPrompt(activity: FragmentActivity, callback: BiometricAuthenticationCallback): BiometricPrompt {
        return BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Authentication successful
                    callback.onAuthenticationSuccessful()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Authentication failed
                    callback.onAuthenticationFailed(errorCode = errorCode, "Biometric authentication failed: $errString")

                }
            })
    }

    private fun createBiometricPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증")
            .setSubtitle("PIECE 로그인")
            .setConfirmationRequired(false)
            .setNegativeButtonText("취소")
            .build()
    }

    interface BiometricAuthenticationCallback {
        fun onAuthenticationSuccessful()
        fun onAuthenticationFailed(errorCode: Int, errorMessage: String)
    }
}
package run.piece.dev.refactoring.ui.deletemember

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityDeletememberSuccessBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick

// 탈퇴 성공시 보여지는 Activity
@AndroidEntryPoint
class DeleteMemberSuccessActivity : AppCompatActivity(R.layout.activity_deletemember_success){
    private lateinit var binding: ActivityDeletememberSuccessBinding

    private val dataStoreViewModel by viewModels<DataNexusViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeletememberSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.apply {
            PrefsHelper.removeAll()

            dataStoreViewModel.putIsLoginKey("")
            dataStoreViewModel.putPassWord("")
            dataStoreViewModel.putMemberId("")
            dataStoreViewModel.putAccessToken("")
            dataStoreViewModel.putRefreshToken("")
            dataStoreViewModel.putName("")
            dataStoreViewModel.putEmail("")
            dataStoreViewModel.putIsFido("")
            dataStoreViewModel.putExpiredAt("")
            dataStoreViewModel.putPurchaseAgreeTime("")
            dataStoreViewModel.putVulnerable("")
            dataStoreViewModel.putFinalVulnerable("")
            dataStoreViewModel.setFbToken("")
            dataStoreViewModel.putCON1501("")
            dataStoreViewModel.putInvestScore(0)
            dataStoreViewModel.putInvestFinalScore(0)
            dataStoreViewModel.putInvestResult("")
            dataStoreViewModel.putPassWordModal("N")

            /*PrefsHelper.removeKey("isLogin")
            PrefsHelper.removeKey("inputPinNumber")
            PrefsHelper.removeKey("memberId")
            PrefsHelper.removeKey("accessToken")
            PrefsHelper.removeKey("refreshToken")
            PrefsHelper.removeKey("name")
            PrefsHelper.removeKey("pinNumber")
            PrefsHelper.removeKey("cellPhoneNo")
            PrefsHelper.removeKey("cellPhoneIdNo")
            PrefsHelper.removeKey("birthDay")
            PrefsHelper.removeKey("zipCode")
            PrefsHelper.removeKey("baseAddress")
            PrefsHelper.removeKey("detailAddress")
            PrefsHelper.removeKey("idNo")
            PrefsHelper.removeKey("ci")
            PrefsHelper.removeKey("di")
            PrefsHelper.removeKey("gender")
            PrefsHelper.removeKey("email")
            PrefsHelper.removeKey("isFido")
            PrefsHelper.removeKey("createdAt")
            PrefsHelper.removeKey("joinDay")
            PrefsHelper.removeKey("vran")
            PrefsHelper.removeKey("requiredConsentDate")
            PrefsHelper.removeKey("notRequiredConsentDate")
            PrefsHelper.removeKey("assetNotification")
            PrefsHelper.removeKey("portfolioNotification")
            PrefsHelper.removeKey("marketingSms")
            PrefsHelper.removeKey("marketingApp")
            PrefsHelper.removeKey("isAd")
            PrefsHelper.removeKey("isNotice")
            PrefsHelper.write("passCodeModal", "N")*/

            confirmBtn.onThrottleClick {
                val intent = Intent(this@DeleteMemberSuccessActivity, IntroActivity::class.java)
                startActivity(intent)

                BackPressedUtil().activityFinish(this@DeleteMemberSuccessActivity,this@DeleteMemberSuccessActivity)
            }
        }

        window?.apply {
            // 캡쳐방지 Kotlin Ver
            addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        BackPressedUtil().activityCreate(this@DeleteMemberSuccessActivity,this@DeleteMemberSuccessActivity)
        BackPressedUtil().systemBackPressed(this@DeleteMemberSuccessActivity,this@DeleteMemberSuccessActivity)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, DeleteMemberSuccessActivity::class.java)
        }
    }
}
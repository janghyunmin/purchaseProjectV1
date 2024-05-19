package run.piece.dev.view.deed

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.viewModels
import com.rajat.pdfviewer.PdfRendererView
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.api.NetworkInfo
import run.piece.dev.data.api.RetrofitService
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.document.MemberPortfolioDocument
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.BaseDTO
import run.piece.dev.data.viewmodel.GetUserViewModel
import run.piece.dev.databinding.ActivityDeedBinding
import run.piece.dev.refactoring.ui.info.NewMyInfoActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.widget.utils.CustomDialogListener
import run.piece.dev.widget.utils.DialogManager

/**
 * 소유증서 전용 PDF Activity
 * */
@AndroidEntryPoint
class DeedActivity : BaseActivity<ActivityDeedBinding>(R.layout.activity_deed) {
    private val activityDeedBinding by lazy { ActivityDeedBinding.inflate(layoutInflater) }
    var mContext: Context = this@DeedActivity

    private val mv by viewModels<GetUserViewModel>()
    val response: RetrofitService? = NetworkInfo.getRetrofit().create(RetrofitService::class.java)
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    var purchaseId: String = ""


    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        binding.apply {
            lifecycleOwner = this@DeedActivity

            activityDeedBinding.lifecycleOwner = this@DeedActivity
            activityDeedBinding.activity = this@DeedActivity

            memberVm = mv
            mv.getUserData()

            pdfView.statusListener = object : PdfRendererView.StatusCallBack {
                override fun onPdfLoadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {
                    super.onPdfLoadProgress(progress, downloadedBytes, totalBytes)
                    loading.visibility = View.VISIBLE
                }
                override fun onPdfLoadStart() {
                    Log.i("statusCallBack","onPdfLoadStart")
                }

                override fun onPdfLoadSuccess(absolutePath: String) {
                    super.onPdfLoadSuccess(absolutePath)
                    loading.visibility = View.GONE
                    pdfView.post {
                        pdfView.recyclerView.scrollToPosition(0)
                    }
                }

                override fun onError(error: Throwable) {
                    super.onError(error)
                    Log.e("onError : ", "${error.message}")
                }
            }

            setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
            setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
            setNaviBarIconColor(true) // 네비게이션 true : 검정색
            setNaviBarBgColor("#f2f3f4") // 네비게이션 배경색

            // 캡쳐방지 Kotlin Ver - jhm 2023/03/21
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            binding.backImg.setOnClickListener {
                BackPressedUtil().activityFinish(this@DeedActivity,this@DeedActivity)
            }

            purchaseId = intent.getStringExtra("purchaseId").toString()
        }

        // 주소 등록 Listener - jhm 2022/10/31
        val failAddressListener: CustomDialogListener = object : CustomDialogListener {
            override fun onCancelButtonClicked() {
                // 닫기 버튼 누른 후 로직 - jhm 2022/07/04
            }

            override fun onOkButtonClicked() {
                // 주소 등록하러 가기 OnClick..
                val intent = Intent(mContext, NewMyInfoActivity::class.java)
                startActivity(intent)
            }
        }

        // 등록된 이메일이 없어요 Listener - jhm 2022/10/31
        val failDialogEmailListener: CustomDialogListener = object : CustomDialogListener {
            override fun onCancelButtonClicked() {
                // 닫기 버튼 누른 후 로직 - jhm 2022/07/04

            }

            override fun onOkButtonClicked() {
                // 이메일 등록하러 가기 OnClick..
                val intent = Intent(mContext, NewMyInfoActivity::class.java)
                startActivity(intent)
            }
        }

        // 이메일로 소유증서 신청 Listener - jhm 2022/10/31
        val postDialogEmailListener: CustomDialogListener = object : CustomDialogListener {
            override fun onCancelButtonClicked() {
                // 닫기 버튼 누른 후 로직 - jhm 2022/07/04

            }

            override fun onOkButtonClicked() {
                // 이메일로 소유증서 신청 API Call..
                // 소유증서 발송시 필요 모델 - jhm 2022/10/25
                val memberPortfolioDocument =
                    MemberPortfolioDocument(
                        memberId,
                        purchaseId,
                        "EMAIL"
                    )
                // 이메일로 소유증서 발송 API Call.. - jhm 2022/10/31
                try {
                    response?.postDocument(
                        accessToken = "Bearer $accessToken",
                        deviceId = deviceId,
                        memberId = memberId,
                        memberPortfolioDocument
                    )?.enqueue(object : Callback<BaseDTO> {
                        override fun onResponse(
                            call: Call<BaseDTO>,
                            response: Response<BaseDTO>
                        ) {
                            try {
                                if (response.isSuccessful) {

                                    DialogManager.openNotGoDalog(
                                        mContext,
                                        "이메일 신청 완료",
                                        "소유권 증명서를 이메일로 보내드릴게요.\n최대 1~5분의 시간이 소요될 수 있어요."
                                    )
                                } else {
                                    DialogManager.openNotGoDalog(
                                        mContext,
                                        "이메일 신청 실패",
                                        getString(R.string.email_send_fail_content_txt)
                                    )
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                DialogManager.openNotGoDalog(
                                    mContext,
                                    "이메일 신청 실패",
                                    getString(R.string.email_send_fail_content_txt)
                                )
                            }
                        }

                        override fun onFailure(call: Call<BaseDTO>, t: Throwable) {
                            t.printStackTrace()
                            DialogManager.openNotGoDalog(
                                mContext,
                                "이메일 신청 실패",
                                getString(R.string.email_send_fail_content_txt)
                            )
                        }
                    })
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    DialogManager.openNotGoDalog(
                        mContext,
                        "이메일 신청 실패",
                        getString(R.string.email_send_fail_content_txt)
                    )
                }
            }
        }

        // 이메일로 소유증서 신청 - jhm 2022/10/31
//        binding.emailBtn.setOnClickListener {
//            val data = PrefsHelper.read("email", "")
//            if (data == "" || data == "null" == data.isNullOrEmpty()) {
//                DialogManager.openTwoBtnDialog(
//                    mContext,
//                    "등록된 이메일이 없어요",
//                    "이메일을 등록하고 소유권 증명서를 메일로 받아보세요.",
//                    failDialogEmailListener,
//                    "이메일 등록"
//                )
//            } else {
//                DialogManager.openTwoBtnDialog(
//                    mContext,
//                    "이메일로 보내기",
//                    "등록된 이메일로 소유권 증명서를 직접 받아보실 수 있어요",
//                    postDialogEmailListener,
//                    "이메일"
//                )
//            }
//        }

        BackPressedUtil().activityCreate(this@DeedActivity,this@DeedActivity)
        BackPressedUtil().systemBackPressed(this@DeedActivity,this@DeedActivity)


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
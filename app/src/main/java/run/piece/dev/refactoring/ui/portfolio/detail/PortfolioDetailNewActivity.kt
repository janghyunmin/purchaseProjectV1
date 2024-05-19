package run.piece.dev.refactoring.ui.portfolio.detail

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.BuildConfig
import run.piece.dev.data.api.WebSocketListener
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityPortfolioDetailNewBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.PortfolioDetailAnimUtils
import run.piece.dev.refactoring.PortfolioDetailAnimUtils.animImageScale
import run.piece.dev.refactoring.PortfolioDetailAnimUtils.animImageTranslationY
import run.piece.dev.refactoring.PortfolioDetailAnimUtils.animScrollAlpha
import run.piece.dev.refactoring.PortfolioDetailAnimUtils.animStatusButtonVisibility
import run.piece.dev.refactoring.PortfolioDetailAnimUtils.changeHeader
import run.piece.dev.refactoring.PortfolioDetailAnimUtils.overScrollBlock
import run.piece.dev.refactoring.base.BasePdfActivity
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.deposit.NhAccountNotiBtDlg
import run.piece.dev.refactoring.ui.portfolio.detail.agree.ElectronicDocumentAgreeBtDlg
import run.piece.dev.refactoring.ui.portfolio.detail.assets.AssetsFragment
import run.piece.dev.refactoring.ui.portfolio.detail.business.BusinessFragment
import run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata.DisclosureDataFragment
import run.piece.dev.refactoring.ui.portfolio.detail.marketinfo.MarketInfoFragment
import run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide.PortfolioGuideFragment
import run.piece.dev.refactoring.ui.portfolio.detail.securities.SecuritiesFragment
import run.piece.dev.refactoring.ui.portfolio.detail.story.StoryFragment
import run.piece.dev.refactoring.ui.purchase.PurchaseBDF
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toDecimalComma
import run.piece.dev.view.bank.BankSelectActivity
import run.piece.dev.view.common.LoginChkActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.extension.SnackBarCommon
import run.piece.dev.widget.utils.AlarmDlgListener
import run.piece.dev.widget.utils.DialogManager
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PortfolioDetailNewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPortfolioDetailNewBinding
    private val viewModel: PortfolioDetailNewViewModel by viewModels()
    private val dataStoreViewModel by viewModels<DataNexusViewModel>()

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var recruitmentState: RecruitmentState

    private var subscriptionState: Boolean = false
    private var portfolioAlarmState: Boolean = false
    private var scrollState = false

    private var client: OkHttpClient? = null
    private val socketListener: WebSocketListener = WebSocketListener()

    private var countDownTimer: CountDownTimer? = null

    private var purchaseCancelBDF: PurchaseBDF? = null
    private var purchaseCancelConfirmDF: AppConfirmDF? = null

    private var alarmCancelConfirmDF: AppConfirmDF? = null
    private var purchaseCancelFailConfirmDF: AppConfirmDF? = null
    private var vran: String = ""
    private var userAge: Int = 0


    // 전자문서 교부 동의 BtDlg
    private var electronicDocAgreeBtDlg: ElectronicDocumentAgreeBtDlg? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                binding = ActivityPortfolioDetailNewBinding.inflate(layoutInflater)
                binding.lifecycleOwner = this@PortfolioDetailNewActivity
                binding.viewModel = viewModel
                binding.activity = this@PortfolioDetailNewActivity
                binding.loading.visibility = View.VISIBLE
                binding.loading.bringToFront()

                setContentView(binding.root)

                electronicDocAgreeBtDlg = ElectronicDocumentAgreeBtDlg(this@PortfolioDetailNewActivity)

                binding.pieceInfoLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewModel.pieceInfoLayoutHeight = binding.pieceInfoLayout.measuredHeight
                        initParamSetting()
                        binding.pieceInfoLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })

                viewModel.buttonHeight = binding.buttonSubCardView.layoutParams.height
                viewModel.cardHeight = binding.cardView.layoutParams.height
                viewModel.hideBarHeight = binding.appBarIv.layoutParams.height

                val sheetHeight = binding.cardView.layoutParams.height + binding.buttonSubCardView.layoutParams.height
                viewModel.portfolioIvScale = (viewModel.portfolioIvScale + ((sheetHeight / 2) * 0.001)).toFloat()

                setCautionContentTv()

                binding.scrollView.run {
                    setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                        animImageScale(
                            scrollY = scrollY,
                            portfolioIvScale = viewModel.portfolioIvScale,
                            size = binding.cardView.layoutParams.height + binding.buttonSubCardView.layoutParams.height
                        ).apply {
                            binding.portfolioIv.scaleX = this
                            binding.portfolioIv.scaleY = this
                        }

                        animStatusButtonVisibility(scrollY = scrollY, cardHeight = viewModel.cardHeight, isButtonShow = viewModel.isButtonShow) {
                            viewModel.isButtonShow = it
                            if (it) showAnimButton() else hideAnimButton()
                        }

                        animImageTranslationY(scrollY = scrollY, reachPosition = viewModel.getDeviceHeight() - viewModel.hideBarHeight) { state: Boolean, value: Float ->
                            if (state) binding.portfolioIv.translationY = -value
                            else {
                                viewModel.portfolioIvAlpha = value
                                binding.portfolioIv.alpha = viewModel.portfolioIvAlpha
                            }
                        }

                        animScrollAlpha(scrollY = scrollY, reachPosition = viewModel.getDeviceHeight() - viewModel.hideBarHeight, deviceWidth = viewModel.getDeviceWidth()) {
                            viewModel.portfolioIvAlpha = it
                            binding.portfolioIv.alpha = viewModel.portfolioIvAlpha
                        }

                        overScrollBlock(scrollY = scrollY, scrollState = scrollState, viewHeight = binding.cardView.layoutParams.height + binding.buttonSubCardView.layoutParams.height) {
                            if (it) {
                                binding.scrollView.scrollTo(viewModel.getDeviceWidth(), binding.cardView.layoutParams.height + binding.buttonSubCardView.layoutParams.height)
                            }
                        }

                        changeHeader(scrollY = scrollY, reachPosition = viewModel.getDeviceHeight() - viewModel.hideBarHeight, hideBarHeight = viewModel.hideBarHeight,
                            headerAlpha = {
                                viewModel.headerAlpha = it
                            }, viewChangeEvent = {
                                if (it) {
                                    binding.titleTv.visibility = View.VISIBLE
                                    binding.appBarIv.visibility = View.VISIBLE
                                    binding.backIv.setImageDrawable(ContextCompat.getDrawable(this@PortfolioDetailNewActivity, R.drawable.ic_x24_arrow_left_black))
                                    binding.shareIv.setImageDrawable(ContextCompat.getDrawable(this@PortfolioDetailNewActivity, R.drawable.ic_x24_share_black))

                                    //상태바 아이콘(true: 검정 / false: 흰색)
                                    window.apply { WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true }

                                    viewModel.headerAlpha = 1F

                                    binding.appBarIv.setBackgroundColor(ContextCompat.getColor(this@PortfolioDetailNewActivity, R.color.c_f2f3f4))
                                    window.statusBarColor = ContextCompat.getColor(this@PortfolioDetailNewActivity, R.color.c_f2f3f4)

                                    binding.cardView.setBackgroundColor(ContextCompat.getColor(this@PortfolioDetailNewActivity, R.color.c_f2f3f4))
                                } else {
                                    binding.titleTv.visibility = View.GONE
                                    binding.appBarIv.visibility = View.GONE
                                    binding.backIv.setImageDrawable(ContextCompat.getDrawable(this@PortfolioDetailNewActivity, R.drawable.ic_x24_arrow_left_white))
                                    binding.shareIv.setImageDrawable(ContextCompat.getDrawable(this@PortfolioDetailNewActivity, R.drawable.ic_x24_share_white))

                                    window.apply { WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false }

                                    binding.appBarIv.setBackgroundColor(ContextCompat.getColor(this@PortfolioDetailNewActivity, R.color.trans))
                                    window.statusBarColor = ContextCompat.getColor(this@PortfolioDetailNewActivity, R.color.trans)

                                    binding.cardView.background = ContextCompat.getDrawable(this@PortfolioDetailNewActivity, R.drawable.back_top_round_radius_24_f2f3f4)
                                }

                                binding.appBarIv.alpha = viewModel.headerAlpha
                                binding.titleTv.alpha = viewModel.headerAlpha
                            }
                        )
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.getPortfolioDetail()
            }.join()

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                window.apply {
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    addFlags(WindowManager.LayoutParams.FLAG_SECURE) //캡처 방지

                    //상태바 아이콘(true: 검정 / false: 흰색)
                    WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = false
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                binding.loading.visibility = View.VISIBLE
                binding.root.alpha = 0f
                val startAnimView = ObjectAnimator.ofFloat(binding.root, "alpha", 0f, 1f)
                startAnimView.duration = 1600
                startAnimView.start()
            }

            if (viewModel.memberId.isNotEmpty()) {
                launch(Dispatchers.IO) {
                    viewModel.getMemberDataNvo()
                }.join()

                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    this@PortfolioDetailNewActivity.viewModel.memberInfo.collect {
                        when (it) {
                            is PortfolioDetailNewViewModel.MemberInfoState.Success -> {

                                val year = it.memberVo.birthDay.substring(0, 4).toInt()
                                val month = it.memberVo.birthDay.substring(5, 7).toInt()
                                val day = it.memberVo.birthDay.substring(8, 10).toInt()

                                // Create a LocalDate object for the birth date
                                // 생년월일 설정
                                val birthDate = LocalDate.of(year, month, day)
                                // 현재 날짜 설정
                                val currentDate = LocalDate.now()

                                // 만 나이 계산
                                val koreanAge = viewModel.calculateKoreanAge(birthDate, currentDate)


                                // 생일이 지났는지 여부 판별
                                val isBirthdayPassed = currentDate.monthValue > birthDate.monthValue ||
                                        (currentDate.monthValue == birthDate.monthValue &&
                                                currentDate.dayOfMonth >= birthDate.dayOfMonth)
                                if (isBirthdayPassed) {
                                    userAge = koreanAge + 1
                                } else {
                                    userAge = koreanAge
                                }
                            }

                            is PortfolioDetailNewViewModel.MemberInfoState.Failure -> {}
                            else -> {}
                        }

                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                viewModel.purchaseCancelState.collect {
                    when (it) {
                        is PortfolioDetailNewViewModel.PurchaseCancelState.Success -> {
                            this@PortfolioDetailNewActivity.viewModel.getPortfolioDetail(true)

                            AppConfirmDF.newInstance(
                                getString(R.string.purchase_cancel_success_title_txt),
                                getString(R.string.purchase_cancel_success_content_txt),
                                false,
                                R.string.purchase_cancel_btn_success_txt,
                                positiveAction = {},
                                dismissAction = {}
                            ).show(supportFragmentManager, "신청취소 완료")
                        }

                        is PortfolioDetailNewViewModel.PurchaseCancelState.Failure -> {
                            purchaseCancelFailConfirmDF = AppConfirmDF.newInstance(
                                "청약신청 취소 실패",
                                "마감되어 취소가 불가능해요.",
                                false,
                                R.string.confirm,
                                positiveAction = {},
                                dismissAction = {},
                            )
                            purchaseCancelFailConfirmDF?.show(supportFragmentManager, "PurchaseCancelFail")
                        }

                        else -> {

                        }
                    }
                }
            }

            // 오픈 알림 신청
            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.sendAlarm.collect {
                    when (it) {
                        is PortfolioDetailNewViewModel.AlarmPortfolioSendState.Success -> {
                            if (viewModel.getDeviceWidth() < 1600) SnackBarCommon(binding.root, getString(R.string.open_alarm_success_text), "포트폴리오 알림").show(56, 16)
                            else SnackBarCommon(binding.root, getString(R.string.open_alarm_success_text), "포트폴리오 알림").show(56)
                            portfolioAlarmState = true
                        }

                        is PortfolioDetailNewViewModel.AlarmPortfolioSendState.Failure -> {}

                        else -> {}
                    }
                }
            }

            // 오픈 알림 신청 취소
            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.deleteAlarm.collect {
                    when (it) {
                        is PortfolioDetailNewViewModel.AlarmPortfolioDeleteState.Success -> {
                            if (viewModel.getDeviceWidth() < 1600) SnackBarCommon(binding.root, getString(R.string.open_alarm_cancel_text), "포트폴리오 알림").show(56, 16)
                            else SnackBarCommon(binding.root, getString(R.string.open_alarm_cancel_text), "포트폴리오 알림").show(56)
                            portfolioAlarmState = false
                        }

                        is PortfolioDetailNewViewModel.AlarmPortfolioDeleteState.Failure -> {
                            startActivity(Intent(this@PortfolioDetailNewActivity, NetworkActivity::class.java))
                        }

                        else -> {}
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.portfolioDetail.collect { it ->
                    binding.loading.visibility = View.GONE
                    when (it) {
                        is PortfolioDetailNewViewModel.PortfolioDetailState.Success -> {
                            val data = it.portfolioDetailVo
                            setRecruitmentState(data.detailDefault.recruitmentState)

                            Glide
                                .with(this@PortfolioDetailNewActivity)
                                .load(data.detailDefault.representThumbnailImagePath)
                                .centerCrop()
                                .override(viewModel.getDeviceWidth(), viewModel.getDeviceHeight())
                                .error(R.drawable.image_placeholder)
                                .placeholder(R.drawable.image_placeholder)
                                .into(binding.portfolioIv)

                            binding.portfolioIv.scaleX = viewModel.portfolioIvScale
                            binding.portfolioIv.scaleY = viewModel.portfolioIvScale

                            initView(data)

                            delay(500)

                            //cardView를 상단으로 Y만큼 이동
                            PortfolioDetailAnimUtils.initAnimScrollY(ObjectAnimator.ofInt(binding.scrollView, "scrollY", viewModel.getInitScrollHeight())) {
                                scrollState = true
                            }
                            binding.shareLayout.onThrottleClick({ shareEvent(data.detailDefault.shareUrl) }, 2000)
                        }

                        is PortfolioDetailNewViewModel.PortfolioDetailState.Failure -> {
                            binding.loading.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.alarmState.collect { it ->
                    when (it) {
                        is PortfolioDetailNewViewModel.AlarmPortfolioGetState.Success -> {
                            portfolioAlarmState = it.portfolioGetAlarmVo.notificationYn == "Y"

                            if (!it.isInit) { //클릭시
                                //Y 신청 완료된 상태, N 신청 가능한 상태
                                if (portfolioAlarmState) {
                                    alarmCancelConfirmDF = AppConfirmDF.newInstance(
                                        getString(R.string.alarm_popup_request_cancel_text),
                                        getString(R.string.alarm_popup_request_cancel_content_text),
                                        false,
                                        positiveStrRes = R.string.cancel_application_txt,
                                        positiveAction = {
                                            this@PortfolioDetailNewActivity.viewModel.deletePortfolioAlarm() // 청약 알림 취소 API
                                        },
                                        negativeStrRes = R.string.dismiss,
                                        negativeAction = {},
                                        dismissAction = {}
                                    )
                                    alarmCancelConfirmDF?.show(supportFragmentManager, "AlarmCancel")

                                } else this@PortfolioDetailNewActivity.viewModel.sendPortfolioAlarm() // 청약 알림 신청
                            }
                        }

                        is PortfolioDetailNewViewModel.AlarmPortfolioGetState.Failure -> {}
                        else -> {}
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.portfolioRefreshDetail.collect {
                    when (it) {
                        is PortfolioDetailNewViewModel.PortfolioRefreshDetailState.Success -> {
                            refreshView(it.portfolioDetailVo)
                        }

                        is PortfolioDetailNewViewModel.PortfolioRefreshDetailState.Failure -> {
                        }

                        else -> {}
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                this@PortfolioDetailNewActivity.viewModel.memberInfo.collect {
                    when (it) {
                        is PortfolioDetailNewViewModel.MemberInfoState.Success -> {
                            if (it.memberVo.vran.isEmpty()) { //계좌번호 없는 회원
                                vran = ""
                            } else { //계좌번호 있는 회원
                                vran = it.memberVo.vran
                            }
                        }

                        is PortfolioDetailNewViewModel.MemberInfoState.Failure -> {}
                        else -> {}
                    }
                }
            }

            launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                binding.backLayout.onThrottleClick {
                    BackPressedUtil().activityFinish(this@PortfolioDetailNewActivity, this@PortfolioDetailNewActivity)
                }
            }
        }

        BackPressedUtil().activityCreate(this@PortfolioDetailNewActivity, this@PortfolioDetailNewActivity)
        BackPressedUtil().systemBackPressed(this@PortfolioDetailNewActivity, this@PortfolioDetailNewActivity)

    }

    private fun initView(initVo: PortfolioDetailVo) {
        if (initVo.offerId == "null" || initVo.offerId.isEmpty()) PrefsHelper.write("isRecruitmentSubscriptionState", false) //청약 x 청약 신청 버튼 활성화 == false
        else PrefsHelper.write("isRecruitmentSubscriptionState", true) //회원이 청약 완료 청약 취소 버튼 활성화 == true

        viewModel.offerId = initVo.offerId

        supportFragmentManager.commitNow {
            setReorderingAllowed(true)

            add(R.id.securities, SecuritiesFragment.newInstance(initVo.portfolioStock, initVo.detailDefault.recruitmentState))

            if (initVo.detailDefault.recruitmentState.isNotEmpty() && initVo.detailDefault.portfolioId.isNotEmpty() && initVo.productCompositionList.isNotEmpty()) {
                add(
                    R.id.asset,
                    AssetsFragment.newInstance(
                        initVo.detailDefault.recruitmentState,
                        initVo.detailDefault.portfolioId,
                        initVo.productCompositionList
                    )
                )
            }

            if (initVo.portfolioMarketInfos.isNotEmpty()) {
                if (initVo.detailDefault.recruitmentState != "PRS0108" && initVo.detailDefault.recruitmentState != "PRS0111") {
                    add(R.id.market_info, MarketInfoFragment.newInstance(initVo.portfolioMarketInfos))
                }
            }

            if (initVo.joinBizList.isNotEmpty()) {
                add(R.id.business, BusinessFragment.newInstance(initVo.joinBizList))
            }

            if (initVo.attachFile.isNotEmpty()) {
                val itemList = ArrayList<AttachFileItemVo>()

                /**
                 * PAF02 증권 첨부파일
                 * PAF0201	PAF02	증권신고서
                 * PAF0202	PAF02	투자설명서
                 * PAF0203	PAF02	청약안내문
                 * PAF0204	PAF02	투자계약서
                 * PAF0205	PAF02	소유권 증명서
                 * */
                initVo.attachFile.filterNot {
                    when (it.attachFileCode) {
                        "PAF0201" -> itemList.add(it)
                        "PAF0202" -> itemList.add(it)
                        "PAF0203" -> itemList.add(it)
                    }
                    true
                }

                if (itemList.isNotEmpty()) {
                    add(R.id.disclosure, DisclosureDataFragment.newInstance(itemList))
                }
            }

            add(R.id.story, StoryFragment.newInstance(initVo.story, initVo.purchaseGuides))
            add(R.id.guide, PortfolioGuideFragment.newInstance(initVo.detailDefault.portfolioId))
        }

        val networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this@PortfolioDetailNewActivity) { isConnected ->
            if (!isConnected) {
                startActivity(Intent(applicationContext, NetworkActivity::class.java))
                if (recruitmentState == RecruitmentState.RECRUITMENT_SCHEDULED || recruitmentState == RecruitmentState.RECRUITING) {
                    client?.dispatcher?.cancelAll()
                    client?.dispatcher?.executorService?.shutdown()
                }
            } else {
                if (recruitmentState == RecruitmentState.RECRUITMENT_SCHEDULED || recruitmentState == RecruitmentState.RECRUITING) startWebSocket()
            }
        }

        binding.portfolioId.text = initVo.detailDefault.subTitle
        binding.portfolioTitleTv.text = initVo.detailDefault.title
        binding.titleTv.text = initVo.detailDefault.subTitle

        viewModel.recruitmentStateLiveData.observe(this@PortfolioDetailNewActivity) {
            val data: Any = when (it) {
                "PRS0101" -> RecruitmentState.RECRUITMENT_SCHEDULED
                "PRS0102" -> RecruitmentState.RECRUITING
                "PRS0103" -> RecruitmentState.DEADLINE
                "PRS0104" -> RecruitmentState.DIVIDEND_SCHEDULED
                "PRS0105" -> RecruitmentState.DIVIDEND_SCHEDULED
                "PRS0106" -> RecruitmentState.DIVIDEND_SCHEDULED
                "PRS0107" -> RecruitmentState.DIVIDEND_SCHEDULED
                "PRS0108" -> RecruitmentState.DIVIDEND_ENDOF
                "PRS0111" -> RecruitmentState.DIVIDEND_COMPLETED
                else -> {}
            }

            if (recruitmentState != data as RecruitmentState) { //리프래시
                recruitmentState = data
                CoroutineScope(Dispatchers.IO).launch {
                    this@PortfolioDetailNewActivity.viewModel.getPortfolioDetail(true)
                }

                //청약 상태가 변경되면 유저 액션이 들어가는 팝업을 닫습니다.
                when (recruitmentState) {
                    RecruitmentState.RECRUITING -> { //모집예정 -> 모집중
                        alarmCancelConfirmDF?.dismiss()
                    }

                    RecruitmentState.DEADLINE -> { //모집중 -> 모집마감
                        purchaseCancelBDF?.dismiss()
                        purchaseCancelConfirmDF?.dismiss()
                    }

                    else -> {}
                }
            }
        }

        if (recruitmentState == RecruitmentState.RECRUITMENT_SCHEDULED || recruitmentState == RecruitmentState.RECRUITING) { //알람 신청, 청약중
            lifecycleScope.launch {
                launch(coroutineScope.coroutineContext + Dispatchers.IO) {
                    startWebSocket()
                }

                // 웹소켓
                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    socketListener.liveData.observe(this@PortfolioDetailNewActivity) {
                        /*{
                            "data":[
                            {
                                "portfolioId":"db87677d-fd25-43e8-b907-1632a36b69a1",
                                "achievementRate":"0",
                                "offerMemberCount":"0",
                                "recruitmentState":"PRS0102"
                            }
                                   ],
                            "message":"포트폴리오 구매 정보 조회 성공",
                            "status":"OK","statusCode":0
                        }*/

                        try {
                            it?.let {
                                val jsonObject = JsonObject()
                                jsonObject.add("data", it)

                                val response = jsonObject.getAsJsonObject("data")

                                response.getAsJsonArray("data")?.run {
                                    get(0)?.run {
                                        val portfolioId = asJsonObject["portfolioId"].asString
                                        viewModel.achievementRateLiveData.value = asJsonObject["achievementRate"].asString
                                        viewModel.offerMemberCountLiveData.value = asJsonObject["offerMemberCount"].asString
                                        viewModel.recruitmentStateLiveData.value = asJsonObject["recruitmentState"].asString
                                        viewModel.notificationCountLiveData.value = asJsonObject["notificationCount"].asString
                                    }
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }
        }

        viewModel.notificationCountLiveData.observe(this@PortfolioDetailNewActivity) {
            //"n명의 회원이 알림을 신청했어요!" else "알림 신청하고 빠르게 소식을 받아보세요!"
            if (recruitmentState == RecruitmentState.RECRUITMENT_SCHEDULED) {
                //Log.e("알람상태테스트", "${if (portfolioAlarmState) "신청 불가 신청 내역 있음" else "신청 가능 신청 내역 없음"}")
                binding.marketingTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.marketingMessage(!portfolioAlarmState, it.toDecimalComma())
                binding.buttonSubStateTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.buttonTitle(!portfolioAlarmState)
                settingGif(RecruitmentState.RECRUITMENT_SCHEDULED.gifResource(!portfolioAlarmState, it))
                alarmButtonSetting(portfolioAlarmState)
            }
        }

        viewModel.achievementRateLiveData.observe(this@PortfolioDetailNewActivity) {
            if (recruitmentState == RecruitmentState.RECRUITING) binding.statusDescriptionTv.text = "${it}% 모집되었어요"
        }

        viewModel.offerMemberCountLiveData.observe(this@PortfolioDetailNewActivity) {
            subscriptionState = PrefsHelper.read("isRecruitmentSubscriptionState", false)

            if (recruitmentState == RecruitmentState.RECRUITING) {
                binding.marketingTv.text = RecruitmentState.RECRUITING.marketingMessage(subscriptionState, it.toDecimalComma())
                settingGif(RecruitmentState.RECRUITING.gifResource(subscriptionState, it))
            }
        }

        when (recruitmentState) {
            RecruitmentState.RECRUITMENT_SCHEDULED -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                val parsed = input.parse(initVo.detailDefault.recruitmentBeginDate)
                binding.statusSubDescriptionTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.endDate(output.format(parsed))

                val timeDifference = timeDifference(RecruitmentState.RECRUITMENT_SCHEDULED, initVo.detailDefault.recruitmentBeginDate)
                binding.statusDescriptionTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.description(timeDifference, "")
                binding.timeTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.buttonTime(timeDifference)

                //테스트
//                val timeDifference = timeDifference("2023-10-18 21:14:30.0")
//                Log.e("TimeDifference", "${timeDifference.day}일 ${timeDifference.hour}시간 ${timeDifference.minute}분 ${timeDifference.second}초")

                alarmButtonSetting(false)

                binding.statusTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.title
                binding.marketingTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.marketingMessage(true, initVo.detailDefault.offerMemberCount)
                binding.buttonSubStateTv.text = RecruitmentState.RECRUITMENT_SCHEDULED.buttonTitle(true)
                settingGif(RecruitmentState.RECRUITMENT_SCHEDULED.gifResource(true, ""))

                if (viewModel.memberId.isNotEmpty()) { //회원
                    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) { //알림 설정 O
                        this@PortfolioDetailNewActivity.viewModel.getPortfolioAlarm(true) // 청약 알람 리스트 조회
                    } else {
                    } //알림 설정 X
                } else {
                } // 비회원

                binding.buttonSubCardView.onThrottleClick {
                    if (viewModel.memberId.isNotEmpty()) { //회원
                        Log.v("여기 오픈알림 버튼 클릭 : ", "OnClick")
                        // 만나이가 19세가 넘으면 정상 프로세스 ( 알림 신청 )
                        if (userAge > 19) {
                            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) { //알림 설정 O
                                this@PortfolioDetailNewActivity.viewModel.getPortfolioAlarm(false) // 청약 알람 리스트 조회
                            } else goAlarmSetting() //알림 설정 X
                        } else {
                            // 만나이가 19세 미만이면 신청 불가
                            val appConfirmDF = AppConfirmDF.newInstance(
                                getString(R.string.portfolio_purchase_verify_txt),
                                getString(R.string.portfolio_purchase_verify_content),
                                false,
                                R.string.confirm_text,
                                positiveAction = {},
                                dismissAction = {}
                            )
                            appConfirmDF.show(supportFragmentManager, "PurchaseNotAvailable")
                        }

                    } else goLoginActivity() //비회원
                }

                timeDifference.milliseconds?.let {
                    countDownTimer = initTimer(it)
                }
                countDownTimer?.start()

                binding.timeCardView.visibility = View.VISIBLE
            }

            RecruitmentState.RECRUITING -> { //버튼 타이틀 필수
                //recruitmentEndDate
                subscriptionState = PrefsHelper.read("isRecruitmentSubscriptionState", false)

                binding.statusTv.text = RecruitmentState.RECRUITING.title

                val input = SimpleDateFormat("yyyy-M-d H", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일 H시", Locale.getDefault())
                val parsed = input.parse(initVo.detailDefault.recruitmentEndDate)

                binding.statusSubDescriptionTv.text = RecruitmentState.RECRUITING.endDate(output.format(parsed))
                binding.buttonSubStateTv.text = RecruitmentState.RECRUITING.buttonTitle(subscriptionState)

                subscriptionButtonSetting(subscriptionState)

                binding.timeCardView.visibility = View.GONE

                settingGif(RecruitmentState.RECRUITING.gifResource(subscriptionState, ""))

                subscriptionClickEvent(initVo, initVo.offerId)
            }

            RecruitmentState.DEADLINE -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())

                if (initVo.detailDefault.prizeAt.isEmpty()) {
                    binding.statusSubDescriptionTv.visibility = View.GONE
                } else {
                    val parsed = input.parse(initVo.detailDefault.prizeAt)
                    binding.statusSubDescriptionTv.text = RecruitmentState.DEADLINE.endDate(output.format(parsed))
                }

                binding.statusTv.text = RecruitmentState.DEADLINE.title
                binding.statusDescriptionTv.text = RecruitmentState.DEADLINE.description(null, "")

                binding.buttonSubStateTv.text = RecruitmentState.DEADLINE.buttonTitle(false)
                binding.marketingTv.text = RecruitmentState.DEADLINE.marketingMessage(false, "")

                etcButtonSetting()
                binding.buttonSubCardView.onThrottleClick {}

                settingGif(RecruitmentState.DEADLINE.gifResource(false, ""))

                binding.timeCardView.visibility = View.GONE
            }

            RecruitmentState.DIVIDEND_SCHEDULED -> {
                //dividendsExpecatationDate
                refreshDividendScheduled(initVo)
            }

            // PRS0108 - 분배 예정 ( 증권 만기 )
            RecruitmentState.DIVIDEND_ENDOF -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                val parsed = input.parse(initVo.detailDefault.dividendsExpecatationDate)

                binding.statusTv.text = RecruitmentState.DIVIDEND_ENDOF.title
                binding.statusDescriptionTv.text = RecruitmentState.DIVIDEND_ENDOF.description(null, "")
                binding.statusSubDescriptionTv.text = RecruitmentState.DIVIDEND_ENDOF.endDate(output.format(parsed))
                binding.buttonSubStateTv.text = RecruitmentState.DIVIDEND_ENDOF.buttonTitle(false)
                binding.marketingTv.text = RecruitmentState.DIVIDEND_ENDOF.marketingMessage(false, "")

                etcButtonSetting()
                binding.buttonSubCardView.onThrottleClick {}

                settingGif(RecruitmentState.DIVIDEND_ENDOF.gifResource(false, ""))

                binding.timeCardView.visibility = View.GONE
            }

            RecruitmentState.DIVIDEND_COMPLETED -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                val parsed = input.parse(initVo.detailDefault.dividendsExpecatationDate)

                binding.statusTv.text = RecruitmentState.DIVIDEND_COMPLETED.title
                binding.statusDescriptionTv.text = RecruitmentState.DIVIDEND_COMPLETED.description(null, initVo.detailDefault.achieveProfitRate)
                binding.statusSubDescriptionTv.text = RecruitmentState.DIVIDEND_COMPLETED.endDate(output.format(parsed))
                binding.buttonSubStateTv.text = RecruitmentState.DIVIDEND_COMPLETED.buttonTitle(false)
                binding.marketingTv.text = RecruitmentState.DIVIDEND_COMPLETED.marketingMessage(false, "")

                etcButtonSetting()
                binding.buttonSubCardView.onThrottleClick {}

                settingGif(RecruitmentState.DIVIDEND_COMPLETED.gifResource(false, ""))
                binding.timeCardView.visibility = View.GONE
            }
        }
    }

    private fun refreshView(refreshVo: PortfolioDetailVo) {
        if (refreshVo.offerId == "null" || refreshVo.offerId.isBlank()) PrefsHelper.write("isRecruitmentSubscriptionState", false) //청약 x 청약 신청 버튼 활성화 == false
        else PrefsHelper.write("isRecruitmentSubscriptionState", true) //회원이 청약 완료 청약 취소 버튼 활성화 == true
        viewModel.offerId = refreshVo.offerId

        setRecruitmentState(refreshVo.detailDefault.recruitmentState)

        when (recruitmentState) {
            RecruitmentState.RECRUITMENT_SCHEDULED -> {}
            RecruitmentState.RECRUITING -> { //버튼 타이틀 필수
                subscriptionState = PrefsHelper.read("isRecruitmentSubscriptionState", false)

                binding.statusTv.text = RecruitmentState.RECRUITING.title

                val input = SimpleDateFormat("yyyy-M-d H", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일 H시", Locale.getDefault())
                val parsed = input.parse(refreshVo.detailDefault.recruitmentEndDate)

                binding.statusSubDescriptionTv.text = RecruitmentState.RECRUITING.endDate(output.format(parsed))
                binding.buttonSubStateTv.text = RecruitmentState.RECRUITING.buttonTitle(subscriptionState)

                subscriptionButtonSetting(subscriptionState)

                binding.timeCardView.visibility = View.GONE

                subscriptionClickEvent(refreshVo, viewModel.offerId)

                countDownTimer?.cancel()
            }

            RecruitmentState.DEADLINE -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())

                if (refreshVo.detailDefault.prizeAt.isEmpty()) {
                    binding.statusSubDescriptionTv.visibility = View.GONE
                } else {
                    val parsed = input.parse(refreshVo.detailDefault.prizeAt)
                    binding.statusSubDescriptionTv.text = RecruitmentState.DEADLINE.endDate(output.format(parsed))
                }

                binding.statusTv.text = RecruitmentState.DEADLINE.title
                binding.statusDescriptionTv.text = RecruitmentState.DEADLINE.description(null, "")

                binding.buttonSubStateTv.text = RecruitmentState.DEADLINE.buttonTitle(false)
                binding.marketingTv.text = RecruitmentState.DEADLINE.marketingMessage(false, "")

                etcButtonSetting()
                binding.buttonSubCardView.onThrottleClick {}

                settingGif(RecruitmentState.DEADLINE.gifResource(false, ""))

                binding.timeCardView.visibility = View.GONE
            }

            RecruitmentState.DIVIDEND_SCHEDULED -> {
                //dividendsExpecatationDate
                refreshDividendScheduled(refreshVo)
            }

            // PRS0108 - 분배 예정 ( 증권 만기 )
            RecruitmentState.DIVIDEND_ENDOF -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                val parsed = input.parse(refreshVo.detailDefault.dividendsExpecatationDate)

                binding.statusTv.text = RecruitmentState.DIVIDEND_ENDOF.title
                binding.statusDescriptionTv.text = RecruitmentState.DIVIDEND_ENDOF.description(null, "")
                binding.statusSubDescriptionTv.text = RecruitmentState.DIVIDEND_ENDOF.endDate(output.format(parsed))
                binding.buttonSubStateTv.text = RecruitmentState.DIVIDEND_ENDOF.buttonTitle(false)
                binding.marketingTv.text = RecruitmentState.DIVIDEND_ENDOF.marketingMessage(false, "")

                etcButtonSetting()
                binding.buttonSubCardView.onThrottleClick {}

                settingGif(RecruitmentState.DIVIDEND_ENDOF.gifResource(false, ""))

                binding.timeCardView.visibility = View.GONE

            }

            RecruitmentState.DIVIDEND_COMPLETED -> {
                val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
                val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
                val parsed = input.parse(refreshVo.detailDefault.dividendsExpecatationDate)

                binding.statusTv.text = RecruitmentState.DIVIDEND_COMPLETED.title
                binding.statusDescriptionTv.text = RecruitmentState.DIVIDEND_COMPLETED.description(null, refreshVo.detailDefault.achieveProfitRate)
                binding.statusSubDescriptionTv.text = RecruitmentState.DIVIDEND_COMPLETED.endDate(output.format(parsed))
                binding.buttonSubStateTv.text = RecruitmentState.DIVIDEND_COMPLETED.buttonTitle(false)
                binding.marketingTv.text = RecruitmentState.DIVIDEND_COMPLETED.marketingMessage(false, "")

                etcButtonSetting()
                binding.buttonSubCardView.onThrottleClick {}

                settingGif(RecruitmentState.DIVIDEND_COMPLETED.gifResource(false, ""))

                binding.timeCardView.visibility = View.GONE
            }
        }
    }

    private fun refreshDividendScheduled(detailVo: PortfolioDetailVo) {
        val input = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val output = SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault())
        val parsed = input.parse(detailVo.detailDefault.dividendsExpecatationDate)

        val timeDifference = timeDifference(RecruitmentState.DIVIDEND_SCHEDULED, detailVo.detailDefault.dividendsExpecatationDate)

        // PRS0104 ( 분배 예정에서 만기일이 0 일 일경우 )
        if (timeDifference.day == 0) {
            binding.statusTv.text = RecruitmentState.DIVIDEND_ENDOF.title
            binding.statusDescriptionTv.text = RecruitmentState.DIVIDEND_ENDOF.description(null, "")
            binding.statusSubDescriptionTv.text = RecruitmentState.DIVIDEND_ENDOF.endDate(output.format(parsed))
            binding.buttonSubStateTv.text = RecruitmentState.DIVIDEND_ENDOF.buttonTitle(false)
            binding.marketingTv.text = RecruitmentState.DIVIDEND_ENDOF.marketingMessage(false, "")

            settingGif(RecruitmentState.DIVIDEND_ENDOF.gifResource(false, ""))

        } else {
            binding.statusTv.text = RecruitmentState.DIVIDEND_SCHEDULED.title
            binding.statusDescriptionTv.text = RecruitmentState.DIVIDEND_SCHEDULED.description(timeDifference, "")
            binding.statusSubDescriptionTv.text = RecruitmentState.DIVIDEND_SCHEDULED.endDate(output.format(parsed))
            binding.buttonSubStateTv.text = RecruitmentState.DIVIDEND_SCHEDULED.buttonTitle(false)
            binding.marketingTv.text = RecruitmentState.DIVIDEND_SCHEDULED.marketingMessage(false, "")

            settingGif(RecruitmentState.DIVIDEND_SCHEDULED.gifResource(false, ""))
        }

        etcButtonSetting()
        binding.buttonSubCardView.onThrottleClick {}
        binding.timeCardView.visibility = View.GONE
    }

    @SuppressLint("SimpleDateFormat")
    private fun subscriptionClickEvent(vo: PortfolioDetailVo, offerId: String) {
        viewModel.offerId = offerId

        // 나이 validation
        binding.buttonSubCardView.onThrottleClick {

            if (viewModel.memberId.isNullOrEmpty()) {
                goLoginActivity()
            } else {
                viewModel.getMemberData(vo)
                viewModel.getPortfolioDetail(true)

                // 만약 나이가 만 19세 미만이면
                if (userAge <= 19) {
                    // 만나이가 19세 미만이면 신청 불가
                    val appConfirmDF = AppConfirmDF.newInstance(
                        getString(R.string.portfolio_purchase_verify_txt),
                        getString(R.string.portfolio_purchase_verify_content),
                        false,
                        R.string.confirm_text,
                        positiveAction = {},
                        dismissAction = {}
                    )
                    appConfirmDF.show(supportFragmentManager, "PurchaseNotAvailable")
                } else {
                    // 청약 신청 정보가 없으면
                    if (viewModel.offerId.isBlank() || viewModel.offerId == "null") {
                        if (vran.isEmpty()) {
                            NhAccountNotiBtDlg(this@PortfolioDetailNewActivity).apply {
                                setCallback(object : NhAccountNotiBtDlg.OnSendFromBottomSheetDialog {
                                    override fun sendValue() {
                                        val intent = Intent(this@PortfolioDetailNewActivity, BankSelectActivity::class.java)
                                        intent.putExtra("vranNo", "")
                                        startActivity(intent)
                                        dismiss()
                                    }
                                })
                            }.show(supportFragmentManager, getString(R.string.nh_bt_sheet_btn_txt))
                        } else {

                            if(!electronicDocAgreeBtDlg?.isAdded!!) {
                                electronicDocAgreeBtDlg?.show(supportFragmentManager, "전자문서 교부 동의")
                                electronicDocAgreeBtDlg?.setCallback(object : ElectronicDocumentAgreeBtDlg.OnSendFromBottomSheetDialog {
                                    override fun sendValue() {
                                        // 전자 문서 교부 동의 dismiss

                                        val calendar = Calendar.getInstance()
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                        val currentDateAndTime: String = dateFormat.format(calendar.time)
                                        dataStoreViewModel.putPurchaseAgreeTime(currentDateAndTime)

                                        /**
                                         * PAF02 증권 첨부파일
                                         * PAF0201	PAF02	증권신고서
                                         * PAF0202	PAF02	투자설명서
                                         * PAF0203	PAF02	청약안내문
                                         * PAF0204	PAF02	투자계약서
                                         * PAF0205	PAF02	소유권 증명서
                                         * */

                                        //투자설명서
                                        vo.let { vo ->
                                            startActivity(
                                                BasePdfActivity.getPurchaseManualPdfIntent(
                                                    context = this@PortfolioDetailNewActivity,
                                                    title = getString(R.string.purchase_manual_txt),
                                                    detailDefaultVo = vo.detailDefault,
                                                    stockVo = vo.portfolioStock,
                                                    attachFileItemVo = vo.attachFile,
                                                    viewType = "PortfolioPurchaseContract"
                                                )
                                            )
                                        }
                                    }
                                })
                            }
                        }
                    }
                    // 기존 청약 정보가 있으면
                    else {
                        viewModel.getPurchaseInfo()

                        // 만약 웹소켓에서 PRS0102 -> PRS0103 으로 변경되고 청약신청 취소 API를 호출할 경우 실패 Modal을 출력한다.
                        var recruitmentState = viewModel.recruitmentStateLiveData.value
                        if (recruitmentState == "PRS0103") {
                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.purchaseInfoState.collect {
                                    when (it) {
                                        is PortfolioDetailNewViewModel.PurchaseInfoState.Success -> {
                                            viewModel.portfolioDetailDefaultVo?.let { data ->
                                                purchaseCancelBDF = PurchaseBDF.newPurchaseCancel(
                                                    purchaseInfoVo = it.purchaseInfoVo,
                                                    portfolioDetailDefaultVo = data,
                                                    investmentProspectusUrl = viewModel.investmentProspectusUrl
                                                ) {

                                                    // 실패 모달 바로 출력
                                                    purchaseCancelFailConfirmDF = AppConfirmDF.newInstance(
                                                        "청약신청 취소 실패",
                                                        "마감되어 취소가 불가능해요.",
                                                        false,
                                                        R.string.confirm,
                                                        positiveAction = {},
                                                        dismissAction = {},
                                                    )
                                                    purchaseCancelFailConfirmDF?.show(supportFragmentManager, "PurchaseCancelFail")
                                                }
                                                purchaseCancelBDF?.show(supportFragmentManager, "청약신청 취소")
                                            }
                                        }

                                        is PortfolioDetailNewViewModel.PurchaseInfoState.Failure -> {
                                            startActivity(Intent(this@PortfolioDetailNewActivity, NetworkActivity::class.java))
                                        }

                                        else -> {}
                                    }
                                }
                            }
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                viewModel.purchaseInfoState.collect {
                                    when (it) {
                                        is PortfolioDetailNewViewModel.PurchaseInfoState.Success -> {
                                            viewModel.portfolioDetailDefaultVo?.let { data ->
                                                purchaseCancelBDF = PurchaseBDF.newPurchaseCancel(
                                                    purchaseInfoVo = it.purchaseInfoVo,
                                                    portfolioDetailDefaultVo = data,
                                                    investmentProspectusUrl = viewModel.investmentProspectusUrl
                                                ) {

                                                    purchaseCancelConfirmDF = AppConfirmDF.newInstance(
                                                        "청약신청 취소",
                                                        "취소한 금액은 가상계좌로 환불해 드려요.",
                                                        false,
                                                        R.string.purchase_cancel_btn_txt,
                                                        positiveAction = {
                                                            viewModel.purchaseCancel() //청약 취소 API
                                                        },
                                                        R.string.purchase_cancel_btn_close_txt,
                                                        negativeAction = {},
                                                        dismissAction = {},
                                                        backgroundDrawable = R.drawable.btn_round_ff7878
                                                    )
                                                    purchaseCancelConfirmDF?.show(supportFragmentManager, "취소 팝업")
                                                }
                                                purchaseCancelBDF?.show(supportFragmentManager, "청약신청 취소")
                                            }
                                        }

                                        is PortfolioDetailNewViewModel.PurchaseInfoState.Failure -> {
                                            startActivity(Intent(this@PortfolioDetailNewActivity, NetworkActivity::class.java))
                                        }

                                        else -> {}
                                    }
                                }
                            }

                            CoroutineScope(Dispatchers.Main).launch {

                            }
                        }
                    }
                }
            }

        }
    }

    private fun settingGif(path: String) {
        when (path) {
            "portfolio_bell" -> {
                Glide.with(this@PortfolioDetailNewActivity).load(R.raw.portfolio_bell).into(binding.animIv)
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.VISIBLE
            }

            "portfolio_clap" -> {
                Glide.with(this@PortfolioDetailNewActivity).load(R.raw.portfolio_clap).into(binding.animIv)
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.VISIBLE
            }

            "portfolio_run" -> {
                Glide.with(this@PortfolioDetailNewActivity).load(R.raw.portfolio_run).into(binding.animIv)
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.VISIBLE
            }

            "portfolio_welcome" -> {
                Glide.with(this@PortfolioDetailNewActivity).load(R.raw.portfolio_welcome).into(binding.animIv)
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.VISIBLE
            }

            "portfolio_bank_book" -> {
                Glide.with(this@PortfolioDetailNewActivity).load(R.raw.portfolio_bank_book).into(binding.animIv)
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.VISIBLE
            }

            "portfolio_battery" -> {
                Glide.with(this@PortfolioDetailNewActivity).load(R.raw.portfolio_battery).into(binding.animIv)
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.VISIBLE
            }

            "image_person_tri" -> {
                binding.lottieIv.visibility = View.VISIBLE
                binding.animIv.visibility = View.GONE
            }

            else -> {
                binding.lottieIv.visibility = View.GONE
                binding.animIv.visibility = View.GONE
            }
        }
    }

    private fun initParamSetting() {

        /* pieceInfo Layout을 ScrollView 최하단에 배치하는 작업 */
        val pieceInfoParams: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        pieceInfoParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        pieceInfoParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        pieceInfoParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID

        pieceInfoParams.topMargin = viewModel.getInitParamHeight()

        binding.pieceInfoLayout.layoutParams = pieceInfoParams

        /* status bar만큼 top margin을 넣는 작업
        appBarIv == 숨긴 header Background ImageView
        topLayout == 숨긴 header Layout */
        val appBarIvParam = binding.appBarIv.layoutParams as ConstraintLayout.LayoutParams
        appBarIvParam.setMargins(0, viewModel.getStatusBarHeight(), 0, 0)
        binding.appBarIv.layoutParams = appBarIvParam

        val appBarLayoutParam = binding.topLayout.layoutParams as ConstraintLayout.LayoutParams
        appBarLayoutParam.setMargins(0, viewModel.getStatusBarHeight(), 0, 0)
        binding.topLayout.layoutParams = appBarLayoutParam
    }

    private fun showAnimButton() {
        ObjectAnimator.ofFloat(binding.buttonSubCardView, "translationY", 0F, -this@PortfolioDetailNewActivity.viewModel.dpToPixel(48).toFloat()).apply {
            duration = 500L
        }.start()
        ObjectAnimator.ofFloat(binding.buttonSubCardBackIv, "translationY", 0F, -this@PortfolioDetailNewActivity.viewModel.dpToPixel(38).toFloat()).apply {
            duration = 500L
        }.start()
    }

    private fun hideAnimButton() {
        ObjectAnimator.ofFloat(binding.buttonSubCardView, "translationY", 0F, this@PortfolioDetailNewActivity.viewModel.dpToPixel(48).toFloat()).apply {
            duration = 500L
        }.start()
        ObjectAnimator.ofFloat(binding.buttonSubCardBackIv, "translationY", 0F, this@PortfolioDetailNewActivity.viewModel.dpToPixel(38).toFloat()).apply {
            duration = 500L
        }.start()
    }

    private fun initTimer(milliseconds: Long): CountDownTimer {
        return object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                val day = TimeUnit.SECONDS.toDays(seconds)
                val hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.SECONDS.toDays(seconds) * 24
                val minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
                val second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60

                val timeDay = "$day"
                val timeSecond = if (second in 1..9) "0$second" else "$second"
                val timeMinute = if (minute in 1..9) "0$minute" else "$minute"
                val timeHours = if (hours in 1..9) "0$hours" else "$hours"

                if (day > 0) { //1일 이상
                    binding.statusDescriptionTv.text = "청약까지 ${timeDay}일 ${timeHours}시간 남았어요"
                    binding.timeTv.text = "D-$timeDay"
                } else if (hours > 0) { // 1시간 이상
                    binding.statusDescriptionTv.text = "청약까지 ${hours}시간 ${minute}분 남았어요"
                    binding.timeTv.text = "$timeHours:$timeMinute"
                } else if (minute > 0) { // 1분 이상
                    binding.statusDescriptionTv.text = "청약까지 ${minute}분 ${second}초 남았어요"
                    binding.timeTv.text = "$timeMinute:$timeSecond"
                } else { // 1분 미만
                    binding.statusDescriptionTv.text = "청약까지 ${second}초 남았어요"
                    binding.timeTv.text = "$timeMinute:$timeSecond"
                }

                Log.e("customTimer ==>", "${day}일 ${hours}시간 ${minute}분 ${second}초")
            }

            override fun onFinish() { //리프레시
                if (recruitmentState == RecruitmentState.RECRUITMENT_SCHEDULED) {
                    this@PortfolioDetailNewActivity.viewModel.getPortfolioDetail(true)
                    Log.e("customTimer ==>", "타이머 종료!")
                }
            }
        }
    }

    private fun timeDifference(type: RecruitmentState, date: String): TimeDifference {
        val dateFormat = when (type) {
            RecruitmentState.RECRUITMENT_SCHEDULED -> {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            }

            RecruitmentState.DIVIDEND_SCHEDULED -> {
                SimpleDateFormat("yyyy-M-d")
            }

            else -> {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            }
        }

        val now = dateFormat.format(Date(System.currentTimeMillis()))
        val nowFormat = dateFormat.parse(now)
        val afterFormat = dateFormat.parse(date)

        var diffMilliseconds = afterFormat.time - nowFormat.time

        var seconds: Long = TimeUnit.MILLISECONDS.toSeconds(diffMilliseconds)
        var day = TimeUnit.SECONDS.toDays(seconds).toInt()
        var hours = TimeUnit.SECONDS.toHours(seconds) - day * 24
        var minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60
        var second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60

        if (day <= 0) day = 0
        if (hours <= 0) hours = 0
        if (minute <= 0) minute = 0
        if (second <= 0) second = 0
        if (seconds <= 0) diffMilliseconds = 0L

        return TimeDifference(day, hours, minute, second, diffMilliseconds)
    }

    private fun setRecruitmentState(value: String) {
        when (value) {
            "PRS0101" -> recruitmentState = RecruitmentState.RECRUITMENT_SCHEDULED
            "PRS0102" -> recruitmentState = RecruitmentState.RECRUITING
            "PRS0103" -> recruitmentState = RecruitmentState.DEADLINE
            "PRS0104" -> recruitmentState = RecruitmentState.DIVIDEND_SCHEDULED
            "PRS0105" -> recruitmentState = RecruitmentState.DIVIDEND_SCHEDULED
            "PRS0106" -> recruitmentState = RecruitmentState.DIVIDEND_SCHEDULED
            "PRS0107" -> recruitmentState = RecruitmentState.DIVIDEND_SCHEDULED
            "PRS0108" -> recruitmentState = RecruitmentState.DIVIDEND_ENDOF
            "PRS0111" -> recruitmentState = RecruitmentState.DIVIDEND_COMPLETED
        }
    }

    private fun subscriptionButtonSetting(state: Boolean) {
        if (!state) {
            binding.buttonSubCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.c_10cfc9))
            binding.buttonSubCardView.strokeColor = ContextCompat.getColor(this, R.color.c_10cfc9)
            binding.buttonSubStateTv.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.buttonSubCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.buttonSubCardView.strokeColor = ContextCompat.getColor(this, R.color.g300_EAECF0)
            binding.buttonSubStateTv.setTextColor(ContextCompat.getColor(this, R.color.g800_4A4D55))
        }
        binding.buttonSubCardView.strokeWidth = 1
    }

    private fun alarmButtonSetting(state: Boolean) {
        if (!state) {
            binding.buttonSubCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.c_10cfc9))
            binding.buttonSubCardView.strokeColor = ContextCompat.getColor(this, R.color.c_10cfc9)
            binding.buttonSubStateTv.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.timeCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.buttonSubCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.buttonSubCardView.strokeColor = ContextCompat.getColor(this, R.color.g300_EAECF0)
            binding.buttonSubStateTv.setTextColor(ContextCompat.getColor(this, R.color.g800_4A4D55))
            binding.timeCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.p100_CFF5F4))
        }
        binding.buttonSubCardView.strokeWidth = 1
    }

    private fun etcButtonSetting() {
        binding.buttonSubCardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.g400_DADCE3))
        binding.buttonSubCardView.strokeWidth = 0
        binding.buttonSubStateTv.setTextColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun setCautionContentTv() {
        val text = getString(R.string.portfolio_detail_warning_text)
        val sentences = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val font = ResourcesCompat.getFont(this@PortfolioDetailNewActivity, R.font.pretendard_regular)
        val params = LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.WRAP_CONTENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT).apply {
            setMargins(0, 0, 0, viewModel.dpToPixel(16))
        }

        sentences.forEachIndexed { index, s ->
            val spannableStringBuilder = if (s.contains("철회가 불가능")) {
                val boldTextIndex = s.indexOf("철회가 불가능")
                val spannable = SpannableStringBuilder(s).apply {
                    setSpan(StyleSpan(Typeface.BOLD), boldTextIndex, boldTextIndex + 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(LeadingMarginSpan.Standard(0, 28), 0, length, 0)
                }
                spannable
            } else {
                val spannable = SpannableStringBuilder(s).apply {
                    setSpan(LeadingMarginSpan.Standard(0, 28), 0, length, 0)
                }
                spannable
            }

            val textView = AppCompatTextView(this).apply {
                this.text = spannableStringBuilder
                this.setTextColor(ContextCompat.getColor(this@PortfolioDetailNewActivity, R.color.g800_4A4D55))
                setTypeface(font, Typeface.NORMAL)
                lineHeight = viewModel.dpToPixel(22)
                textSize = 14F
                includeFontPadding = false
                layoutParams = params
            }
            binding.bottomLayout.addView(textView)
        }
    }

    private fun goLoginActivity() {
        startActivity(Intent(this@PortfolioDetailNewActivity, LoginChkActivity::class.java))
    }

    private fun goAlarmSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DialogManager.openAlarmDlg(this, this, object : AlarmDlgListener {
                @RequiresApi(Build.VERSION_CODES.R)
                override fun openOptionsClicked() {
                    val intent = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            Intent().apply {
                                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                putExtra(
                                    Settings.EXTRA_APP_PACKAGE,
                                    this@PortfolioDetailNewActivity.packageName
                                )
                            }

                        }

                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                            Intent().apply {
                                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                                putExtra("app_package", this@PortfolioDetailNewActivity.packageName)
                                putExtra(
                                    "app_uid",
                                    this@PortfolioDetailNewActivity.applicationInfo?.uid
                                )
                            }
                        }

                        else -> {
                            Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                addCategory(Intent.CATEGORY_DEFAULT)
                                data =
                                    Uri.parse("package:" + this@PortfolioDetailNewActivity.packageName)
                            }
                        }
                    }
                    this@PortfolioDetailNewActivity.startActivity(intent)
                }

                override fun offCloseClicked() {}
            })
        }
    }

    private fun shareEvent(shareUrl: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, shareUrl)

        val shareIntent = Intent.createChooser(intent, "PIECE")
        startActivity(shareIntent)
    }

    // 청약 상세 WebSocket
    private fun startWebSocket() {
        // CoroutineScope을 사용하여 IO 스레드에서 작업을 수행하는 코루틴을 시작합니다.
        CoroutineScope(Dispatchers.IO).launch {

            // OkHttpClient 객체를 초기화합니다.
            client = OkHttpClient()

            // 포트폴리오 ID 및 회원 ID에 따라 요청할 URL을 생성합니다.
            val url = if (viewModel.memberId.isEmpty()) BuildConfig.PIECE_WS_PORTFOLIO_DETAIL + "${intent.getStringExtra("portfolioId")}" + "/" + "N"
            else BuildConfig.PIECE_WS_PORTFOLIO_DETAIL + "${intent.getStringExtra("portfolioId")}" + "/" + viewModel.memberId

            // WebSocket에 사용할 Request 객체를 생성합니다.
            val request: Request = Request.Builder()
                .url(url)
                .build()

            // OkHttpClient를 사용하여 WebSocket을 열고 소켓 리스너를 등록합니다.
            client?.newWebSocket(request, socketListener)
        }
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
        viewModel.getPortfolioDetail(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        client?.dispatcher?.cancelAll()
        client?.dispatcher?.executorService?.shutdown()

        coroutineScope.cancel()
        countDownTimer?.cancel()
    }

    companion object {
        fun getIntent(context: Context, portfolioId: String): Intent {
            val intent = Intent(context, PortfolioDetailNewActivity::class.java)
            intent.putExtra("portfolioId", portfolioId)
            return intent
        }
    }
}
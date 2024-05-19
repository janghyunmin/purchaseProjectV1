package run.piece.dev.view.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseFragment
import run.piece.dev.data.BuildConfig
import run.piece.dev.data.api.WebSocketListener
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.portfolio.PortfolioItem
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.FragmentHomeBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.alarm.AlarmActivity
import run.piece.dev.refactoring.ui.alarm.AlarmViewModel
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.main.MainViewModel
import run.piece.dev.refactoring.ui.newinvestment.InvestmentActivity
import run.piece.dev.refactoring.ui.newinvestment.InvestmentIntroActivity
import run.piece.dev.refactoring.ui.portfolio.PortfolioNewViewModel
import run.piece.dev.refactoring.ui.portfolio.list.NewPortfolioAdapter
import run.piece.dev.refactoring.utils.FragmentLifecycleOwner
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.ErrorActivity
import run.piece.dev.view.common.LoginChkActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.domain.refactoring.portfolio.model.PortfolioListVo

// 포트폴리오 리스트 ( 청약 리스트 )
@AndroidEntryPoint
class FragmentHome : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    protected val visibleLifecycleOwner: FragmentLifecycleOwner by lazy {
        FragmentLifecycleOwner()
    }
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var portfolioAdapter: NewPortfolioAdapter
    private lateinit var mainActivity: MainActivity

    private val viewModel by viewModels<PortfolioNewViewModel>()
    private val dataNexusViewModel by viewModels<DataNexusViewModel>()

    private var userName: String? = null

    private val achieveList = ArrayList<PortfolioItem.AchieveListModel>()
    private val combinedList = ArrayList<PortfolioItem.PortfolioListModel>() // 수익리포트 + 청약 리스트 데이터
    private var layoutManager: RecyclerView.LayoutManager? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coroutineScope = viewLifecycleOwner.lifecycleScope

        binding.apply {
            binding.fragment = this@FragmentHome
            binding.lifecycleOwner = viewLifecycleOwner
            binding.viewModel = viewModel
            binding.dataStoreViewModel = dataNexusViewModel
            rootLayout.setPadding(0, getStatusBarHeight(context = requireContext()), 0, 0)
            setStatusBarIconColor()
            requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            App()

            coroutineScope.launch {
                launch(Dispatchers.IO) {
                    this@FragmentHome.viewModel.getPortfolio()
                    if (this@FragmentHome.viewModel.isLogin.isNotEmpty() && this@FragmentHome.viewModel.memberId.isNotEmpty()) {
                        this@FragmentHome.viewModel.getAlarm("NTT01")
                        if (userName.isNullOrEmpty()) userName = dataNexusViewModel.getName()
                        else userName = PrefsHelper.read("name", "")
                    }
                }.join()

                launch(Dispatchers.Main) {
                    Log.v("combinedList : ", "${combinedList.size}")

                    try {
                        this@FragmentHome.viewModel.portfolioList.collect { data ->
                            when (data) {
                                is PortfolioNewViewModel.PortfolioState.Success -> {
                                    val loadingJob = async { loadingView() }
                                    val resultView = async {
                                        initPortfolioItem(data.isSuccess)
                                    }

                                    loadingJob.join()
                                    resultView.await()

                                    portfolioAdapter = NewPortfolioAdapter(
                                        requireContext(),
                                        this@FragmentHome.viewModel,
                                        achieveList,
                                        combinedList,
                                        this@FragmentHome.viewModel.getDisplayType(resources.displayMetrics),
                                        childFragmentManager,
                                        dataNexusViewModel.getInvestResult(),
                                        dataNexusViewModel.getInvestFinalScore(),
                                        dataNexusViewModel.getFinalVulnerable(),
                                        userName.default(),
                                        this@FragmentHome.viewModel.responseArray
                                    )
                                    binding.portfolioRv.adapter = portfolioAdapter
                                    binding.portfolioRv.itemAnimator = null
                                    val anim = binding.portfolioRv.itemAnimator
                                    if (anim is SimpleItemAnimator) {
                                        anim.supportsChangeAnimations = false
                                    }
                                    binding.portfolioRv.layoutManager = initAdapter(this@FragmentHome.viewModel.getDisplayType(resources.displayMetrics))
                                    portfolioAdapter.notifyData()

                                }

                                is PortfolioNewViewModel.PortfolioState.Failure -> {
                                    binding.loadingLv.visibility = View.GONE
                                }

                                else -> {}
                            }
                        }

                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        Log.e("Portfolio List Exception : ", "${exception.message}")
                    }
                }
            }
        }

        // 청약 리스트 lifecycle observer
        viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                Log.v("FragmentHome : ", event.name)
                when (event) {
                    Lifecycle.Event.ON_RESUME,
                    Lifecycle.Event.ON_PAUSE -> {
                        if (isHidden) {
                            wsConnected = false
                            visibleLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)

                            // onPause 이면서 onStop 일 경우 웹소켓 Cancel
                            wsClient?.dispatcher?.cancelAll()
                            wsClient?.dispatcher?.executorService?.shutdown()

                        } else {
                            visibleLifecycleOwner.handleLifecycleEvent(event)

                            if (!wsConnected) {
                                // client 객체 생성 및 웹소켓 연결 시도
                                wsClient = OkHttpClient()
                                wsClient?.newWebSocket(wsRequest, wsListener)
                                wsConnected = true
                            }
                        }
                    }

                    else -> {
                        visibleLifecycleOwner.handleLifecycleEvent(event)
                    }
                }
            }
        })

        coroutineScope.launch {
            wsListener.liveData.observe(viewLifecycleOwner, Observer {
                try {
                    if (it != null) {
                        wsObject.add("data", it)
                        wsResponse = wsObject.getAsJsonObject("data")
                        try {
                            binding.loadingLv.visibility = View.GONE

                            // 포트폴리오 웹소켓 데이터
                            Log.v("청약 리스트 웹소켓 : ", "${(wsResponse.getAsJsonArray("data") ?: null) as JsonArray}")
                            viewModel.ptWsArrayData((wsResponse.getAsJsonArray("data") ?: null) as JsonArray)
                            if (::portfolioAdapter.isInitialized) {
                                portfolioAdapter.notifyItemChanged(1, wsResponse.getAsJsonArray("data").size())
                            } else {
                                Log.e("PortfolioAdapter", "Not initialized")
                            }

                        } catch (ex: Exception) {

                            binding.loadingLv.visibility = View.VISIBLE
                            ex.printStackTrace()
                        }
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.e("Exception : ", "${exception.message}")
                }
            })
        }

        coroutineScope.launch {
            launch(Dispatchers.IO) {
                try {
                    if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                        viewModel.memberDeviceChk()
                        viewModel.getMemberData()
                        viewModel.getAlarm("NTT01")
                    }
                    viewModel.getPortfolio()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("Error Catch Dispatchers.IO: ", "${e.message}")
                }
            }

            launch(Dispatchers.Main) {
                try {
                    viewModel.portfolioList.collect { data ->
                        when (data) {
                            is PortfolioNewViewModel.PortfolioState.Success -> {
                                val loadingJob = async { loadingView() }
                                val resultView = async {
                                    initPortfolioItem(data.isSuccess)
                                }

                                loadingJob.join()
                                resultView.await()

                                viewModel.wsStatus.observe(visibleLifecycleOwner, Observer {
                                    Log.d("타이머 전달 : ", "$it")
                                    if (it == true) {
                                        initPortfolioItem(data.isSuccess)
                                    }
                                    portfolioAdapter.notifyData()
                                })

                            }

                            is PortfolioNewViewModel.PortfolioState.Failure -> {
                                binding.loadingLv.visibility = View.GONE
                            }

                            else -> {}
                        }
                    }

                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.e("Portfolio List Exception : ", "${exception.message}")
                }
            }

            // 투자성향 분석 배너
            launch(Dispatchers.Main) {
                try {
                    // 회원일 경우 배너 로직
                    if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                        // 회원 정보 조회 API Response Data를 가져온다.
                        this@FragmentHome.viewModel.memberInfo.collect { it ->
                            when (it) {
                                is MainViewModel.MemberInfoState.Success -> {

                                    PrefsHelper.write("name", it.memberVo.name)
                                    dataNexusViewModel.putName(it.memberVo.name)

                                    // 투자 성향 분석을 한번도 진행하지 않은 회원
                                    if (it.memberVo.preference == null) {

                                        binding.footerBannerLayout.visibility = View.VISIBLE
                                        CoroutineScope(Dispatchers.Main).launch {
                                            setupFooterBannerLayout(radius = viewModel.defaultBannerRadius(), name = it.memberVo.name)
                                        }
                                    }
                                    // 투자 성향 분석을 한번이라도 진행한 회원
                                    else {
                                        binding.footerBannerLayout.visibility = View.GONE
                                    }
                                }

                                is MainViewModel.MemberInfoState.Failure -> {
//                                    LogUtil.e("MainViewModel : ${it.message}")
                                }

                                else -> {
//                                    LogUtil.e("MainViewModel : $it")
                                }
                            }
                        }
                    }
                    // 비회원일 경우 배너 로직
                    else {
                        binding.footerBannerLayout.visibility = View.GONE
                    }

                } catch (exception: Exception) {
                    exception.printStackTrace()
                    Log.e("Exception ! :", "${exception.message}")
                }
            }


            // 알림조회
            launch(Dispatchers.Main) {
                try {
                    this@FragmentHome.viewModel.alarmList.collect { alarmState ->
                        when (alarmState) {
                            is AlarmViewModel.AlarmGetState.Success -> {
                                if (PrefsHelper.read("noti", "") == "Y") binding.notiIv.visibility = View.VISIBLE
                                else binding.notiIv.visibility = View.GONE
                            }

                            is AlarmViewModel.AlarmGetState.Failure -> {
                                binding.notiIv.visibility = View.GONE
                            }

                            else -> {
                                binding.notiIv.visibility = View.GONE
                            }
                        }
                    }
                } catch (e: Exception) {
                    LogUtil.e("Error UI tasks: ${e.message} ")
                }
            }


            // 디바이스 체크 ( 다른기기 로그아웃 처리 )
            launch(Dispatchers.Main) {
                try {
                    if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                        this@FragmentHome.viewModel.deviceChk.collect {
                            when (it) {
                                is MainViewModel.MemberDeviceState.Success -> {
//                                    LogUtil.e("=== MemberDeviceChk Success === ${it.isSuccess}")
                                }

                                is MainViewModel.MemberDeviceState.Failure -> {
//                                    LogUtil.e("=== MemberDeviceChk Failure === ${it.message}")
                                    PrefsHelper.removeKey("inputPinNumber")
                                    PrefsHelper.removeKey("memberId")
                                    PrefsHelper.removeKey("isLogin")
                                    startActivity(getIntroActivity(requireContext()))
                                    mainActivity.finish()
                                }

                                else -> {
//                                    LogUtil.e("=== MemberDeviceChk More === $it")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        // 당겨서 새로고침
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = false
            coroutineScope.launch {
                viewModel.getPortfolio()
                if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                    viewModel.getAlarm("NTT01")
                }
            }

            coroutineScope.launch(Dispatchers.Main) {
                viewModel.portfolioList.collect { data ->
                    when (data) {
                        is PortfolioNewViewModel.PortfolioState.Success -> {
                            Log.v("Success ", "Success ")
                            initPortfolioItem(data.isSuccess)
                        }

                        is PortfolioNewViewModel.PortfolioState.Failure -> {
                            Log.v("Fail ", "Fail ")
                        }

                        else -> {
                            Log.v("Else ", "Else ")
                        }
                    }
                }
            }
        }

        // 알림 및 혜택
        binding.notiLayout.onThrottleClick {
            if (viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                startActivity(getLoginChkActivity(requireContext()))
            } else {
                startActivity(getAlarmActivity(requireContext()))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = viewLifecycleOwner.lifecycleScope
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        wsClient?.dispatcher?.cancelAll()
        wsClient?.dispatcher?.executorService?.shutdown()
    }


    private suspend fun loadingView() {
        binding.loadingLv.visibility = View.VISIBLE
        delay(500)
        binding.loadingLv.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val recyclerViewState = binding.portfolioRv.layoutManager?.onSaveInstanceState()
        outState.putParcelable(KEY_RECYCLER_VIEW_STATE, recyclerViewState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { state ->
            val recyclerViewState = state.getParcelable<Parcelable>(KEY_RECYCLER_VIEW_STATE)
            recyclerViewState?.let {
                binding.portfolioRv.layoutManager?.onRestoreInstanceState(it)
            }
        }
    }

    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private fun setStatusBarIconColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else activity?.window?.decorView?.systemUiVisibility = activity?.window?.decorView!!.systemUiVisibility
    }


    // 투자성향 분석하기 배너
    private fun setupFooterBannerLayout(radius: Int, name: String) {
        coroutineScope.launch(Dispatchers.Main) {
            try {
                binding.footerBannerLayout.alpha = 0f
                binding.footerBannerLayout.visibility = View.GONE

                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                    viewModel.dpToPixels(72f)
                ).apply {
                    setMargins(
                        viewModel.footerBannerLeftMargins(),
                        0,
                        viewModel.footerBannerRightMargins(),
                        viewModel.footerBannerBottomMargins()
                    )
                    topToTop = viewModel.footerBannerTopToTop()
                    bottomToBottom = viewModel.footerBannerBottomToBottom()
                    startToStart = viewModel.footerBannerStartToStart()
                    endToEnd = viewModel.footerBannerEndToEnd()
                }

                binding.footerBannerLayout.layoutParams = layoutParams

                Glide.with(requireContext())
                    .load(ContextCompat.getDrawable(requireContext(), R.drawable.banner_h72_investmentdna))
                    .transform(RoundedCorners(viewModel.footerBannerRadius(requireContext(), radius)))
                    .into(binding.footerBannerLayout)

                // 투자 성향 분석 배너
                binding.footerBannerLayout.animate()
                    .alpha(1f)
                    .setDuration(200) // Set the duration as needed
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            binding.footerBannerLayout.visibility = View.VISIBLE
                            binding.footerBannerLayout.onThrottleClick {
                                startActivity(InvestmentIntroActivity.getIntent(requireContext(), name))
                            }
                        }
                    })

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initPortfolioItem(data: PortfolioListVo) {
        var portfolioList = ArrayList<PortfolioItem.PortfolioListModel>()
        portfolioList.clear()

        achieveList.clear()
        data.achieveInfos?.forEach { data ->
            val achieve = PortfolioItem.AchieveListModel(
                portfolioId = data.portfolioId.toString().default(),
                subTitle = data.subTitle.toString().default(),
                achieveProfitRate = data.achieveProfitRate.toString().default(),
                viewType = 0
            )
            achieveList.add(achieve)
        }
        achieveList.reverse()

        val subPortfolioList = PortfolioItem.PortfolioListModel(
            portfolioId = "",
            title = "",
            subTitle = "",
            representThumbnailImagePath = "",
            recruitmentState = "",
            recruitmentBeginDate = "",
            dividendsExpecatationDate = "",
            achievementRate = "",
            viewType = 0
        )

        // 폴드 테스트 데이터
//        for (index in 0 until 80) {
//            val portfolio = PortfolioItem.PortfolioListModel(
//                portfolioId = data.portfolios?.get(4)?.portfolioId.default(),
//                title = data.portfolios?.get(4)?.title.default(),
//                subTitle = data.portfolios?.get(4)?.subTitle.default(),
//                representThumbnailImagePath = data.portfolios?.get(4)?.representThumbnailImagePath.default(),
//                recruitmentState = data.portfolios?.get(4)?.recruitmentState.default(),
//                recruitmentBeginDate = data.portfolios?.get(4)?.recruitmentBeginDate.default(),
//                dividendsExpecatationDate = data.portfolios?.get(4)?.dividendsExpecatationDate.default(),
//                achievementRate = data.portfolios?.get(4)?.achievementRate.default(),
//                viewType = 1
//            )
//            portfolioList.add(portfolio)
//        }

        data.portfolios?.forEach { data ->
            val portfolio = PortfolioItem.PortfolioListModel(
                portfolioId = data.portfolioId.default(),
                title = data.title.default(),
                subTitle = data.subTitle.default(),
                representThumbnailImagePath = data.representThumbnailImagePath.default(),
                recruitmentState = data.recruitmentState.default(),
                recruitmentBeginDate = data.recruitmentBeginDate.default(),
                dividendsExpecatationDate = data.dividendsExpecatationDate.default(),
                achievementRate = data.achievementRate.default(),
                viewType = 1
            )
            portfolioList.add(portfolio)
        }

        // 두 개의 리스트를 합치기

        combinedList.clear()
        combinedList.addAll(listOf(subPortfolioList))
        combinedList.addAll(portfolioList)

        val anim = binding.portfolioRv.itemAnimator
        if (anim is SimpleItemAnimator) {
            anim.supportsChangeAnimations = false
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initAdapter(displayType: String): RecyclerView.LayoutManager? {
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val gridFullLayoutManager = GridLayoutManager(context, 2) // 2는 열의 수입니다.

        binding.portfolioRv.itemAnimator = null
        binding.portfolioRv.setHasFixedSize(false)
        // 첫 번째 아이템의 viewType이 0일 경우와 그렇지 않은 경우를 나누어 처리합니다.
        when (displayType) {
            "FOLD_DISPLAY_EXPAND" -> {
                layoutManager = gridFullLayoutManager
                // 첫 번째 아이템은 span을 1개로 설정합니다.
                gridFullLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) 2 else 1
                    }
                }
            }

            else -> {
                layoutManager = linearLayoutManager
            }
        }

        return layoutManager
    }

    companion object {
        // recyclerView ViewState 상태값 Key
        private const val KEY_RECYCLER_VIEW_STATE = "recycler_view_state"
        private val wsRequest: Request = Request.Builder().url(BuildConfig.PIECE_WS_PORTFOLIO).build()
        private var wsClient: OkHttpClient? = null
        private var wsListener = WebSocketListener()
        private var wsObject = JsonObject()
        private var wsResponse = JsonObject()
        private var wsConnected = false

        fun newInstance(title: String): FragmentHome {
            return FragmentHome().apply {
                arguments = bundleOf("title" to title)
            }
        }

        fun getErrorActivity(context: Context): Intent {
            return Intent(context, ErrorActivity::class.java)
        }

        // 네트워크 체크 화면 이동
        fun getNetWorkChkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        // 로그인 체크 이동
        fun getLoginChkActivity(context: Context): Intent {
            val intent = Intent(context, LoginChkActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }

        // 알림 화면 이동
        fun getAlarmActivity(context: Context): Intent {
            return Intent(context, AlarmActivity::class.java)
        }

        // 로그인, 서비스 둘러보기 화면 이동
        fun getIntroActivity(context: Context): Intent {
            val intent = Intent(context, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}

package run.piece.dev.refactoring.base.html

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import run.piece.dev.R
import run.piece.dev.databinding.ActivityBaseWebviewBinding
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.dev.refactoring.utils.toDotDateFormat
import run.piece.dev.view.common.NetworkActivity
import run.piece.domain.refactoring.board.model.FilesVo


// 웹뷰 공통 Activity

@AndroidEntryPoint
class BaseWebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBaseWebviewBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel: BaseWebViewModel by viewModels()

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this@BaseWebViewActivity
        binding.activity = this@BaseWebViewActivity
        binding.viewModel = viewModel
        binding.apply {
            if (!isNetworkConnected(this@BaseWebViewActivity)) {
                startActivity(NewPassCodeActivity.getNetworkActivity(this@BaseWebViewActivity))
            }

            window?.apply {
                window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }

            setStatusBarSetting(true)
            naviBarSetting(false)
        }

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onWebViewSetting(viewModel.startType())

                // 공통 Header
                intent?.let {
                    with(binding) {
                        titleTv.text = it.getStringExtra("topTitle")
                        subTitleTv.text = it.getStringExtra("title")

                        // 숨김 처리 Default
                        shareImg.visibility = View.GONE
                    }
                }

                // Type
                // 0: 공통 , 1: 라운지 , 2: 스토리
                // 3: 약관 , 4: 투자공시 , 5: 공지사항
                // 6: 이벤트
                when (viewModel.startType()) {
                    0, 1, 2 -> {
                        // 공통 웹뷰
                        // 라운지 웹뷰
                        // 스토리 웹뷰
                        intent?.let {
                            with(binding) {
                                // 공통 Header
                                subTitleTv.visibility = View.GONE
                                statusTv.visibility = View.GONE
                                createAtTv.visibility = View.GONE
                                line.visibility = View.GONE

                                it.getStringExtra("contents")?.let { html ->
                                    webView.loadDataWithBaseURL(
                                        null, html, "text/html; charset=utf-8", "utf-8", null
                                    )
                                }
                            }
                        }

                    }

                    3, 5 -> {
                        // 약관 웹뷰
                        // 투자공시 웹뷰
                        // 공지사항 웹뷰
                        intent?.let { data ->
                            // 공지사항
                            data.getStringExtra("boardId")?.let {
                                launch(Dispatchers.IO) {
                                    viewModel.getNoticeDetail(it, "v0.0.2")
                                }

                                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                                    with(binding) {
                                        shareImg.visibility = View.GONE

                                        this@BaseWebViewActivity.viewModel.noticeDetail.collect { notice ->
                                            when (notice) {
                                                is BaseWebViewModel.NoticeDetailState.Success -> {
                                                    subTitleTv.text = notice.noticeDetailVo.title

                                                    if (data.getStringExtra("tabDvn").isNullOrEmpty()) {
                                                        // 공지사항 날짜
                                                        createAtTv.text = notice.noticeDetailVo.createdAt.toBaseDateFormat()
                                                    } else {
                                                        statusTv.text = "${data.getStringExtra("tabDvn")} | "
                                                        // 투자 공시 날짜
                                                        createAtTv.text = notice.noticeDetailVo.createdAt.toDotDateFormat()
                                                    }



                                                    webView.loadDataWithBaseURL(
                                                        null, notice.noticeDetailVo.contents, "text/html; charset=utf-8", "utf-8", null
                                                    )
                                                }

                                                is BaseWebViewModel.NoticeDetailState.Failure -> {
                                                    startActivity(Intent(this@BaseWebViewActivity, NetworkActivity::class.java))
                                                    binding.loading.visibility = View.GONE
                                                }

                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 투자 공시
                    4 -> {
                        intent?.let {
                            with(binding) {
                                // 공통 Header
                                subTitleTv.visibility = View.VISIBLE
                                statusTv.visibility = View.VISIBLE
                                createAtTv.visibility = View.VISIBLE
                                line.visibility = View.VISIBLE

                                it.getStringExtra("topTitle")?.let { topTitle ->
                                    titleTv.text = topTitle
                                }
                                it.getStringExtra("codeName")?.let { codeName ->
                                    statusTv.text = "$codeName | "
                                }
                                it.getStringExtra("createAt")?.let {createAt ->
                                    createAtTv.text = createAt
                                }

                                it.getStringExtra("contents")?.let { html ->
                                    webView.loadDataWithBaseURL(
                                        null, html, "text/html; charset=utf-8", "utf-8", null
                                    )
                                }
                            }
                        }
                    }

                    // 이벤트 웹뷰
                    6 -> {
                        intent?.let {
                            it.getStringExtra("eventId")?.let {
                                binding.subTitleTv.visibility = View.GONE
                                binding.statusTv.visibility = View.GONE
                                binding.createAtTv.visibility = View.GONE
                                binding.line.visibility = View.GONE

                                launch(Dispatchers.IO) {
                                    viewModel.getEventDetail()
                                }

                                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                                    viewModel.eventDetail.collect { data ->
                                        when(data) {
                                            is BaseWebViewModel.EventDetailState.Success -> {
                                                with(binding) {
                                                    webView.loadDataWithBaseURL(null, data.eventDetailVo.contents, "text/html; charset=utf-8", "utf-8", null)

                                                    shareImg.visibility = View.VISIBLE
                                                    shareImg.onThrottleClick {
                                                        val shareData: Intent = Intent().apply {
                                                            action = Intent.ACTION_SEND
                                                            putExtra(Intent.EXTRA_TEXT, data.eventDetailVo.shareUrl)
                                                            putExtra(Intent.EXTRA_SUBJECT, data.eventDetailVo.title)
                                                            type = "text/plain"
                                                        }
                                                        val shareIntent = Intent.createChooser(shareData, null)
                                                        startActivity(shareIntent)
                                                    }
                                                }
                                            }
                                            is BaseWebViewModel.EventDetailState.Failure -> {

                                            }
                                            else -> {

                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                binding.backLayout.onThrottleClick {
                    BackPressedUtil().activityFinish(this@BaseWebViewActivity,this@BaseWebViewActivity)
                }

            }
        }


        BackPressedUtil().activityCreate(this@BaseWebViewActivity,this@BaseWebViewActivity)
        BackPressedUtil().systemBackPressed(this@BaseWebViewActivity,this@BaseWebViewActivity)

    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }


    inner class WebViewClient : android.webkit.WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?, request: WebResourceRequest?
        ): Boolean {
            if (request != null) {
                view?.loadUrl(request.url.toString())
            }
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.loading.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.loading.visibility = View.GONE
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
        }

        override fun onReceivedSslError(
            view: WebView?, handler: SslErrorHandler?, error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    fun onWebViewSetting(type: Int) {
        binding.webView.apply {
            webViewClient = WebViewClient() // 새창안뜨게
            settings.javaScriptEnabled = true // 자바스크립트 허용
            settings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부
            settings.javaScriptCanOpenWindowsAutomatically = false // 자바스크립트 새창 띄우기 (멀티뷰) 허용 여부
            settings.loadWithOverviewMode = true // 메타태그 허용
            when (type) {
                6 -> {
                    settings.useWideViewPort = true // 화면 사이즈 맞추기 허용
                }

                else -> {
                    settings.useWideViewPort = false // 화면 사이즈 맞추기 허용
                }
            }
            settings.setSupportZoom(false) // 화면 줌 허용 여부
            settings.builtInZoomControls = false // 화면 확대 축소 허용 여부
            // 운영중엔 주석처리 - jhm 2022/12/06
            settings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용 여부
            settings.domStorageEnabled = true // 로컬 저장소 허용 여부
            settings.displayZoomControls = false // 줌 컨트롤 허용 여부

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true // api 26
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                settings.mediaPlaybackRequiresUserGesture = false
            }
            settings.allowContentAccess = true
            settings.setGeolocationEnabled(false) // 위치 허용 여부
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.allowUniversalAccessFromFileURLs = true
            }
            fitsSystemWindows = true
            settings.allowFileAccess = true

            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
        }
    }

    companion object {
        // 기본 공통 웹뷰 호출
        fun commonIntent(context: Context, viewName: String, topTitle: String, contents: String): Intent {
            val intent = Intent(context, BaseWebViewActivity::class.java)
            intent.putExtra("viewName", viewName)
            intent.putExtra("topTitle", topTitle)
            intent.putExtra("contents", contents)
            return intent
        }

        // 공지사항 상세
        fun getNoticeDetail(context: Context, viewName: String, topTitle: String, boardId: String, title: String, createAt: String): Intent {
            val intent = Intent(context, BaseWebViewActivity::class.java)
            intent.putExtra("viewName", viewName)
            intent.putExtra("topTitle", topTitle)
            intent.putExtra("boardId", boardId)
            intent.putExtra("title", title)
            intent.putExtra("createAt", createAt)
            return intent
        }

        fun getNoticeDetail(context: Context, viewName: String, topTitle: String, boardId: String): Intent {
            val intent = Intent(context, BaseWebViewActivity::class.java)
            intent.putExtra("viewName",viewName)
            intent.putExtra("topTitle", topTitle)
            intent.putExtra("boardId", boardId)
            return intent
        }

        // 이벤트 상세
        fun getEventDetail(context: Context, viewName: String, eventId: String): Intent {
            val intent = Intent(context, BaseWebViewActivity::class.java)
            intent.putExtra("viewName", viewName)
            intent.putExtra("eventId", eventId)
            return intent
        }
    }


    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    private fun setStatusBarSetting(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = if (isBlack) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else window.decorView.systemUiVisibility

            window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }

    private fun naviBarSetting(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능
            window.insetsController?.setSystemBarsAppearance(
                if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                else 0, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 아이콘 색상이 8.0부터 가능하므로 커스텀은 동시에 진행해야 하므로 조건 동일 처리.
            // 기존 uiVisibility 유지
            // -> 0으로 설정할 경우, 상태바 아이콘 색상 설정 등이 지워지기 때문
            window.decorView.systemUiVisibility = if (isBlack) View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else window.decorView.systemUiVisibility

            // 내비바 배경색은 8.0부터 지원한다.
            window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        }
    }

}
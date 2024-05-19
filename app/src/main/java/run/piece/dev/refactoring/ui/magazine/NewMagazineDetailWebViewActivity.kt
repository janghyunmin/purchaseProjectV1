package run.piece.dev.refactoring.ui.magazine

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.android.tools.build.jetifier.core.utils.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityNewMagazineWebviewBinding
import run.piece.dev.refactoring.ui.bookmark.NewBookMarkActivity
import run.piece.dev.refactoring.utils.*
import run.piece.dev.view.common.LoginChkActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.extension.SnackBarCommon
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.vo.MagazineDetailVo
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo

@AndroidEntryPoint
class NewMagazineDetailWebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewMagazineWebviewBinding
    private lateinit var coroutineScope: CoroutineScope
    private val viewModel: NewMagazineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        App()
        
        binding = ActivityNewMagazineWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val networkConnection = NetworkConnection(this@NewMagazineDetailWebViewActivity)
        networkConnection.observe(this@NewMagazineDetailWebViewActivity) { isConnected ->
            if (!isConnected) startActivity(getNetworkActivity(this@NewMagazineDetailWebViewActivity))
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE) //캡처 방지
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        coroutineScope = lifecycleScope


        binding.apply {
            activity = this@NewMagazineDetailWebViewActivity
            lifecycleOwner = this@NewMagazineDetailWebViewActivity
            vm = viewModel

            backImgIv.onThrottleClick {
                BackPressedUtil().activityFinish(this@NewMagazineDetailWebViewActivity,this@NewMagazineDetailWebViewActivity)
            }

            optionLayout.onThrottleClick {
                val bundle = Bundle()
                bundle.putString("shareUrl", shareUrl) // 공유되는 주소
                bundle.putString("magazineId", magazineId)
                bundle.putString("isFavorite", isFavorite) // 북마크 판별값

                val dialog = NewMagazineBtDlg(
                    this@NewMagazineDetailWebViewActivity,
                    R.layout.slideup_shared
                )

                dialog.apply {
                    showDialog(isFavorite = isFavorite)
                    setCallback(object  : NewMagazineBtDlg.OnSendFromBottomSheetDialog {
                        override fun sendValue(value: String, boolean: Boolean) {
                            when(value) {
                                "공유" -> {
                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.type = "text/plain"

                                    //공유 가능한 앱들을 실행하는 Action값으로 intent생성
                                    // String으로 받아서 넣기
                                    val shareUrl = shareUrl
                                    val shareTitle = title.toString() + "\n" + shareUrl

                                    intent.putExtra(Intent.EXTRA_TEXT, shareTitle)

                                    val shareIntent = Intent.createChooser(intent, "PIECE")
                                    startActivity(shareIntent)
                                }

                                "링크 복사" -> {
                                    val snackBar = SnackBarCommon(binding.root,"링크를 복사했어요.","링크복사")
                                    if (resources.displayMetrics.widthPixels < 1600) snackBar.show(16, 16)
                                    else snackBar.show(16)

                                    val clipboard: ClipboardManager =
                                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip =
                                        ClipData.newPlainText(
                                            "magazineLink",
                                            "$shareUrl"
                                        )
                                    clipboard.setPrimaryClip(clip)
                                }
                                "북마크" -> {
                                    if(viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                                        startActivity(getLoginChkActivity(this@NewMagazineDetailWebViewActivity))
                                    }
                                    else {

                                        if (isFavorite == "N") {
                                            val memberBookmarkRegModel = MemberBookmarkRegModel(viewModel.memberId, magazineId)

                                            coroutineScope.launch {
                                                withContext(this.coroutineContext) {
                                                    viewModel.updateBookMark(memberBookmarkRegModel)
//                                                    viewModel.getMagazineDetailMember(magazineId)
                                                    // isFavorite 값을 갱신합니다.
                                                    isFavorite = "Y"
                                                }
                                            }

                                            val snackBarCommon = SnackBarCommon(
                                                binding.root,
                                                "북마크에 담았어요.","북마크"
                                            )
                                            snackBarCommon.setItemClickListener(object : SnackBarCommon.OnItemClickListener {
                                                override fun onClick(v: View) {
                                                    val intent = Intent(context, NewBookMarkActivity::class.java)

                                                    startActivity(intent)
                                                }
                                            })
                                            snackBarCommon.show(16)

                                        } else {
                                            val memberBookmarkRemoveModel = MemberBookmarkRemoveModel(viewModel.memberId, magazineId)
                                            coroutineScope.launch {
                                                withContext(this.coroutineContext) {
                                                    viewModel.deleteBookMark(memberBookmarkRemoveModel)
//                                                    viewModel.getMagazineDetailMember(magazineId)
                                                    // isFavorite 값을 갱신합니다.
                                                    isFavorite = "N"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    })
                }

            }


        }

        BackPressedUtil().activityCreate(this@NewMagazineDetailWebViewActivity,this@NewMagazineDetailWebViewActivity)
        BackPressedUtil().systemBackPressed(this@NewMagazineDetailWebViewActivity,this@NewMagazineDetailWebViewActivity)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
        intent?.let {
            magazineId = it.getStringExtra("magazineId").toString().default()
            Log.v("MagazineId : ", magazineId)

            // 비 회원 일 경우
            if(viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                coroutineScope.launch(Dispatchers.IO) {
                    withContext(this.coroutineContext) {
                        viewModel.getMagazineDetailNotMember(magazineId)
                    }
                }

                coroutineScope.launch(Dispatchers.Main) {
                    webViewClientInit().join()

                    viewModel.magazineDetailNotMember.collect { vo ->
                        when(vo) {
                            is NewMagazineViewModel.MagazineDetailNotMemberState.Success -> {
                                notMemberWebViewInit(vo = vo.data).join()
                            }
                            is NewMagazineViewModel.MagazineDetailNotMemberState.Failure -> {}
                            else -> {}
                        }

                    }
                }

            } else {
                // 회원으로 진입 할 경우
                coroutineScope.launch(Dispatchers.IO) {
                    withContext(this.coroutineContext) {
                        viewModel.getMagazineDetailMember(magazineId)
                    }
                }
                coroutineScope.launch(Dispatchers.Main) {
                    webViewClientInit().join()
                    viewModel.magazineDetailMember.collect { vo ->
                        when(vo) {
                            is NewMagazineViewModel.MagazineDetailMemberState.Success -> {
                                memberWebViewInit(vo = vo.data).join()
                            }
                            is NewMagazineViewModel.MagazineDetailMemberState.Failure -> {}
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun webViewClientInit() = coroutineScope.launch {
        delay(50)
        binding.webView.apply {
            webViewClient = WebViewClient() // 새창안뜨게 - jhm 2022/08/30
            settings.javaScriptEnabled = true // 자바스크립트 허용 - jhm 2022/08/30
            settings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부 - jhm 2022/08/30
            settings.javaScriptCanOpenWindowsAutomatically =
                false // 자바스크립트 새창 띄우기 (멀티뷰) 허용 여부 - jhm 2022/08/30
            settings.loadWithOverviewMode = true // 메타태그 허용 - jhm 2022/08/30
            settings.useWideViewPort = true // 화면 사이즈 맞추기 허용 - jhm 2022/08/30
            settings.setSupportZoom(false) // 화면 줌 허용 여부 - j부m 2022/08/30
            settings.builtInZoomControls = false // 화면 확대 축소 허용 여부 - jhm 2022/08/30

            // 운영중엔 주석처리 - jhm 2022/12/06
            settings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용 여부 - jhm 2022/08/30
            settings.domStorageEnabled = true // 로컬 저장소 허용 여부 - jhm 2022/08/30
            settings.displayZoomControls = false // 줌 컨트롤 허용 여부 - jhm 2022/08/30

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true // api 26 - jhm 2022/08/30
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                settings.mediaPlaybackRequiresUserGesture = false
            }
            settings.allowContentAccess = true
            settings.setGeolocationEnabled(false) // 위치 허용 여부 - jhm 2022/08/30
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                settings.allowUniversalAccessFromFileURLs = true
            }
            fitsSystemWindows = true
            settings.allowFileAccess = true

            isHorizontalScrollBarEnabled = false
            isVerticalScrollBarEnabled = false
        }
    }
    inner class WebViewClient : android.webkit.WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
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
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
        }
    }

    @SuppressLint("SetTextI18n")
    suspend fun notMemberWebViewInit(vo: MagazineDetailVo) = coroutineScope.launch {
        binding.loading.visibility = View.VISIBLE
        delay(200)

        binding.loading.visibility = View.GONE

        shareUrl = vo.shareUrl
        title = vo.title
        url = vo.contents
        binding.titleTv.text = vo.title
        binding.subTitleTv.text = vo.smallTitle + " | " + (vo.createdAt?.toBasicDateFormat() ?: "")

        binding.webView.loadDataWithBaseURL(
            null,
            vo.contents.toString(),
            "text/html; charset=utf-8",
            "utf-8",
            null
        )
    }

    @SuppressLint("SetTextI18n")
    suspend fun memberWebViewInit(vo: MagazineItemVo) = coroutineScope.launch {
        binding.loading.visibility = View.VISIBLE
        delay(200)

        binding.loading.visibility = View.GONE

        isFavorite = vo.isFavorite
        shareUrl = vo.shareUrl
        title = vo.title
        url = vo.contents
        binding.titleTv.text = vo.title
        binding.subTitleTv.text = vo.smallTitle + " | " + (vo.createdAt?.toBasicDateFormat() ?: "")



        binding.webView.loadDataWithBaseURL(
            null,
            vo.contents.toString(),
            "text/html; charset=utf-8",
            "utf-8",
            null
        )
    }



    companion object {
        private var magazineId: String = ""
        private var shareUrl: String? = ""
        private var title: String? = ""
        private var url: String? = ""
        private var isFavorite: String? = ""
        private var createAt: String? = ""
        private var subTitle: String? = ""

        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        // 로그인 화면 이동
        fun getLoginChkActivity(context: Context) : Intent {
            val intent = Intent(context, LoginChkActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }
}
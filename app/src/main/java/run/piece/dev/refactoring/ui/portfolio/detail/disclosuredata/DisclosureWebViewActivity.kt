package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityDisclosureWebviewBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.board.model.FilesVo


@AndroidEntryPoint
class DisclosureWebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDisclosureWebviewBinding
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var fileRvAdapter: DisclosureFileAdapter
    private val fileDataItem = mutableListOf<DisclosureFileDataItem>()
    private var isReceiverRegistered = false

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisclosureWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        val networkConnection = NetworkConnection(this@DisclosureWebViewActivity)
        networkConnection.observe(this@DisclosureWebViewActivity) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this@DisclosureWebViewActivity, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.lifecycleOwner = this@DisclosureWebViewActivity
        binding.activity = this@DisclosureWebViewActivity
        binding.apply {

            window?.apply {
                window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }

            setStatusBarSetting(true)
            naviBarSetting(false)

            fileRvAdapter = DisclosureFileAdapter(this@DisclosureWebViewActivity)
            filesRv.run {
                adapter = fileRvAdapter
                this.layoutManager = LinearLayoutManager(this@DisclosureWebViewActivity, RecyclerView.VERTICAL, false)
            }
        }

        coroutineScope = lifecycleScope

        initWebViewSetting()
        fileDataItem.clear()

        intent?.let {
            val filesVo: List<FilesVo>? = it.getParcelableArrayListExtra("files")

            filesVo?.let { vo ->
                vo.forEach { item ->
                    fileDataItem.apply {
                        add(
                            DisclosureFileDataItem(
                                item.fileId,
                                item.originFileName,
                                item.cdnFilePath
                            )
                        )
                    }
                }
                fileRvAdapter.pdfFileItem = fileDataItem
                fileRvAdapter.notifyDataSetChanged()

                // File 클릭 및 다운로드
                fileRvAdapter.setItemClickListener(object : DisclosureFileAdapter.OnItemClickListener {
                    override fun downLoad(v: View, position: Int, originFileName: String, cdnFilePath: String) {
                        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val fileUri = Uri.parse(cdnFilePath)
                        val request = DownloadManager.Request(fileUri)
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${originFileName}.pdf")
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // 앱 상단에 다운로드 표시
                        request.setAllowedOverMetered(true)
                        request.setAllowedOverRoaming(true)
                        request.setDescription("파일 다운로드 진행중입니다.") // 다운로드 중 표시되는 내용
                        request.setNotificationVisibility(1) // 앱 상단에 다운로드 상태 표시
                        request.setMimeType("application/pdf") // MIME 유형 설정

                        binding.loading.visibility = View.VISIBLE

                        val downloadId = downloadManager.enqueue(request)
                        registerDownloadReceiver(downloadId)
                    }
                })
            }

            with(binding) {
                // 공통 Header
                subTitleTv.visibility = View.VISIBLE
                statusTv.visibility = View.VISIBLE
                createAtTv.visibility = View.VISIBLE
                line.visibility = View.VISIBLE

                it.getStringExtra("topTitle")?.let { topTitle ->
                    titleTv.text = topTitle
                }

                if(it.getStringExtra("codeName").default().isNotEmpty()) {
                    statusTv.text = "${it.getStringExtra("codeName")} | "
                } else {
                    statusTv.text = ""
                }

                it.getStringExtra("title")?.let { title ->
                    subTitleTv.text = title
                }

                it.getStringExtra("createAt")?.let { createAt ->
                    createAtTv.text = createAt
                }

                it.getStringExtra("contents")?.let { html ->
                    webView.loadDataWithBaseURL(
                        null, html, "text/html; charset=utf-8", "utf-8", null
                    )
                }
            }
        }

        binding.backLayout.onThrottleClick {
            BackPressedUtil().activityFinish(this@DisclosureWebViewActivity,this@DisclosureWebViewActivity)
        }

        BackPressedUtil().activityCreate(this@DisclosureWebViewActivity,this@DisclosureWebViewActivity)
        BackPressedUtil().systemBackPressed(this@DisclosureWebViewActivity,this@DisclosureWebViewActivity)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun initWebViewSetting() {
        binding.webView.apply {
            webViewClient = WebViewClient() // 새창안뜨게
            settings.javaScriptEnabled = true // 자바스크립트 허용
            settings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부
            settings.javaScriptCanOpenWindowsAutomatically = false // 자바스크립트 새창 띄우기 (멀티뷰) 허용 여부
            settings.loadWithOverviewMode = true // 메타태그 허용
            settings.useWideViewPort = false // 화면 사이즈 맞추기 허용
            settings.setSupportZoom(false) // 화면 줌 허용 여부
            settings.builtInZoomControls = false // 화면 확대 축소 허용 여부
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

    // 파일 다운로드 진행 receiver
    private fun registerDownloadReceiver(downloadId: Long) {
        if(!isReceiverRegistered) {
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("Range")
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null) {
                        val action = intent.action
                        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                            // Download completed, handle completion
                            val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val query = DownloadManager.Query().setFilterById(downloadId)
                            val cursor = downloadManager.query(query)
                            if (cursor.moveToFirst()) {
                                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(700)
                                        Toast.makeText(context, "다운로드 완료", Toast.LENGTH_SHORT).show()
                                        binding.loading.visibility = View.GONE
                                    }

                                } else if (status == DownloadManager.STATUS_FAILED) {
                                    binding.loading.visibility = View.GONE
                                    Toast.makeText(context, "다운로드에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    binding.loading.visibility = View.VISIBLE
                                }
                            }
                            cursor.close()
                        } else if (action == DownloadManager.ACTION_NOTIFICATION_CLICKED) {
                            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context?.startActivity(intent)
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, RECEIVER_EXPORTED)
            }else {
                registerReceiver(receiver, filter)
            }
            isReceiverRegistered = true
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
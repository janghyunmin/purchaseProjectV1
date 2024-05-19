package run.piece.dev.refactoring.base

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rajat.pdfviewer.PdfRendererView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityBasePdfBinding
import run.piece.dev.refactoring.ui.deed.DeedPdfViewModel
import run.piece.dev.refactoring.ui.portfolio.detail.pdf.PurchaseManualPdfActivity
import run.piece.dev.refactoring.ui.purchase.PurchaseRenewalActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.portfolio.model.PortfolioStockItemVo

/**
 * PDF 공통 Base Activity
 *
 * 청약 상세시 PDF 코듸 정의
 * attachFile Code
 *
 * PAF0201 : 증권신고서
 * PAF0202 : 투자설명서
 * PAF0203 : 청약안내문
 * PAF0204 : 투자계약서
 * PAF0205 : 소유권 증명서
 *
 * */
@AndroidEntryPoint
class BasePdfActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBasePdfBinding
    private lateinit var coroutineScope: CoroutineScope
    private val vm: DeedPdfViewModel by viewModels()
    private var isReceiverRegistered = false // File Download register

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        binding = ActivityBasePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val networkConnection = NetworkConnection(this@BasePdfActivity)
            networkConnection.observe(this@BasePdfActivity) { isConnected ->
                if (!isConnected) startActivity(getNetworkActivity(this@BasePdfActivity))
            }

            lifecycleOwner = this@BasePdfActivity
            activity = this@BasePdfActivity
            viewModel = vm
            coroutineScope = lifecycleScope

        }
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }


        coroutineScope.launch {
            with(binding) {
                activity = this@BasePdfActivity

                // 공통 Header Title
                vm.title?.let { titleTv.text = it }

                loadingView.visibility = View.VISIBLE

                // 공통 pdfUrl
                vm.pdfUrl?.let { url ->
                    vm.showPdf(
                        "Wallet",
                        this@BasePdfActivity,
                        pdfView,
                        loadingView,
                        url,
                        lifecycleScope,
                        lifecycle
                    )
                }

                vm.viewType?.let { type ->
                    when (type) {
                        // 투자 계약서
                        "PortfolioManual" -> {
                            if (vm.attachFileVo.isNullOrEmpty()) {
                                binding.loadingView.visibility = View.VISIBLE
                            } else {

                                shareLayout.visibility = View.GONE

                                vm.attachFileVo?.let { attachFileList ->

                                    pdfView.statusListener = object : PdfRendererView.StatusCallBack {
                                        override fun onPdfLoadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {
                                            super.onPdfLoadProgress(progress, downloadedBytes, totalBytes)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                loadingView.visibility = View.VISIBLE
                                            }
                                        }

                                        override fun onPdfLoadStart() {
                                            super.onPdfLoadStart()
                                            CoroutineScope(Dispatchers.Main).launch {
                                                loadingView.visibility = View.VISIBLE
                                            }
                                        }

                                        override fun onPdfLoadSuccess(absolutePath: String) {
                                            super.onPdfLoadSuccess(absolutePath)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                loadingView.visibility = View.GONE
                                            }
                                        }

                                        override fun onPageChanged(currentPage: Int, totalPage: Int) {
                                            super.onPageChanged(currentPage, totalPage)


                                            if (currentPage == totalPage - 1) { // 마지막 보여짐... ex) 53 == 54 -1
                                                Glide.with(this@BasePdfActivity).load(R.drawable.ic_x16_check_white).into(binding.btnIv)
                                                purchaseBtn.background = ContextCompat.getDrawable(this@BasePdfActivity, R.drawable.layout_round_10cfc9)
                                                btnTitle.apply {
                                                    text = getString(R.string.scroll_btn_txt_next)
                                                    setTextColor(ContextCompat.getColor(this@BasePdfActivity, R.color.white))

                                                    purchaseBtn.onThrottleClick {
                                                        vm.detailDefaultVo?.let { detailDefaultVo ->
                                                            vm.stockVo?.run {
                                                                vm.attachFileVo?.let { attachFiles ->

                                                                    purchaseBtn.background = ContextCompat.getDrawable(this@BasePdfActivity, R.drawable.layout_round_10cfc9)

                                                                    startActivity(
                                                                        getPurchaseRenewalActivity(
                                                                            this@BasePdfActivity,
                                                                            detailDefaultVo = detailDefaultVo,
                                                                            stockVo = this,
                                                                            attachFileItemVo = attachFileList,
                                                                            attachFileCode = Uri.parse(attachFileList.getAttachFilePositionUrl("PAF0202")).toString()
                                                                        )
                                                                    )
                                                                    BackPressedUtil().activityFinish(this@BasePdfActivity, this@BasePdfActivity)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                            } else {
                                                Glide.with(this@BasePdfActivity).load(R.drawable.ic_x16_arrow_down_c10cfc9).into(btnIv)
                                                purchaseBtn.background = ContextCompat.getDrawable(this@BasePdfActivity, R.drawable.layout_border_10cfc9)
                                                btnTitle.apply {
                                                    text = getString(R.string.scroll_btn_txt)
                                                    setTextColor(ContextCompat.getColor(this@BasePdfActivity, R.color.c_10cfc9))
                                                }

                                                purchaseBtn.onThrottleClick {
                                                    if (totalPage >= 20) {
                                                        pdfView.recyclerView.anchorSmoothScrollToPosition(totalPage, 10)
                                                    } else {
                                                        pdfView.recyclerView.smoothScrollToPosition(totalPage)
                                                    }
                                                }
                                            }
                                        }

                                        override fun onError(error: Throwable) {
                                            super.onError(error)
                                            loadingView.visibility = View.GONE
                                        }
                                    }

                                    // 투자 계약서
                                    pdfView.initWithUrl(
                                        url = Uri.parse(attachFileList.getAttachFilePositionUrl("PAF0204")).toString(),
                                        lifecycleCoroutineScope = lifecycleScope,
                                        lifecycle = lifecycle
                                    )
                                }
                            }

                            backLayout.onThrottleClick {
                                vm.detailDefaultVo?.let { detailDefaultVo ->
                                        vm.attachFileVo?.let { attachFiles ->
                                            startActivity(
                                                vm.stockVo?.let { stockVo ->
                                                    getPurchaseManualPdfIntent(
                                                        context = this@BasePdfActivity,
                                                        title = getString(R.string.purchase_manual_txt),
                                                        detailDefaultVo = detailDefaultVo,
                                                        stockVo = stockVo,
                                                        attachFileItemVo = attachFiles,
                                                        viewType = "ReturnActivity"
                                                    )
                                                }
                                            )
                                            BackPressedUtil().activityFinish(this@BasePdfActivity, this@BasePdfActivity)
                                        }
                                }
                            }

                            val backPressCallBack = object : OnBackPressedCallback(true) {
                                override fun handleOnBackPressed() {
                                    vm.detailDefaultVo?.let { detailDefaultVo ->
                                        vm.attachFileVo?.let { attachFiles ->
                                            startActivity(
                                                vm.stockVo?.let { stockVo ->
                                                    getPurchaseManualPdfIntent(
                                                        context = this@BasePdfActivity,
                                                        title = getString(R.string.purchase_manual_txt),
                                                        detailDefaultVo = detailDefaultVo,
                                                        stockVo = stockVo,
                                                        attachFileItemVo = attachFiles,
                                                        viewType = "ReturnActivity"
                                                    )
                                                }
                                            )
                                            BackPressedUtil().activityFinish(this@BasePdfActivity, this@BasePdfActivity)
                                        }
                                    }
                                }
                            }
                            this@BasePdfActivity.onBackPressedDispatcher.addCallback(this@BasePdfActivity, backPressCallBack)
                        }


                        "PortfolioDetailAssetActivity","NewPurchaseDetailActivity" -> {
                            backLayout.visibility = View.VISIBLE
                            purchaseBtn.visibility = View.GONE
                            shareLayout.visibility = View.GONE

                            backLayout.onThrottleClick {
                                BackPressedUtil().activityFinish(this@BasePdfActivity, this@BasePdfActivity)
                            }
                            BackPressedUtil().systemBackPressed(this@BasePdfActivity, this@BasePdfActivity)
                        }

                        // 청약 신청시 거래 내역 확인 BtDlg
                        "PurchaseBtDlg" -> {
                            backLayout.visibility = View.VISIBLE
                            purchaseBtn.visibility = View.GONE
                            shareLayout.visibility = View.VISIBLE
                            shareLayout.onThrottleClick {
                                vm.detailDefaultVo?.let { detailDefaultVo ->
                                    vm.attachFileVo?.let { attachFiles ->
                                        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        val fileUri = Uri.parse(attachFiles.getAttachFilePositionUrl("PAF0204"))
                                        val request = DownloadManager.Request(fileUri)

                                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${attachFiles.getAttachFilePositionName("PAF0202")}.pdf") // 확장자 명시

                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // 앱 상단에 다운로드 표시
                                        request.setAllowedOverMetered(true)
                                        request.setAllowedOverRoaming(true)
                                        request.setDescription("파일 다운로드 진행중입니다.") // 다운로드 중 표시되는 내용
                                        request.setNotificationVisibility(1) // 앱 상단에 다운로드 상태 표시
                                        request.setMimeType("application/pdf") // MIME 유형 설정

                                        binding.loadingView.visibility = View.VISIBLE

                                        val downloadId = downloadManager.enqueue(request)
                                        registerDownloadReceiver(downloadId)
                                    } ?: run {
                                        LogUtil.v("attachFiles run..")
                                    }
                                } ?: run {
                                    LogUtil.v("detailDefaultVo run..")
                                }
                            }

                            backLayout.onThrottleClick {
                                BackPressedUtil().activityFinish(this@BasePdfActivity, this@BasePdfActivity)
                            }
                            BackPressedUtil().systemBackPressed(this@BasePdfActivity, this@BasePdfActivity)
                        }


                        else -> {}
                    }
                }
            }
        }
        BackPressedUtil().activityCreate(this@BasePdfActivity, this@BasePdfActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        BackPressedUtil().activityFinish(this@BasePdfActivity, this@BasePdfActivity)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    private fun List<AttachFileItemVo>.getAttachFilePositionUrl(key: String): String {
        if (size > 1) {
            forEach {
                if (key == it.attachFileCode) return it.attachFilePath
            }
        } else return ""
        return ""
    }

    private fun List<AttachFileItemVo>.getAttachFilePositionName(key: String): String {
        if (size > 1) {
            forEach {
                if (key == it.attachFileCode) return it.codeName
            }
        } else return ""
        return ""
    }


    private fun RecyclerView.anchorSmoothScrollToPosition(position: Int, anchorPosition: Int) {
        Log.v("anchorPosition : ", "$anchorPosition")
        layoutManager?.apply {
            when (this) {
                is LinearLayoutManager -> {
                    val bottomItem = findLastVisibleItemPosition()
                    val distance = bottomItem - position
                    val anchorItem = when {
                        distance > anchorPosition -> position + anchorPosition
                        distance < -anchorPosition -> position - anchorPosition
                        else -> {
                            bottomItem
                        }
                    }
                    if (anchorItem != bottomItem) scrollToPosition(anchorItem)
                    post {
                        smoothScrollToPosition(position)
                    }
                }

                else -> smoothScrollToPosition(position)
            }
        }
    }

    // 파일 다운로드 진행 receiver
    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerDownloadReceiver(downloadId: Long) {
        if (!isReceiverRegistered) {
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
                                    coroutineScope.launch {
                                        delay(700)
                                        Toast.makeText(context, "다운로드 완료", Toast.LENGTH_SHORT).show()
                                        binding.loadingView.visibility = View.GONE
                                    }

                                } else if (status == DownloadManager.STATUS_FAILED) {
                                    binding.loadingView.visibility = View.GONE
                                    Toast.makeText(context, "다운로드에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    binding.loadingView.visibility = View.VISIBLE
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
            } else {
                registerReceiver(receiver, filter)
            }
            isReceiverRegistered = true
        }
    }

    companion object {
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        fun getBasePdfIntent(
            context: Context,
            pdfUrl: String,
            title: String,
            viewType: String
        ): Intent {
            val intent = Intent(context, BasePdfActivity::class.java)
            intent.putExtra("pdfUrl", pdfUrl)
            intent.putExtra("title", title)
            intent.putExtra("viewType", viewType)
            return intent
        }

        fun getPurchaseBtPdfIntent(
            context: Context,
            pdfUrl: String,
            title: String,
            detailDefaultVo: PortfolioDetailDefaultVo,
            stockVo: PortfolioStockItemVo,
            attachFileItemVo: List<AttachFileItemVo>,
            viewType: String
        ): Intent {
            val intent = Intent(context, BasePdfActivity::class.java)
            intent.putExtra("pdfUrl", pdfUrl)
            intent.putExtra("title", title)
            intent.putExtra("detailDefaultVo", detailDefaultVo)
            intent.putExtra("stockVo", stockVo)
            intent.putParcelableArrayListExtra("attachFileItemVo", ArrayList(attachFileItemVo))
            intent.putExtra("viewType", viewType)
            return intent
        }

        // 소유증서
        fun getDeedIntent(
            context: Context,
            viewType: String,
            pdfUrl: String,
            title: String,
            purchaseId: String
        ): Intent {
            val intent = Intent(context, BasePdfActivity::class.java)
            intent.putExtra("viewType", viewType)
            intent.putExtra("pdfUrl", pdfUrl)
            intent.putExtra("purchaseId", purchaseId)
            intent.putExtra("title", title)
            return intent
        }

        // 투자 설명서
        fun getPurchaseManualPdfIntent(
            context: Context,
            title: String,
            detailDefaultVo: PortfolioDetailDefaultVo,
            stockVo: PortfolioStockItemVo,
            attachFileItemVo: List<AttachFileItemVo>,
            viewType: String
        ): Intent {
            val intent = Intent(context, PurchaseManualPdfActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("detailDefaultVo", detailDefaultVo)
            intent.putExtra("stockVo", stockVo)
            intent.putParcelableArrayListExtra("attachFileItemVo", ArrayList(attachFileItemVo))
            intent.putExtra("viewType", viewType)
            return intent
        }


        // 투자 계약서
        fun getPurchaseIntent(
            context: Context,
            title: String,
            detailDefaultVo: PortfolioDetailDefaultVo,
            stockVo: PortfolioStockItemVo,
            attachFileItemVo: List<AttachFileItemVo>,
            viewType: String
        ): Intent {
            val intent = Intent(context, BasePdfActivity::class.java)
            intent.putExtra("title", title)
            intent.putExtra("detailDefaultVo", detailDefaultVo)
            intent.putExtra("stockVo", stockVo)
            intent.putParcelableArrayListExtra("attachFileItemVo", ArrayList(attachFileItemVo))
            intent.putExtra("viewType", viewType)
            return intent
        }


        // 구매 입력 화면
        fun getPurchaseRenewalActivity(
            context: Context,
            detailDefaultVo: PortfolioDetailDefaultVo,
            stockVo: PortfolioStockItemVo,
            attachFileItemVo: List<AttachFileItemVo>,
            attachFileCode: String
        ): Intent {
            val intent = Intent(context, PurchaseRenewalActivity::class.java)
            intent.putExtra("detailDefaultVo", detailDefaultVo)
            intent.putExtra("stockVo", stockVo)
            intent.putParcelableArrayListExtra("attachFileItemVo", ArrayList(attachFileItemVo))
            intent.putExtra("attachFileCode", attachFileCode)
            return intent
        }
    }
}
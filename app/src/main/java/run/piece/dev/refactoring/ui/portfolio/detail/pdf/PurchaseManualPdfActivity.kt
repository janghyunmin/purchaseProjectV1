package run.piece.dev.refactoring.ui.portfolio.detail.pdf

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import run.piece.dev.R
import run.piece.dev.databinding.ActivityPurchaseManualPdfBinding
import run.piece.dev.refactoring.base.BaseActivity
import run.piece.dev.refactoring.base.BasePdfActivity
import run.piece.dev.refactoring.ui.deed.DeedPdfViewModel
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo


@AndroidEntryPoint
class PurchaseManualPdfActivity: BaseActivity<ActivityPurchaseManualPdfBinding, DeedPdfViewModel>(R.layout.activity_purchase_manual_pdf) {
    override fun getViewModelClass(): Class<DeedPdfViewModel> = DeedPdfViewModel::class.java
    private lateinit var coroutineScope: CoroutineScope
    private var isReceiverRegistered = false // File Download register

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        setNaviBarIconColor(true)
        setNaviBarBgColor("#ffffff") // 네비게이션 배경색

        coroutineScope = lifecycleScope
        coroutineScope.launch {
            with(binding) {
                activity = this@PurchaseManualPdfActivity

                viewModel.title?.let { titleTv.text = it }

                viewModel.viewType?.let { type ->
                    if(type == "PortfolioPurchaseContract") {
                        BackPressedUtil().activityCreate(this@PurchaseManualPdfActivity, this@PurchaseManualPdfActivity)
                    } else {
                        BackPressedUtil().activityClear(this@PurchaseManualPdfActivity, this@PurchaseManualPdfActivity)
                    }

                    if (viewModel.attachFileVo.isNullOrEmpty()) {
                        binding.loadingView.visibility = View.VISIBLE
                    } else {
                        viewModel.attachFileVo?.let { attachList ->
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

                                    Log.e("테스트_onPageChanged", "$currentPage, $totalPage")

                                    if (currentPage == totalPage - 1) { // 마지막 보여짐... ex) 53 == 54 -1
                                        Glide.with(this@PurchaseManualPdfActivity).load(R.drawable.ic_x16_check_white).into(binding.btnIv)
                                        purchaseBtn.background = ContextCompat.getDrawable(this@PurchaseManualPdfActivity, R.drawable.layout_round_10cfc9)
                                        btnTitle.apply {
                                            text = getString(R.string.scroll_btn_txt_next)
                                            setTextColor(ContextCompat.getColor(this@PurchaseManualPdfActivity, R.color.white))

                                            purchaseBtn.onThrottleClick {
                                                viewModel.detailDefaultVo?.let { detailDefaultVo ->
                                                    viewModel.stockVo?.run {
                                                        viewModel.attachFileVo?.let { attachFiles ->
                                                            startActivity(
                                                                BasePdfActivity.getPurchaseIntent(
                                                                    context = this@PurchaseManualPdfActivity,
                                                                    title = getString(R.string.purchase_contract_txt),
                                                                    detailDefaultVo = detailDefaultVo,
                                                                    stockVo = this,
                                                                    attachFileItemVo = attachFiles,
                                                                    viewType = "PortfolioManual"
                                                                )
                                                            )
                                                            BackPressedUtil().activityFinish(this@PurchaseManualPdfActivity, this@PurchaseManualPdfActivity)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        Glide.with(this@PurchaseManualPdfActivity).load(R.drawable.ic_x16_arrow_down_c10cfc9).into(btnIv)
                                        purchaseBtn.background = ContextCompat.getDrawable(this@PurchaseManualPdfActivity, R.drawable.layout_border_10cfc9)
                                        btnTitle.apply {
                                            text = getString(R.string.scroll_btn_txt)
                                            setTextColor(ContextCompat.getColor(this@PurchaseManualPdfActivity, R.color.c_10cfc9))
                                        }

                                        purchaseBtn.onThrottleClick {
                                            if (totalPage >= 20) {
                                                pdfView.recyclerView.anchorSmoothScrollToPosition(totalPage, 10)
                                            } else {
                                                pdfView.recyclerView.smoothScrollToPosition(totalPage -1)
                                            }
                                        }
                                    }
                                }

                                override fun onError(error: Throwable) {
                                    super.onError(error)
                                    loadingView.visibility = View.GONE
                                }
                            }

                            shareLayout.visibility = View.VISIBLE
                            shareLayout.onThrottleClick {
                                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                val fileUri = Uri.parse(attachList.getAttachFilePositionUrl("PAF0202"))
                                val request = DownloadManager.Request(fileUri)

                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${attachList.getAttachFilePositionName("PAF0202")}.pdf") // 확장자 명시

                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // 앱 상단에 다운로드 표시
                                request.setAllowedOverMetered(true)
                                request.setAllowedOverRoaming(true)
                                request.setDescription("파일 다운로드 진행중입니다.") // 다운로드 중 표시되는 내용
                                request.setNotificationVisibility(1) // 앱 상단에 다운로드 상태 표시
                                request.setMimeType("application/pdf") // MIME 유형 설정

                                binding.loadingView.visibility = View.VISIBLE

                                val downloadId = downloadManager.enqueue(request)
                                registerDownloadReceiver(downloadId)
                            }



                            // 투자 설명서
                            pdfView.initWithUrl(
                                url = Uri.parse(attachList.getAttachFilePositionUrl("PAF0202")).toString(),
                                lifecycleCoroutineScope = lifecycleScope,
                                lifecycle = lifecycle
                            )


                            backLayout.onThrottleClick {
                                BackPressedUtil().activityFinish(this@PurchaseManualPdfActivity,this@PurchaseManualPdfActivity)
                            }
                        }
                    }
                }
            }
        }

        BackPressedUtil().systemBackPressed(this@PurchaseManualPdfActivity, this@PurchaseManualPdfActivity)
    }

    private fun setNaviBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.setSystemBarsAppearance(
                    if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.systemUiVisibility =
                if (isBlack) {
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    window.decorView.systemUiVisibility
                }

        }
    }

    private fun setNaviBarBgColor(colorHexValue: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor(colorHexValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        BackPressedUtil().activityFinish(this@PurchaseManualPdfActivity, this@PurchaseManualPdfActivity)
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
                        else -> { bottomItem }
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
}
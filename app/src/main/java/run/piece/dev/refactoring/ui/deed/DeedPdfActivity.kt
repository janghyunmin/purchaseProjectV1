package run.piece.dev.refactoring.ui.deed

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
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.rajat.pdfviewer.PdfRendererView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityDeedPdfBinding
import run.piece.dev.refactoring.base.BaseActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick

@AndroidEntryPoint
class DeedPdfActivity : BaseActivity<ActivityDeedPdfBinding, DeedPdfViewModel>(R.layout.activity_deed_pdf) {
    override fun getViewModelClass(): Class<DeedPdfViewModel> = DeedPdfViewModel::class.java
    private var isReceiverRegistered = false // File Download register

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        App()

        binding.apply {
            activity = this@DeedPdfActivity

            backLayout.onThrottleClick {
                BackPressedUtil().activityFinish(this@DeedPdfActivity,this@DeedPdfActivity)
            }

            intent?.let {
                it.getStringExtra("title")?.let { title ->
                    titleTv.text = title
                }

                it.getStringExtra("attachFileCode")?.let { attachFileCode ->
                    if(attachFileCode == "PAF0202") {
                        shareLayout.visibility = View.VISIBLE
                        shareLayout.onThrottleClick {
                            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val fileUri = Uri.parse(intent.getStringExtra("pdfUrl"))
                            val request = DownloadManager.Request(fileUri)
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${intent.getStringExtra("title")}.pdf") // 확장자 명시
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
                    } else {
                        shareLayout.visibility = View.GONE
                    }
                }
            }

            intent?.let {
                it.getStringExtra("pdfUrl")?.let { url ->
                    pdfRendererView.statusListener = object  : PdfRendererView.StatusCallBack {
                        override fun onPdfLoadProgress(progress: Int, downloadedBytes: Long, totalBytes: Long?) {
                            super.onPdfLoadProgress(progress, downloadedBytes, totalBytes)
                            loadingView.visibility = View.VISIBLE
                        }

                        override fun onPdfLoadStart() {
                            super.onPdfLoadStart()
                        }

                        override fun onPdfLoadSuccess(absolutePath: String) {
                            super.onPdfLoadSuccess(absolutePath)
                            loadingView.visibility = View.GONE
                            pdfRendererView.post {
                                pdfRendererView.recyclerView.scrollToPosition(0)
                            }
                        }
                    }
                    pdfRendererView.initWithUrl(
                        url = url,
                        lifecycleCoroutineScope = lifecycleScope,
                        lifecycle = lifecycle
                    )
                }
            }
        }

        BackPressedUtil().activityCreate(this@DeedPdfActivity,this@DeedPdfActivity)
        BackPressedUtil().systemBackPressed(this@DeedPdfActivity,this@DeedPdfActivity)
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
                                    CoroutineScope(Dispatchers.Main).launch {
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
        fun getDeedIntent(context: Context, pdfUrl: String, title: String, purchaseId: String): Intent {
            val intent = Intent(context, DeedPdfActivity::class.java)
            intent.putExtra("pdfUrl",pdfUrl)
            intent.putExtra("purchaseId",purchaseId)
            intent.putExtra("title", title)
            return intent
        }

        fun getIntent(context: Context, pdfUrl: String, title: String, attachFileCode: String): Intent {
            val intent = Intent(context, DeedPdfActivity::class.java)
            intent.putExtra("pdfUrl",pdfUrl)
            intent.putExtra("title",title)
            intent.putExtra("attachFileCode",attachFileCode)
            return intent
        }
    }
}
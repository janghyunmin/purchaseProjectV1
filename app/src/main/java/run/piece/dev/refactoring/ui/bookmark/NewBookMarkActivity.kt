package run.piece.dev.refactoring.ui.bookmark

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import run.piece.dev.App
import run.piece.dev.databinding.ActivityNewBookmarkBinding
import run.piece.dev.refactoring.ui.magazine.NewMagazineDetailWebViewActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.NewVibratorUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo

@AndroidEntryPoint
class NewBookMarkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewBookmarkBinding
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var bookMarkRvAdapter: NewBookMarkRvAdapter
    private var lm: RecyclerView.LayoutManager? = null
    private val viewModel: NewBookMarkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        binding = ActivityNewBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val networkConnection = NetworkConnection(this@NewBookMarkActivity)
        networkConnection.observe(this@NewBookMarkActivity) { isConnected ->
            if (!isConnected) startActivity(getNetworkActivity(this@NewBookMarkActivity))
        }

        binding.apply {
            activity = this@NewBookMarkActivity
            lifecycleOwner = this@NewBookMarkActivity
            vm = viewModel

            lm = LinearLayoutManager(this@NewBookMarkActivity, LinearLayoutManager.VERTICAL, false)
            bookMarkRvAdapter = NewBookMarkRvAdapter(this@NewBookMarkActivity, viewModel)
            binding.bookmarkRv.adapter = bookMarkRvAdapter
        }

        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_SECURE) //캡처 방지
            //상태바 아이콘(true: 검정 / false: 흰색)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        coroutineScope = lifecycleScope
        coroutineScope.launch(Dispatchers.IO) {
            viewModel.getBookMark()
        }

        magazineDataInit()

        binding.backImgIv.onThrottleClick {
            BackPressedUtil().activityFinish(this@NewBookMarkActivity, this@NewBookMarkActivity)
        }

        binding.magazineGoBtn.onThrottleClick {
            BackPressedUtil().activityFinish(this@NewBookMarkActivity, this@NewBookMarkActivity)
        }

        bookMarkRvAdapter.setOnItemClickListener(object : NewBookMarkRvAdapter.OnItemClickListener {
            @SuppressLint("NotifyDataSetChanged")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(v: View, tag: String, position: Int, isFavorite: String, data: BookMarkItemVo) {
                when (tag) {
                    "webView" -> {
                        startActivity(
                            getMagazineDetailActivity(
                                this@NewBookMarkActivity,
                                data.magazineId,
                                data.isFavorite,
                                position
                            )
                        )
                    }

                    "bookMark" -> {
                        NewVibratorUtil().run {
                            init(this@NewBookMarkActivity)
                            oneShot(100, 100)
                        }

                        // 북마크 삭제시 필요 Model
                        val bookMkRemove = MemberBookmarkRemoveModel(memberId = viewModel.memberId, magazineId = data.magazineId)

                        coroutineScope.launch {
                            withContext(this.coroutineContext) {
                                deleteBookMarkIO(bookMkRemove = bookMkRemove).join()
                            }

                            withContext(this.coroutineContext) {
                                getBookMarkIO().join()
                            }
                        }
                    }
                }
            }
        })

        BackPressedUtil().activityCreate(this@NewBookMarkActivity, this@NewBookMarkActivity)
        BackPressedUtil().systemBackPressed(this@NewBookMarkActivity, this@NewBookMarkActivity)

    }

    override fun onResume() {
        super.onResume()
        viewModel.getBookMark()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    suspend fun deleteBookMarkIO(bookMkRemove: MemberBookmarkRemoveModel) = coroutineScope.launch {
        delay(100)
        viewModel.deleteBookMark(bookMkRemove)
    }

    suspend fun getBookMarkIO() = coroutineScope.launch {
        delay(100)
        viewModel.getBookMark()
    }

    private fun bookMarkRvInit(bookMarkList: List<BookMarkItemVo>) {
        binding.loading.visibility = View.GONE
        binding.bookmarkRv.itemAnimator = null
        binding.bookmarkRv.layoutManager = lm
        bookMarkRvAdapter.submitList(bookMarkList)
    }

    private fun magazineDataInit() {
        viewModel.viewModelScope.launch {
            viewModel.bookMarkList.collect { vo ->
                when (vo) {
                    is NewBookMarkViewModel.BookMarkState.IsLoading -> {
                        binding.loading.visibility = View.VISIBLE
                    }

                    is NewBookMarkViewModel.BookMarkState.Success -> {
                        binding.loading.visibility = View.GONE

                        // 리스트 반환값이 0 or [] 일 경우
                        if (vo.bookMarkList.isEmpty()) {
                            binding.emptyGroup.visibility = View.VISIBLE
                            binding.bookmarkRv.visibility = View.GONE
                        }

                        // 북마크 리스트 반환값이 있을 경우
                        else {
                            binding.emptyGroup.visibility = View.GONE
                            binding.bookmarkRv.visibility = View.VISIBLE

                            bookMarkRvInit(vo.bookMarkList)
                        }

                    }

                    is NewBookMarkViewModel.BookMarkState.Failure -> {
                        binding.loading.visibility = View.GONE
                    }

                    else -> {
                        binding.loading.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    companion object {
        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        fun getMagazineDetailActivity(context: Context, magazineId: String, isFavorite: String, position: Int): Intent {
            val intent = Intent(context, NewMagazineDetailWebViewActivity::class.java)
            intent.putExtra("magazineId", magazineId)
            intent.putExtra("isFavorite", isFavorite)
            intent.putExtra("pos", position)
            return intent
        }
    }
}
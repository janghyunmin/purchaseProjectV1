package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.ActivityPortfolioDetailAssetBinding
import run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo.PortfolioObjectInfoActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.NetworkActivity
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo

@AndroidEntryPoint
class PortfolioDetailAssetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPortfolioDetailAssetBinding
    private val viewModel: PortfolioDetailAssetViewModel by viewModels()

    private val smoothScroller: RecyclerView.SmoothScroller by lazy {
        object : LinearSmoothScroller(this) {
            override fun getHorizontalSnapPreference() = SNAP_TO_START
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioDetailAssetBinding.inflate(layoutInflater)
        binding.activity = this
        binding.viewModel = viewModel

        setContentView(binding.root)

        window.apply {
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            addFlags(WindowManager.LayoutParams.FLAG_SECURE) //캡처 방지
        }

        binding.apply {
            CoroutineScope(Dispatchers.Main).launch {
                this@PortfolioDetailAssetActivity.viewModel.portfolioProduct.collect {
                    when(it) {
                        is PortfolioProductState.Success -> {

                            if (it.portfolioProductVo.isEmpty()) detailAssetRv.visibility = View.GONE
                            else {
                                val detailAssetRvAdapter = DetailAssetRvAdapter(this@PortfolioDetailAssetActivity, this@PortfolioDetailAssetActivity.viewModel)

                                val decoration = DetailAssetRvDecoration(this@PortfolioDetailAssetActivity.viewModel.dpToPixel(12), it.portfolioProductVo.size)

                                detailAssetRv.apply {
                                    layoutManager = LinearLayoutManager(this@PortfolioDetailAssetActivity, LinearLayoutManager.HORIZONTAL, false)
                                    adapter = detailAssetRvAdapter
                                    addItemDecoration(decoration)
                                }

                                detailAssetRvAdapter.submitList(it.portfolioProductVo)

                                it.portfolioProductVo.forEachIndexed { index, vo ->
                                    if (vo.productId == this@PortfolioDetailAssetActivity.viewModel.productId) {
                                        changeFragment(vo, index) //프래그먼트를 만든다, 포커싱을 이동한다
                                        detailAssetRvAdapter.selectToPosition(index) //클릭 상태로 만든다
                                    }
                                }
                            }
                        }
                        is PortfolioProductState.Failure -> {
                            startActivity(Intent(this@PortfolioDetailAssetActivity, NetworkActivity::class.java))
                        }
                        else -> {}
                    }
                }
            }
        }

        viewModel.getPortfolioProduct()

        binding.backLayout.onThrottleClick {
            BackPressedUtil().activityFinish(this@PortfolioDetailAssetActivity,this@PortfolioDetailAssetActivity)
        }

        BackPressedUtil().activityCreate(this@PortfolioDetailAssetActivity,this@PortfolioDetailAssetActivity)
        BackPressedUtil().systemBackPressed(this@PortfolioDetailAssetActivity,this@PortfolioDetailAssetActivity)
    }

    fun changeFragment(vo: PortfolioProductVo, index: Int) {
        this@PortfolioDetailAssetActivity.viewModel.recruitmentState?.let { recruitmentState ->
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.item_fragment, PortfolioDetailAssetFragment.newInstance(vo, recruitmentState))
                .commit()

            clickedItemPosition(index)
        }
    }
    private fun clickedItemPosition(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            smoothScroller.targetPosition = position
            binding.detailAssetRv.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    fun getScrollView() = binding.scrollView

    fun goObjectInfo() {
        startActivity(PortfolioObjectInfoActivity.getIntent(this))
    }

    companion object {
        fun getIntent(context: Context, recruitmentState: String, portfolioId: String, productId: String): Intent {
            val intent = Intent(context, PortfolioDetailAssetActivity::class.java)
            intent.putExtra("recruitmentState", recruitmentState)
            intent.putExtra("portfolioId", portfolioId)
            intent.putExtra("productId", productId)
            return intent
        }
    }
}
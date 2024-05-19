package run.piece.dev.refactoring.ui.portfolio.detail.assets

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentAssetsBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toInt
import run.piece.dev.refactoring.widget.custom.chart.PercentBarChart
import run.piece.domain.refactoring.portfolio.model.ProductCompositionItemVo

@AndroidEntryPoint
class AssetsFragment: Fragment(R.layout.fragment_assets) {
    private var _binding: FragmentAssetsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AssetsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAssetsBinding.bind(view)
        _binding?.lifecycleOwner = this

        binding.apply {

            arguments?.let { bundle ->
                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList("data", ProductCompositionItemVo::class.java)
                } else {
                    bundle.getParcelableArrayList("data")
                }

                data?.let {
                    if (it.size == 0) toolTipLayout.visibility = View.GONE
                    else {
                        toolTipLayout.visibility = View.VISIBLE
                        val set = AnimatorInflater.loadAnimator(requireActivity(), R.animator.anim_pumping) as AnimatorSet
                        set.setTarget(toolTipLayout)
                        set.start()
                    }

                    // 32 바깥 margin (좌 16 우 16), 48 이너 margin (좌 24 우 24)
                    val width = viewModel.getDeviceWidth() - viewModel.dpToPixel(32 + 48).toInt()
                    val height = viewModel.dpToPixel(24).toInt()

                    it.sortByDescending { data ->
                        data.rate.toInt()
                    }

                    it.forEachIndexed { index, itemVo ->
                        if (itemVo.rate.toInt() == 0) {
                            itemVo.colorId = R.color.g100_F9F9F9
                        } else {
                            when(index) {
                                0 -> { itemVo.colorId = R.color.p500_10CFC9 }
                                1 -> { itemVo.colorId = R.color.p600_0CB2AD }
                                2 -> { itemVo.colorId = R.color.s700_0B589E }
                                3 -> { itemVo.colorId = R.color.s600_0F77D4 }
                                4 -> { itemVo.colorId = R.color.s500_1C93FF }
                                5 -> { itemVo.colorId = R.color.s400_49A9FF }
                                6 -> { itemVo.colorId = R.color.percent_chart_color_8E75FF }
                                7 -> { itemVo.colorId = R.color.pie_chart_color_A591FF }
                                8 -> { itemVo.colorId = R.color.g600_8C919F }
                                9 -> { itemVo.colorId = R.color.g500_B8BCC8 }
                                else -> { itemVo.colorId = R.color.g500_B8BCC8 }
                            }
                        }

                    }

                    val chartView = PercentBarChart.newInstance(requireActivity(), height,  width,
                        viewModel.dpToPixel(8), it)

                    val assetsRvAdapter = AssetsRvAdapter(requireActivity(), this@AssetsFragment.viewModel)

                    val recyclerView = RecyclerView(requireActivity())

                    recyclerView.apply {
                        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                        adapter = assetsRvAdapter
                    }

                    assetsRvAdapter.submitList(it)

                    val imageView = ImageView(requireActivity())
                    imageView.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_talk_box_tip))

                    cardInnerLayout.addView(chartView)
                    cardInnerLayout.addView(recyclerView)

                    val chartViewParam = chartView.layoutParams as ConstraintLayout.LayoutParams
                    chartViewParam.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    chartViewParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    chartViewParam.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    chartViewParam.topMargin = viewModel.dpToPixel(24).toInt()
                    chartView.id = ViewCompat.generateViewId()
                    chartView.layoutParams = chartViewParam

                    val margin = viewModel.dpToPixel(16)

                    val recyclerViewParam = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
                    recyclerViewParam.topToBottom = chartView.id
                    recyclerViewParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    recyclerViewParam.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    recyclerViewParam.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    recyclerViewParam.setMargins(margin, margin, margin, viewModel.dpToPixel(12).toInt())
                    recyclerView.layoutParams = recyclerViewParam

                    closeIvLayout.onThrottleClick {
                        toolTipLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(recruitmentState: String,
                        portfolioId: String,
                        data: List<ProductCompositionItemVo>) : AssetsFragment {

            val fragment = AssetsFragment()

            val bundle = Bundle().apply {
                putString("recruitmentState", recruitmentState)
                putString("portfolioId", portfolioId)
                putParcelableArrayList("data", data as ArrayList)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
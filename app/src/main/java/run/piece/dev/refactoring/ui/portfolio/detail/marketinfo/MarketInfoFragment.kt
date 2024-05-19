package run.piece.dev.refactoring.ui.portfolio.detail.marketinfo

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.FragmentMarketInfoBinding
import run.piece.dev.refactoring.widget.custom.chart.DayValueBarChart
import run.piece.domain.refactoring.portfolio.model.PortfolioMarketInfoVo

@AndroidEntryPoint
class MarketInfoFragment: Fragment(R.layout.fragment_market_info) {
    private var _binding: FragmentMarketInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MarketInfoViewModel by viewModels()

    private val smoothScroller: RecyclerView.SmoothScroller by lazy {
        object : LinearSmoothScroller(context) {
            override fun getHorizontalSnapPreference() = SNAP_TO_START
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMarketInfoBinding.bind(view)
        _binding?.lifecycleOwner = this

        binding.apply {
            arguments?.let {
                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableArrayList("data", PortfolioMarketInfoVo::class.java)
                } else {
                    it.getParcelableArrayList("data")
                }

                data?.let { items ->
                    chartCardView.layoutParams.width = viewModel.getDeviceWidth() - viewModel.dpToPixel(32)
                    chartCardView.layoutParams.height = viewModel.dpToPixel(216)

                    val marketChartRvAdapter = MarketChartRvAdapter(requireActivity(), this@MarketInfoFragment, chartCardView)

                    chartRv.apply {
                        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
                        adapter = marketChartRvAdapter

                        addItemDecoration(MarketChartRvDecoration(viewModel.dpToPixel(16), viewModel.dpToPixel(8), data.size))
                    }

                    if (items.isNotEmpty()) {
                        if (items[0].marketPrices.isNotEmpty()) {
                            items[0].isClicked = true
                            resources.displayMetrics?.widthPixels?.let { width ->
                                DayValueBarChart.newInstance(chartCardView, items[0].marketPrices, width)
                            }
                        }
                        marketChartRvAdapter.submitList(items)
                    }
                }
            }
        }
    }

    fun clickedItemPosition(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            smoothScroller.targetPosition = position
            binding.chartRv.layoutManager?.startSmoothScroll(smoothScroller)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(data: List<PortfolioMarketInfoVo>) : MarketInfoFragment {
            val fragment = MarketInfoFragment()
            val bundle = Bundle().apply {
                putParcelableArrayList("data", data as ArrayList)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
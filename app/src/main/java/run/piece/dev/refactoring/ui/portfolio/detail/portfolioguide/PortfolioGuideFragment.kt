package run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentPortfolioGuideBinding
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.domain.refactoring.portfolio.model.PortfolioBoardVo

@AndroidEntryPoint
class PortfolioGuideFragment : Fragment(R.layout.fragment_portfolio_guide) {
    private var _binding: FragmentPortfolioGuideBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PortfolioGuideViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPortfolioGuideBinding.bind(view)
        _binding?.lifecycleOwner = this

        binding.apply {
            arguments?.let { bundle ->
                // SDK 버전에 따라 다르게 데이터 받기
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arguments?.getParcelable<PortfolioBoardVo>("data")
                } else {
                    arguments?.getParcelable<PortfolioBoardVo>("data")
                }

                val itemList = ArrayList<GuideItem>()

                var oneItem = GuideItem(
                    R.drawable.ic_investment_disclosure,
                    "공시",
                    "주요 공시를 안내해 드릴게요.",
                    null,
                    false
                )


                data?.let { vo ->
                    vo.let {
                        oneItem = GuideItem(
                            R.drawable.ic_investment_disclosure,
                            "투자공시",
                            "주요 공시를 안내해 드릴게요.",
                            data,
                            false
                        )
                    }
                }

                val twoItem = GuideItem(
                    R.drawable.ic_investment_risk_warning,
                    "투자위험",
                    "투자에 신중을 기하여 주시기 바라며, \n" + "핵심위험들에 대한 상세설명을 꼭 참고해 주세요.", null, false
                )

                val threeItem = GuideItem(
                    R.drawable.ic_customer_protection,
                    "고객보호",
                    "회사는 금융당국의 감독하에\n" + "다음과 같이 고객을 보호하고 있어요.", null,
                    false
                )

                val fourItem = GuideItem(
                    R.drawable.ic_investment_contract_securities_faq,
                    "투자계약증권 FAQ",
                    "투자계약증권에 대해 \n" + "자세히 알려드릴게요.",
                    null,
                    false
                )

                itemList.add(oneItem)
                itemList.add(twoItem)
                itemList.add(threeItem)
                itemList.add(fourItem)

                guideRv.setHasFixedSize(true)
                guideRv.layoutManager = LinearLayoutManager(requireActivity())

                val guideRvAdapter = PortfolioGuideRvAdapter(requireActivity(), bundle.getString("portfolioId").default())

                guideRv.adapter = guideRvAdapter

                guideRvAdapter.submitList(itemList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(portfolioId: String) : PortfolioGuideFragment {
            val fragment = PortfolioGuideFragment()
            val bundle = Bundle().apply {
                putString("portfolioId",portfolioId)
//                putParcelableArrayList("data", data as ArrayList)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
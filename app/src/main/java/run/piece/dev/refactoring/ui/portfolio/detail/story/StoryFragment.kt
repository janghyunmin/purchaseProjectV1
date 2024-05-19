package run.piece.dev.refactoring.ui.portfolio.detail.story

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentStoryBinding
import run.piece.dev.refactoring.base.html.BaseWebViewActivity
import run.piece.dev.refactoring.base.html.WebViewRouter
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.portfolio.model.PortfolioStoryVo
import run.piece.domain.refactoring.portfolio.model.PurchaseGuideItemVo

@AndroidEntryPoint
class StoryFragment: Fragment(R.layout.fragment_story) {
    private var _binding: FragmentStoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StoryViewModel by viewModels()

    private var isStoryHide = false
    private var isListHide = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStoryBinding.bind(view)
        _binding?.lifecycleOwner = this
        _binding?.viewModel = viewModel

        binding.apply {
            arguments?.let { bundle ->
                val guideData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList("guideData", PurchaseGuideItemVo::class.java)
                } else {
                    bundle.getParcelableArrayList("guideData")
                }

                guideData?.let {
                    val storyRvAdapter = StoryRvAdapter(this@StoryFragment.viewModel)

                    FlexboxLayoutManager(requireActivity()).apply {
                        flexWrap = FlexWrap.WRAP
                        flexDirection = FlexDirection.ROW
                        justifyContent = JustifyContent.FLEX_START

                    }.let {
                        binding.cardItemRv.layoutManager = it
                        binding.cardItemRv.adapter = storyRvAdapter
                    }
                    storyRvAdapter.submitList(it)
                } ?: run {
                    isListHide = true
                    chatLayout.visibility = View.GONE
                }

                val storyData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable("storyData", PortfolioStoryVo::class.java)
                } else {
                    bundle.getParcelable("storyData")
                }

                storyData?.let { data ->
                    if (data.title.isEmpty() && data.subTitle.isEmpty() && data.storyImagePath.isEmpty()) {
                        isStoryHide = true
                        storyIvCard.visibility = View.GONE

                    } else {
                        titleTv.text = data.title
                        subTitleTv.text = data.subTitle
                        Glide.with(requireActivity()).load(data.storyImagePath).into(storyIv)

                        storyIv.onThrottleClick(
                            {
                                it.context.startActivity(
                                    BaseWebViewActivity.commonIntent(
                                        requireContext(),
                                        WebViewRouter.STORY.viewName,
                                        "스토리",
                                        data.contents
                                    )
                                )
                            }, 1000)
                    }
                }
                if (isListHide && isStoryHide) sectionTitleTv.visibility = View.GONE
            }

            if (this@StoryFragment.viewModel.getDeviceWidth() < 980) {
                investPointTv.textSize = 18F
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(storyData: PortfolioStoryVo, guideData: List<PurchaseGuideItemVo>) : StoryFragment {
            val fragment = StoryFragment()
            val bundle = Bundle().apply {
                putParcelableArrayList("guideData", guideData as ArrayList)
                putParcelable("storyData", storyData)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
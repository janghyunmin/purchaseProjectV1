package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.base.BaseFragment
import run.piece.dev.databinding.FragmentDisclosureBinding
import run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide.disclosure.DisclosureTabViewModel
import run.piece.dev.refactoring.utils.FragmentLifecycleOwner
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.domain.refactoring.board.model.FilesVo
import run.piece.domain.refactoring.board.model.ManagementDisclosureItemVo

@AndroidEntryPoint
open class DisclosureManagementFragment : BaseFragment<FragmentDisclosureBinding>(FragmentDisclosureBinding::inflate) {
    protected val visibleLifecycleOwner: FragmentLifecycleOwner by lazy {
        FragmentLifecycleOwner()
    }
    private val viewModel: DisclosureTabViewModel by viewModels()
    private var moreJob: Job? = null
    private var page: Int = 1
    private var portfolioId: String = ""

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            managementFragment = this@DisclosureManagementFragment
            viewModel = viewModel

            arguments?.let { bundle ->
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList("data", ManagementDisclosureItemVo::class.java)
                } else {
                    bundle.getParcelableArrayList("data")
                }

                portfolioId = bundle.getString("portfolioId", "")

                when(bundle.getString("searchViewStatus","N")) {
                    // 검색 결과로 들어온 경우가 아닐때
                    "N" -> {
                        binding.goBackTv.visibility = View.GONE
                        if (data?.isEmpty() == true) {
                            binding.emptyGroup.visibility = View.VISIBLE
                            binding.disclosureAllRv.visibility = View.GONE
                            binding.noDataTv.text = getString(R.string.disclosure_empty_txt)
                        } else {
                            data?.let {
                                val managementRvAdapter = ManagementRvAdapter(requireContext())
                                adapterOnClick(managementRvAdapter, data)
                                disclosureAllRv.run {
                                    adapter = managementRvAdapter
                                    this.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                                    itemAnimator = null

                                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                            super.onScrolled(recyclerView, dx, dy)

                                            // 리사이클러뷰 아이템 위치 찾기, 아이템 위치가 완전히 보일때 호출됨
                                            val rvPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()

                                            // 리사이클러뷰 아이템 총개수 (index 접근 이기 때문에 -1)
                                            val totalCount = recyclerView.adapter?.itemCount?.minus(1)

                                            // 페이징 처리
                                            if (!disclosureAllRv.canScrollVertically(1)) {
                                                if (rvPosition == totalCount) {
                                                    page++
                                                    loadMore(managementRvAdapter, data , "", page)
                                                }
                                            } else {
                                                return
                                            }
                                        }
                                    })
                                }

                                managementRvAdapter.submitList(data)
                            } ?: run {
                                binding.emptyGroup.visibility = View.VISIBLE
                                binding.disclosureAllRv.visibility = View.GONE
                                binding.goBackTv.visibility = View.GONE
                            }
                        }
                    }

                    // 검색 결과로 들어왔을때
                    "Y" -> {
                        var searchText: String = bundle.getString("searchText","")
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(200)
                            if (data?.isEmpty() == true && searchText.isNotEmpty()) {
                                binding.goBackTv.visibility = View.VISIBLE
                                binding.emptyGroup.visibility = View.VISIBLE
                                binding.noDataTv.text = "'${searchText}'에 대한 검색 결과가 없어요"
                                binding.disclosureAllRv.visibility = View.GONE
                                binding.goBackTv.onThrottleClick {
                                    activity?.finish()
                                    activity?.overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
                                }
                            }
                            else {
                                binding.goBackTv.visibility = View.GONE
                                binding.disclosureAllRv.visibility = View.VISIBLE
                                data?.let {
                                    val managementRvAdapter = ManagementRvAdapter(requireContext())
                                    adapterOnClick(managementRvAdapter, data)
                                    disclosureAllRv.run {
                                        adapter = managementRvAdapter
                                        this.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                                        itemAnimator = null

                                        addOnScrollListener(object : RecyclerView.OnScrollListener() {
                                            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                                super.onScrolled(recyclerView, dx, dy)

                                                // 리사이클러뷰 아이템 위치 찾기, 아이템 위치가 완전히 보일때 호출됨
                                                val rvPosition = (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()

                                                // 리사이클러뷰 아이템 총개수 (index 접근 이기 때문에 -1)
                                                val totalCount = recyclerView.adapter?.itemCount?.minus(1)

                                                // 페이징 처리
                                                if (!disclosureAllRv.canScrollVertically(1)) {
                                                    if (rvPosition == totalCount) {
                                                        page++
                                                        loadMore(managementRvAdapter, data , searchText, page)
                                                    }
                                                } else {
                                                    return
                                                }
                                            }
                                        })
                                    }

                                    managementRvAdapter.submitList(data)
                                } ?: run {
                                    binding.goBackTv.visibility = View.VISIBLE
                                    binding.disclosureAllRv.visibility = View.VISIBLE
                                    binding.emptyGroup.visibility = View.VISIBLE
                                    binding.noDataTv.text = "'${searchText}'에 대한 검색 결과가 없어요"
                                    binding.disclosureAllRv.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMore(rvAdapter: ManagementRvAdapter, data: List<ManagementDisclosureItemVo>, keyword: String, page: Int) {
        var start: Int = rvAdapter.itemCount
        var end: Int = start + 10
        binding.loadingIv.visibility = View.VISIBLE
        moreJob?.cancel()
        moreJob = this@DisclosureManagementFragment.viewModel.viewModelScope.launch {
            if(keyword.isEmpty()) {
                this@DisclosureManagementFragment.viewModel.getBoard("경영공시", portfolioId, "", page, page)
            } else {
                this@DisclosureManagementFragment.viewModel.getBoard("경영공시",portfolioId,keyword,page,page)
            }
            delay(300)
            viewModel.disclosureList.collect { vo ->
                when (vo) {
                    is DisclosureTabViewModel.DisclosureListState.Success -> {
                        binding.loadingIv.visibility = View.GONE
                        val baseList = rvAdapter.currentList.toMutableList()
                        vo.disclosure.managementDisclosure.disclosure.forEach { newItem ->
                            baseList.add(newItem)
                        }

                        // 어뎁터 업데이트
                        rvAdapter.submitList(baseList)
                        adapterOnClick(rvAdapter, baseList)
                    }

                    is DisclosureTabViewModel.DisclosureListState.Failure -> {
                        binding.loadingIv.visibility = View.GONE
                    }

                    else -> {}
                }
            }

        }
    }


    private fun adapterOnClick(rvAdapter: ManagementRvAdapter, data: List<ManagementDisclosureItemVo>) {
        rvAdapter.setItemClickListener(object : ManagementRvAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                if (position >= 0 && position < data.size) {
                    if(data[position].createdAt.isEmpty()) {
                        startActivity(
                            getDisclosureDetailActivity(
                                requireContext(),
                                "투자공시 상세",
                                topTitle = "경영공시",
                                title = data[position].title.default(),
                                codeName = data[position].codeName.default(),
                                boardId = data[position].boardId.default(),
                                contents = data[position].contents.default(),
                                createAt = "",
                                tabDvn = data[position].tabDvn.default(),
                                files = data[position].files.default()
                            )
                        )
                    } else {
                        startActivity(
                            getDisclosureDetailActivity(
                                requireContext(),
                                "투자공시 상세",
                                topTitle = "경영공시",
                                title = data[position].title.default(),
                                codeName = data[position].codeName.default(),
                                boardId = data[position].boardId.default(),
                                contents = data[position].contents.default(),
                                createAt = data[position].createdAt.toBaseDateFormat().default(),
                                tabDvn = data[position].tabDvn.default(),
                                files = data[position].files.default()
                            )
                        )
                    }
                } else {
                    Log.e("IndexOutOfBounds", "Position $position is out of bounds for data list with size ${data.size}")
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun newInstance(data: List<ManagementDisclosureItemVo>, portfolioId: String, searchViewStatus: String, searchText: String): DisclosureManagementFragment {
            var fragment = DisclosureManagementFragment()
            val bundle = Bundle().apply {
                putParcelableArrayList("data", data as ArrayList)
                putString("portfolioId", portfolioId)
                putString("searchViewStatus",searchViewStatus)
                putString("searchText",searchText)
            }
            fragment.arguments = bundle
            return fragment
        }

        // 공시 상세
        fun getDisclosureDetailActivity(
            context: Context,
            viewName: String,
            topTitle: String,
            title: String,
            codeName: String,
            boardId: String,
            contents: String,
            createAt: String,
            tabDvn: String,
            files: List<FilesVo?>
        ): Intent {
            val intent = Intent(context, DisclosureWebViewActivity::class.java)
            intent.putExtra("viewName", viewName)
            intent.putExtra("topTitle", topTitle)
            intent.putExtra("title", title)
            intent.putExtra("codeName", codeName)
            intent.putExtra("boardId", boardId)
            intent.putExtra("contents", contents)
            intent.putExtra("createAt", createAt)
            intent.putExtra("tabDvn", tabDvn)
            intent.putParcelableArrayListExtra("files", ArrayList(files))
            return intent
        }
    }


}
package run.piece.dev.refactoring.ui.deposit.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.base.BaseFragment
import run.piece.dev.databinding.FragmentHistoryDepositBinding
import run.piece.dev.refactoring.ui.deposit.DepositViewModel
import run.piece.dev.refactoring.ui.deposit.NewDepositHistoryActivity
import run.piece.dev.refactoring.ui.deposit.NhAccountNotiBtDlg
import run.piece.dev.refactoring.ui.deposit.adapter.DepositHistoryRvAdapter
import run.piece.dev.refactoring.utils.FragmentLifecycleOwner
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.deposit.model.HistoryItemVo


// 회원 거래 내역 입금 화면
@AndroidEntryPoint
class HistoryDepositFragment : BaseFragment<FragmentHistoryDepositBinding>(FragmentHistoryDepositBinding::inflate) {
    protected val visibleLifecycleOwner: FragmentLifecycleOwner by lazy {
        FragmentLifecycleOwner()
    }
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var activity: NewDepositHistoryActivity
    private val viewModel: DepositViewModel by viewModels()
    private var nhAccountNotiBtDlg: NhAccountNotiBtDlg? = null
    private var moreJob: Job? = null
    private var page: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            fragment = this@HistoryDepositFragment
            viewModel = viewModel

            nhAccountNotiBtDlg = NhAccountNotiBtDlg(requireContext())

            arguments?.let { bundle ->
                var vranNo = bundle.getString("vranNo","")
                if(vranNo.isEmpty()) {
                    // 가상계좌가 없음
                    createAccountTv.visibility = View.VISIBLE
                    createAccountTv.onThrottleClick {
                        nhAccountNotiBtDlg?.show(
                            activity.supportFragmentManager,
                            getString(R.string.nh_bt_sheet_btn_txt)
                        )
                        nhAccountNotiBtDlg?.setCallback(object : NhAccountNotiBtDlg.OnSendFromBottomSheetDialog {
                            override fun sendValue() {
                                LogUtil.v("가상계좌 만들기")
                                startActivity(NewDepositHistoryActivity.createNhAccount(requireContext(), vranNo))
                                nhAccountNotiBtDlg?.dismiss()
                            }
                        })
                    }
                } else {
                    // 가상계좌가 있음
                    createAccountTv.visibility = View.GONE
                }


                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList("data", HistoryItemVo::class.java)
                } else {
                    bundle.getParcelableArrayList("data")
                }

                if(data?.isEmpty() == true) {
                    historyEmptyGroup.visibility = View.VISIBLE
                    historyRv.visibility = View.GONE
                } else {
                    data?.let {
                        val historyRvAdapter = DepositHistoryRvAdapter(requireContext())
                        historyRv.run {
                            adapter = historyRvAdapter
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
                                    if (!historyRv.canScrollVertically(1)) {
                                        if (rvPosition == totalCount) {
                                            page++
                                            loadMore(historyRvAdapter)
                                        }
                                    } else {
                                        return
                                    }
                                }
                            })
                        }

                        historyRvAdapter.submitList(data)

                    } ?: run {
                        historyEmptyGroup.visibility = View.VISIBLE
                        historyRv.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = viewLifecycleOwner.lifecycleScope
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as NewDepositHistoryActivity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    private fun loadMore(rvAdapter: DepositHistoryRvAdapter) {
        var start: Int = rvAdapter.itemCount
        binding.loadingIv.visibility = View.VISIBLE
        moreJob?.cancel()
        moreJob = viewModel.viewModelScope.launch {
            launch(Dispatchers.IO) {
                this@HistoryDepositFragment.viewModel.getHistory("v0.0.2","DEPOSIT", page = page)
            }

            launch(Dispatchers.Main) {
                delay(300)
                viewModel.historyList.collect { vo ->
                    when(vo) {
                        is DepositViewModel.DepositHistoryState.Success -> {
                            delay(200)
                            binding.loadingIv.visibility = View.GONE
                            val updateList = rvAdapter.currentList.toMutableList()
                            vo.data.forEach { newItem ->
                                updateList.add(newItem)
                            }

                            rvAdapter.submitList(updateList)
                        }
                        is DepositViewModel.DepositHistoryState.Failure -> {
                            binding.loadingIv.visibility = View.GONE
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(title: String, data: List<HistoryItemVo>, vranNo: String): HistoryDepositFragment {
            var fragment = HistoryDepositFragment()
            val bundle = Bundle().apply {
                bundleOf("title" to title)
                putParcelableArrayList("data", data as ArrayList)
                putString("vranNo",vranNo)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
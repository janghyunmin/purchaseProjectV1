package run.piece.dev.refactoring.ui.magazine

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.NewFragmentMagazineBinding
import run.piece.dev.refactoring.ui.bookmark.NewBookMarkActivity
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.main.MainViewModel
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.NewVibratorUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.LoginChkActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineTypeVo

@AndroidEntryPoint
class NewFragmentMagazine : Fragment(R.layout.new_fragment_magazine) {
    private val magazineLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            viewModel.requestPage = 1
            if (viewModel.isLogin.isEmpty()) this@NewFragmentMagazine.viewModel.getNotMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = true)
            else {
                this@NewFragmentMagazine.viewModel.getMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = true)
                this@NewFragmentMagazine.viewModel.getBookMark()
            }
        }
    }

    private var _binding: NewFragmentMagazineBinding? = null
    private val binding get() = _binding ?: NewFragmentMagazineBinding.inflate(layoutInflater).also { _binding = it }
    private val viewModel by viewModels<NewMagazineViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()

    private lateinit var mainActivity: MainActivity
    private lateinit var magazineRvAdapter: NewMagazineRvAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = NewFragmentMagazineBinding.inflate(inflater, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.fragment = this@NewFragmentMagazine
        _binding?.newViewModel = viewModel

        App()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deviceWidth = resources.displayMetrics.widthPixels
        val topImgParams: ViewGroup.LayoutParams = binding.topImg.layoutParams

        magazineRvAdapter = NewMagazineRvAdapter(requireContext(), viewModel, magazineLauncher)

        binding.apply {
            setStatusBarIconColor()

            magazineRv.adapter = magazineRvAdapter
            magazineRv.itemAnimator = null

            topImgParams.height = (deviceWidth * 1.4).toInt()
            topImgParams.width = deviceWidth

            topImg.layoutParams = topImgParams

            allBookmark.onThrottleClick {
                if (viewModel.isLogin.isEmpty()) magazineLauncher.launch(getLoginChkActivity(requireContext()))
                else magazineLauncher.launch(getBookMarkActivity(requireContext()))
            }

            tabs.setBackgroundColor(ContextCompat.getColor(this@NewFragmentMagazine.requireContext(), R.color.white))

            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}

                //탭이 눌렸을 경우
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        viewModel.tabItemList.forEach {
                            if (it.magazineTypeName == tab.contentDescription) {
                                viewModel.tabType = it.magazineType
                                viewModel.requestPage = 1

                                Log.e("매거진_카테고리_정보_탭_클릭", tab.contentDescription.toString())
                                if (viewModel.isLogin.isEmpty()) this@NewFragmentMagazine.viewModel.getNotMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = true)
                                else {
                                    this@NewFragmentMagazine.viewModel.getMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = true)
                                    this@NewFragmentMagazine.viewModel.getBookMark()
                                }
                            }
                        }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })

            //리프레시의 경우
            refreshLayout.setOnRefreshListener {
                viewModel.requestPage = 1
                binding.refreshLayout.isRefreshing = false
                if (viewModel.isLogin.isEmpty()) {
                    this@NewFragmentMagazine.viewModel.getNotMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = true)
                } else {
                    this@NewFragmentMagazine.viewModel.getMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = true)
                    this@NewFragmentMagazine.viewModel.getBookMark()
                }
            }

            nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (!binding.nestedScrollView.canScrollVertically(1)) { //최하단
                    if (!viewModel.lastPage) { //마지막 페이지가 아닌경우 통신, 마지막 페이지인 경우 통신하지 않음
                        viewModel.isLoading = true
                        binding.loading.visibility = View.VISIBLE

                        //스크롤의 끝에 도달 했을 때
                        if (viewModel.isLoading) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                launch(Dispatchers.Main) {
                                    if (viewModel.isLogin.isEmpty()) {
                                        this@NewFragmentMagazine.viewModel.getNotMemberMagazine(magazineType = viewModel.tabType, length = 20, page = viewModel.requestPage, isRefresh = false)
                                    } else {
                                        this@NewFragmentMagazine.viewModel.getMemberMagazine(magazineType = viewModel.tabType, length = 20, page = viewModel.requestPage, isRefresh = false)
                                        this@NewFragmentMagazine.viewModel.getBookMark()
                                    }
                                }.join()

                                launch (Dispatchers.IO) {
                                    //위 코루틴이 모든 작업을 완료한 뒤에 isLoading 값을 변경. 중복 호출을 막기위함
                                    viewModel.isLoading = false
                                }
                            }
                        }
                    }
                }

                magazineRvAdapter.setOnItemClickListener(object : NewMagazineRvAdapter.OnItemClickListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onItemClick(position: Int, isSelected: Boolean, data: MagazineItemVo) {
                        NewVibratorUtil().run {
                            init(mainActivity)
                            oneShot(100, 100)
                        }

                        if (isSelected) {
                            val memberBookmarkRegModel = MemberBookmarkRegModel(viewModel.memberId, "${data.magazineId}")
                            viewModel.updateBookMark(memberBookmarkRegModel)
                        } else {
                            val memberBookmarkRemoveModel = MemberBookmarkRemoveModel(viewModel.memberId, "${data.magazineId}")
                            viewModel.deleteBookMark(memberBookmarkRemoveModel)
                        }
                    }
                })
            }
        }

        requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        lifecycleScope.launch {
            launch(Dispatchers.IO) {
                viewModel.getMagazineTopImg() //매거진 이미지 호출
                viewModel.getMagazineTypeList() // 매거진 카테고리 호출

                //시작시
                //매거진 전체 호출
                if (viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                    viewModel.getNotMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = false)
                } else {
                    //회원인 경우 매거진 목록, 디바이스 체크, 북마크를 가져온다.
                    viewModel.getMemberMagazine(magazineType = viewModel.tabType, length = 20, page = 1, isRefresh = false)
                    viewModel.getBookMark()
                    mainViewModel.memberDeviceChk()
                }
            }

            launch(Dispatchers.Main) {
                if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                    try {
                        this@NewFragmentMagazine.mainViewModel.deviceChk.collect {
                            when (it) {
                                is MainViewModel.MemberDeviceState.Success -> {
//                                    LogUtil.e("=== MemberDeviceChk Success === ${it.isSuccess}")
                                }

                                is MainViewModel.MemberDeviceState.Failure -> {
//                                    LogUtil.e("=== MemberDeviceChk Failure === ${it.message}")

                                    val statusCode = extractStatusCode(it.message)

                                    if(statusCode != 406) {
                                        PrefsHelper.removeKey("inputPinNumber")
                                        PrefsHelper.removeKey("memberId")
                                        PrefsHelper.removeKey("isLogin")
                                        magazineLauncher.launch(getIntroActivity(requireContext()))
                                        BackPressedUtil().activityFinish(mainActivity,mainActivity)
                                    }


                                }

                                else -> {
//                                    LogUtil.e("=== MemberDeviceChk More === $it")
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }

            launch(Dispatchers.Main) {
                requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                setStatusBarIconColor()
                val deviceWidth = resources.displayMetrics.widthPixels
                val topImgParams: ViewGroup.LayoutParams = binding.topImg.layoutParams
                topImgParams.height = (deviceWidth * 1.4).toInt()
                topImgParams.width = deviceWidth
                binding.topImg.layoutParams = topImgParams

                this@NewFragmentMagazine.viewModel.magazineTopImg.collect {
                    when (it) {
                        is NewMagazineViewModel.MagazineTopState.Success -> {
                            PrefsHelper.write("magazineImgUrl", it.isSuccess.data.toString())
                            Glide.with(requireContext()).load(it.isSuccess.data.toString()).into(binding.topImg)
                        }

                        is NewMagazineViewModel.MagazineTopState.Failure -> {
                            Glide.with(requireContext()).load(PrefsHelper.read("magazineImgUrl", "")).into(binding.topImg)
                        }

                        else -> { LogUtil.v("MagazineImgUrl Loaind") }
                    }
                }
            }

            launch(Dispatchers.Main) {
                if (viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                    binding.allBookmarkCount.text = "0"
                } else {
                    viewModel.bookMarkList.collect {
                        when (it) {
                            is NewMagazineViewModel.BookMarkState.Success -> {
                                binding.allBookmarkCount.text = it.bookMarkList.size.toString()
                            }
                            is NewMagazineViewModel.BookMarkState.Failure -> {

                            }
                            else -> {}
                        }
                    }
                }
            }

            launch(Dispatchers.Main) {
                viewModel.updateBookmarkList.collect { vo ->
                    when (vo) {
                        is NewMagazineViewModel.MemberBookMarkState.Success -> {
                            binding.allBookmarkCount.text = "${vo.data.bookmarkCount}"
                        }
                        is NewMagazineViewModel.MemberBookMarkState.Failure -> {

                        }
                        else -> {

                        }
                    }
                }
            }

            launch(Dispatchers.Main) {
                viewModel.magazineTypeList.collect {
                    when(it) {
                        is NewMagazineViewModel.MagazineTypeState.Success -> {
                            it.data.forEach { vo ->
                                binding.tabs.addTab(binding.tabs.newTab().setText(vo.magazineTypeName))
                                viewModel.tabItemList.add(vo)
                            }
                        }
                        is NewMagazineViewModel.MagazineTypeState.Failure -> {

                        }
                        else -> {}
                    }
                }
            }

            //기본 collect
            launch(Dispatchers.Main) {
                if (viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                    this@NewFragmentMagazine.viewModel.magazineNotMemberList.collect {
                        when (it) {
                            is NewMagazineViewModel.MagazineState.Success -> {
                                binding.loading.visibility = View.GONE
                                magazineRvAdapter.addItems(it.magazineList)
                            }

                            is NewMagazineViewModel.MagazineState.Failure -> {}
                            else -> {}
                        }
                    }
                } else {
                    this@NewFragmentMagazine.viewModel.magazineMemberList.collect {
                        when (it) {
                            is NewMagazineViewModel.MagazineState.Success -> {
                                binding.loading.visibility = View.GONE
                                magazineRvAdapter.addItems(it.magazineList)
                            }

                            is NewMagazineViewModel.MagazineState.Failure -> {}
                            else -> {}
                        }
                    }
                }
            }


            // 리프레시 collect
            launch(Dispatchers.Main) {
                if (viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                    this@NewFragmentMagazine.viewModel.magazineNotMemberRefreshList.collect {
                        when (it) {
                            is NewMagazineViewModel.MagazineState.Success -> {
                                binding.loading.visibility = View.GONE
                                magazineRvAdapter.submitList(it.magazineList)
                            }

                            is NewMagazineViewModel.MagazineState.Failure -> {}
                            else -> {}
                        }
                    }
                } else {
                    this@NewFragmentMagazine.viewModel.magazineMemberRefreshList.collect {
                        when (it) {
                            is NewMagazineViewModel.MagazineState.Success -> {
                                binding.loading.visibility = View.GONE
                                magazineRvAdapter.submitList(it.magazineList)
                            }

                            is NewMagazineViewModel.MagazineState.Failure -> {}
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onResume() {
        super.onResume()
        binding.refreshLayout.isRefreshing = false
    }

    private fun setStatusBarIconColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else activity?.window?.decorView?.systemUiVisibility = activity?.window?.decorView!!.systemUiVisibility
    }

    private fun extractStatusCode(errorMessage: String): Int {
        val regex = Regex("""(\d{3})""")
        val matchResult = regex.find(errorMessage)
        return matchResult?.value?.toInt() ?: -1
    }

    companion object {
        fun newInstance(title: String): NewFragmentMagazine {
            return NewFragmentMagazine().apply {
                arguments = bundleOf("title" to title)
            }
        }

        fun getIntroActivity(context: Context): Intent {
            val intent = Intent(context, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("another","another")
            return intent
        }

        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        // 로그인 체크 이동
        fun getLoginChkActivity(context: Context): Intent {
            val intent = Intent(context, LoginChkActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }

        // 북마크 상세 이동
        fun getBookMarkActivity(context: Context): Intent {
            return Intent(context, NewBookMarkActivity::class.java)
        }
    }
}
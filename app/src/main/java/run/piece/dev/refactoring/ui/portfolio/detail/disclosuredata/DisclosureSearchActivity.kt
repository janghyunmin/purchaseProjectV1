package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.databinding.ActivityDisclosureSearchBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide.disclosure.DisclosureTabRvAdapter
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.board.model.FilesVo
import run.piece.domain.refactoring.board.model.InvestmentDisclosureItemVo
import run.piece.domain.refactoring.board.model.ManagementDisclosureItemVo

@AndroidEntryPoint
class DisclosureSearchActivity : AppCompatActivity(R.layout.activity_disclosure_search) {
    private lateinit var binding: ActivityDisclosureSearchBinding
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var disclosureSearchAdapter: DisclosureSearchRvAdapter // 검색어 저장 Rv
    private lateinit var disclosureRvAdapter: DisclosureTabRvAdapter // 검색 결과 Rv
    private lateinit var mPrefs: SharedPreferences
    private lateinit var mEditPrefs: SharedPreferences.Editor
    private var searchItem = ArrayList<DisclosureSearchDataItem>() // 저장할 ArrayList
    private val disclosureDataItem = mutableListOf<DisclosureDataItem>() // API 에서 받아온 공시 리스트 데이터
    private var stringPrefs: String? = null // 저장할 때 사용할 문자열 변수
    private val viewModel: DisclosureSearchViewModel by viewModels()
    private var searchJob: Job? = null
    private var portfolioId: String = ""
    private var displayType: String = ""
    private var searchViewStatus: String = ""
    private var tempSearchText: String = ""

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisclosureSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.lifecycleOwner = this@DisclosureSearchActivity
        binding.activity = this@DisclosureSearchActivity
        binding.vm = viewModel

        val networkConnection = NetworkConnection(this@DisclosureSearchActivity)
        networkConnection.observe(this@DisclosureSearchActivity) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this@DisclosureSearchActivity, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        coroutineScope = lifecycleScope
        intent?.let {
            portfolioId = it.getStringExtra("portfolioId").toString()
        }


        binding.apply {
            window.apply {
                // 캡쳐방지 Kotlin Ver
                addFlags(WindowManager.LayoutParams.FLAG_SECURE);

                //상태바 아이콘(true: 검정 / false: 흰색)
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }

            when(getDisplayType(resources.displayMetrics)) {
                // 폴드 펼침
                "FOLD_DISPLAY_EXPAND" -> {
                    binding.tabLayout.isTabIndicatorFullWidth = false
                }
                else -> {
                    binding.tabLayout.isTabIndicatorFullWidth = true
                }
            }

            backIv.onThrottleClick {
                BackPressedUtil().activityFinish(this@DisclosureSearchActivity,this@DisclosureSearchActivity)
            }

            disclosureSearchAdapter = DisclosureSearchRvAdapter(this@DisclosureSearchActivity)

            searchRv.run {
                adapter = disclosureSearchAdapter
                this.layoutManager = LinearLayoutManager(this@DisclosureSearchActivity, RecyclerView.VERTICAL, false)
                itemAnimator = null
            }

            coroutineScope.launch(Dispatchers.Main) {
                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                searchEt.run {
                    delay(300)
                    isFocusableInTouchMode = true
                    requestFocus()
                    // 검색 후 키보드 보이기
                    inputManager.showSoftInput(binding.searchEt, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        initView()
        initPrefs()
        retrievePrefs()
        initSearchRvView()

        binding.clearTouchLayout.visibility = View.GONE

        // 검색어 입력값 실시간 감지
        binding.searchEt.addTextChangedListener {
            if (it != null) {
                if (it.isEmpty()) {
                    binding.clearTouchLayout.visibility = View.GONE
                } else {
                    binding.clearTouchLayout.visibility = View.VISIBLE
                    binding.clearTouchLayout.onThrottleClick {
                        binding.searchEt.text = null
                        tempSearchText = ""
                    }
                }

                disclosureDataItem.clear()
                binding.searchLayout.visibility = View.VISIBLE // 검색 영역 Layout
                binding.searchTouchLayout.visibility = View.VISIBLE // 돋보기 영역 Layout
                binding.recentSearchLayout.visibility = View.VISIBLE // 최근검색어 Text , 전체 삭제 Text Layout
                binding.tabParentLayout.visibility = View.GONE // 경영공시 , 투자공시 Tab Layout

                // 검색 리스트 사이즈가 0이면
                if (searchItem.size == 0 || searchItem.isEmpty()) {
                    binding.noSearchLayout.visibility = View.VISIBLE
                    binding.allDeleteTouchLayout.visibility = View.GONE
                    binding.searchRv.visibility = View.GONE
                }

                // 검색 리스트 사이즈가 1 이상이면
                else {

                    // 검색 리스트 사이즈가 11 이상일때 마지막 Position 의 배열을 지우고 0번째에 새로운 아이템을 추가한다.
                    if (searchItem.size > 10) {
                        removeLastPrefData()
                    }

                    binding.noSearchLayout.visibility = View.GONE
                    binding.allDeleteTouchLayout.visibility = View.VISIBLE
                    binding.searchRv.visibility = View.VISIBLE
                    disclosureSearchAdapter.searchItem = searchItem
                }
            }
        }


        // 돋보기 아이콘 검색 버튼
        binding.searchTouchLayout.onThrottleClick {
            if (binding.searchEt.text.isNullOrEmpty() || binding.searchEt.text.isNullOrBlank()) {
                emptyText()
            } else {
                emptyText()
                binding.tabLayout.getTabAt(0)?.select()
                tempSearchText = binding.searchEt.text.toString()
                searchItem.size + 1
                coroutineScope.launch {
                    // 검색어를 저장한다.
                    val savePrefs = async { savePrefs(searchText = tempSearchText) }
                    val apiCall = async { apiCall(tabType = "경영공시", searchText = tempSearchText) }
                    val loadingJob = async { loadingView() }
                    val resultView = async { resultView(tabType = "경영공시", searchText = tempSearchText) }

                    savePrefs.join()
                    apiCall.await()
                    loadingJob.join()
                    resultView.await()
                }
                // 검색 리스트 사이즈가 11 이상일때 마지막 Position 의 배열을 지우고 0번째에 새로운 아이템을 추가한다.
                if (searchItem.size > 10) {
                    removeLastPrefData()
                }
            }
        }

        // 기기 키패드 완료 버튼
        binding.searchEt.setOnEditorActionListener { textView, action, keyEvent ->
            if (binding.searchEt.text.isNullOrEmpty() || binding.searchEt.text.isNullOrBlank()) {
                emptyText()
                return@setOnEditorActionListener false
            } else {
                if (action == EditorInfo.IME_ACTION_DONE) {
                    binding.tabLayout.getTabAt(0)?.select()
                    tempSearchText = binding.searchEt.text.toString()
                    searchItem.size + 1
                    coroutineScope.launch {
                        val savePrefs = async { savePrefs(searchText = tempSearchText) }
                        val apiCall = async { apiCall(tabType = "경영공시", searchText = tempSearchText) }
                        val loadingJob = async { loadingView() }
                        val resultView = async { resultView(tabType = "경영공시", searchText = tempSearchText) }

                        savePrefs.join()
                        apiCall.await()
                        loadingJob.join()
                        resultView.await()
                    }

                    // 검색 리스트 사이즈가 11 이상일때 마지막 Position 의 배열을 지우고 0번째에 새로운 아이템을 추가한다.
                    if (searchItem.size > 10) {
                        removeLastPrefData()
                    }
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }


        // 전체 삭제 버튼
        binding.allDeleteTouchLayout.onThrottleClick {
            // 최근 검색어 전체 삭제 Dlg
            val appConfirmDF =
                AppConfirmDF.newInstance(
                    getString(R.string.dlg_title),
                    getString(R.string.dlg_content),
                    false,
                    R.string.dlg_delete_txt,
                    positiveAction = {
                        clearAllPrefData()
                    },
                    R.string.dlg_cancel_txt,
                    negativeAction = {

                    },
                    backgroundDrawable = R.drawable.btn_round_ff7878,
                    dismissAction = {}
                )

            appConfirmDF.show(supportFragmentManager, "검색어 전체 삭제")
        }

        // 검색어 전체 영역 터치 OnClick
        disclosureSearchAdapter.setItemClickListener(object : DisclosureSearchRvAdapter.OnItemClickListener {
            @SuppressLint("SetTextI18n")
            override fun onClick(v: View, tag: String, position: Int, title: String) {
                when (tag) {
                    "검색 조회" -> {
                        binding.tabLayout.getTabAt(0)?.select()
                        searchItem.size + 1
                        tempSearchText = title
                        disclosureRvAdapter = DisclosureTabRvAdapter(this@DisclosureSearchActivity)

                        coroutineScope.launch {
                            val apiCall = async { apiCall(tabType = "경영공시", searchText = title) }
                            val loadingJob = async { loadingView() }
                            val resultView = async { resultView(tabType = "경영공시", searchText = title) }

                            apiCall.await()
                            loadingJob.join()
                            resultView.await()
                        }
                    }

                    "개별 삭제" -> {
                        removePrefData(position, searchItem.size - 1)
                    }
                }
            }
        })

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {
                        binding.tabLayout.getTabAt(0)?.select()
                        disclosureRvAdapter = DisclosureTabRvAdapter(this@DisclosureSearchActivity)

                        CoroutineScope(Dispatchers.Main).launch {
                            val apiCall = async { apiCall(tabType = "경영공시", searchText = tempSearchText) }
                            val loadingJob = async { loadingView() }
                            val resultView = async { resultView(tabType = "경영공시", searchText = tempSearchText) }

                            apiCall.await()
                            loadingJob.join()
                            resultView.await()
                        }
                    }
                    1 -> {
                        binding.tabLayout.getTabAt(1)?.select()
                        disclosureRvAdapter = DisclosureTabRvAdapter(this@DisclosureSearchActivity)

                        CoroutineScope(Dispatchers.Main).launch {
                            val apiCall = async { apiCall(tabType = "투자공시", searchText = tempSearchText) }
                            val loadingJob = async { loadingView() }
                            val resultView = async { resultView(tabType = "투자공시", searchText = tempSearchText) }

                            apiCall.await()
                            loadingJob.join()
                            resultView.await()
                        }

                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        BackPressedUtil().activityCreate(this@DisclosureSearchActivity,this@DisclosureSearchActivity)
        BackPressedUtil().systemBackPressed(this@DisclosureSearchActivity,this@DisclosureSearchActivity)
    }


    private fun initView() = runBlocking {
        launch(Dispatchers.Default) {
            async {
                delay(100)
            }
        }
    }

    private suspend fun loadingView() {
        binding.loadingIv.visibility = View.VISIBLE
        delay(500)
        binding.loadingIv.visibility = View.GONE
    }

    private fun emptyText() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(binding.searchEt, InputMethodManager.SHOW_IMPLICIT)
        binding.searchEt.run {
            post {
                isFocusableInTouchMode = true
                requestFocus()
                setSelection(this.length())
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initSearchRvView() {
        searchJob?.cancel()
        searchJob = viewModel.viewModelScope.launch {
            with(binding) {
                disclosureDataItem.clear()
                searchLayout.visibility = View.VISIBLE // 검색 영역 Layout
                searchTouchLayout.visibility = View.VISIBLE // 돋보기 영역 Layout
                recentSearchLayout.visibility = View.VISIBLE // 최근검색어 Text , 전체 삭제 Text Layout
                tabParentLayout.visibility = View.GONE // 경영공시 , 투자공시 Tab Layout

                // 검색 리스트 사이즈가 0이면
                if (searchItem.size == 0 || searchItem.isEmpty()) {
                    noSearchLayout.visibility = View.VISIBLE
                    allDeleteTouchLayout.visibility = View.GONE
                    searchRv.visibility = View.GONE
                }

                // 검색 리스트 사이즈가 1 이상이면
                else {

                    // 검색 리스트 사이즈가 11 이상일때 마지막 Position 의 배열을 지우고 0번째에 새로운 아이템을 추가한다.
                    if (searchItem.size > 10) {
                        removeLastPrefData()
                    }

                    noSearchLayout.visibility = View.GONE
                    allDeleteTouchLayout.visibility = View.VISIBLE
                    searchRv.visibility = View.VISIBLE
                    disclosureSearchAdapter.searchItem = searchItem
                    disclosureSearchAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    /**
     * SharedPreferences 설정
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun initPrefs() {
        mPrefs = getSharedPreferences("pref_file", MODE_PRIVATE) // SharedPreferences 불러오기
        mEditPrefs = mPrefs.edit() // SharedPreferences Edit 선언
        stringPrefs = mPrefs.getString("pref_data", null)

        // SharedPreferences 데이터가 있으면 String을 ArrayList로 변환
        // fromJson → json 형태의 문자열을 명시한 객체로 변환(두번째 인자)
        if (stringPrefs != null && stringPrefs != "[]") {
            searchItem = GsonBuilder().create().fromJson(
                stringPrefs, object : TypeToken<ArrayList<DisclosureSearchDataItem>>() {}.type
            )
        }
    }


    /**
     * SharedPreferences 저장
     */
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun savePrefs(searchText: String) {
        delay(100)
        // ArrayList에 추가
        searchItem.add(
            0,
            DisclosureSearchDataItem(
                searchText
            )
        )

        // ArrayList를 json 형태의 String으로 변환
        // toJson → json으로 변환된 문자열 리턴
        stringPrefs = GsonBuilder().create().toJson(
            searchItem,
            object : TypeToken<ArrayList<DisclosureSearchDataItem>>() {}.type
        )
        mEditPrefs.putString("pref_data", stringPrefs) // SharedPreferences에 push
        mEditPrefs.apply() // SharedPreferences 적용

        disclosureSearchAdapter.searchItem = searchItem // Adapter에 데이터 넘김
//        disclosureSearchAdapter.notifyDataSetChanged()
    }

    /**
     * SharedPreferences에서 데이터 삭제
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun removePrefData(position: Int, size: Int) {
        if (size == 0) {
            with(binding) {
                allDeleteTouchLayout.visibility = View.GONE
                noSearchLayout.visibility = View.VISIBLE
                searchRv.visibility = View.GONE
            }
        } else {
            with(binding) {
                noSearchLayout.visibility = View.GONE
                allDeleteTouchLayout.visibility = View.VISIBLE
                searchRv.visibility = View.VISIBLE
                disclosureSearchAdapter.searchItem = searchItem
                disclosureSearchAdapter.notifyDataSetChanged()
            }
        }

        if (position >= 0 && position < searchItem.size) {
            searchItem.removeAt(position) // 해당 위치의 데이터 삭제

            // ArrayList를 json 형태의 String으로 변환
            stringPrefs = GsonBuilder().create().toJson(
                searchItem,
                object : TypeToken<ArrayList<DisclosureSearchDataItem>>() {}.type
            )
            mEditPrefs.putString("pref_data", stringPrefs) // SharedPreferences에 push
            mEditPrefs.apply() // SharedPreferences 적용

            disclosureSearchAdapter.searchItem = searchItem // Adapter에 데이터 넘김
            disclosureSearchAdapter.notifyDataSetChanged()
        }
    }

    /**
     * SharedPreferences에서 마지막 데이터 삭제
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun removeLastPrefData() {
        if (searchItem.isNotEmpty()) {
            searchItem.removeAt(searchItem.size - 1) // 마지막 데이터 삭제

            // ArrayList를 json 형태의 String으로 변환
            stringPrefs = GsonBuilder().create().toJson(
                searchItem,
                object : TypeToken<ArrayList<DisclosureSearchDataItem>>() {}.type
            )
            mEditPrefs.putString("pref_data", stringPrefs) // SharedPreferences에 push
            mEditPrefs.apply() // SharedPreferences 적용

            disclosureSearchAdapter.searchItem = searchItem // Adapter에 데이터 넘김
            disclosureSearchAdapter.notifyDataSetChanged()
        }
    }

    /**
     * SharedPreferences의 모든 데이터 삭제
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun clearAllPrefData() {
        with(binding) {
            noSearchLayout.visibility = View.VISIBLE
            allDeleteTouchLayout.visibility = View.GONE
            searchRv.visibility = View.GONE
        }

        searchItem.clear() // 모든 데이터 삭제

        // ArrayList를 json 형태의 String으로 변환
        stringPrefs = GsonBuilder().create().toJson(
            searchItem,
            object : TypeToken<ArrayList<DisclosureSearchDataItem>>() {}.type
        )
        mEditPrefs.putString("pref_data", stringPrefs) // SharedPreferences에 push
        mEditPrefs.apply() // SharedPreferences 적용

        disclosureSearchAdapter.searchItem = searchItem // Adapter에 데이터 넘김
        disclosureSearchAdapter.notifyDataSetChanged()
    }

    private fun retrievePrefs() {
        mPrefs = getSharedPreferences("pref_file", MODE_PRIVATE) // SharedPreferences 불러오기
        mEditPrefs = mPrefs.edit() // SharedPreferences Edit 선언
        stringPrefs = mPrefs.getString("pref_data", null)

        // SharedPreferences 데이터가 있으면 String을 ArrayList로 변환
        // fromJson → json 형태의 문자열을 명시한 객체로 변환(두번째 인자)
        if (stringPrefs != null && stringPrefs != "[]") {
            searchItem = GsonBuilder().create().fromJson(
                stringPrefs, object : TypeToken<ArrayList<DisclosureSearchDataItem>>() {}.type
            )
        }

        // Update your adapter or UI with the retrieved data
        disclosureSearchAdapter.searchItem = searchItem
        disclosureSearchAdapter.notifyDataSetChanged()
    }


    private fun apiCall(tabType: String, searchText: String) {
        // 해당 검색어로 공시 조회 API를 호출한다.
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getBoard(tabType,portfolioId = portfolioId, searchText, 1,1)
        }
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun resultView(tabType: String, searchText: String) {
        searchJob?.cancel()
        searchJob = viewModel.viewModelScope.launch {
            delay(100)
            when (tabType) {
                "경영공시" -> {
                    viewModel.disclosureList.collect { vo ->
                        when (vo) {
                            is DisclosureSearchViewModel.DisclosureListState.Success -> {
                                launch(Dispatchers.Main) {
                                    binding.searchEt.setText(searchText) // 검색어 setText
                                    binding.clearTouchLayout.visibility = View.VISIBLE // Text Clear Icon
                                    binding.noSearchLayout.visibility = View.GONE // 최근 검색한 내역이 없어요 Layout
                                    binding.tabParentLayout.visibility = View.VISIBLE // 경영공시 , 투자공시 Tab Layout
                                    binding.searchTouchLayout.visibility = View.GONE // 돋보기 모양 아이콘
                                    binding.searchRv.visibility = View.GONE // 검색어 저장된 리스트 RecyclerView

                                    // Reset margin end to default
                                    val layoutParams = binding.clearTouchLayout.layoutParams as ConstraintLayout.LayoutParams
                                    layoutParams.setMargins(
                                        layoutParams.leftMargin,
                                        layoutParams.topMargin,
                                        4,
                                        layoutParams.bottomMargin
                                    )
                                    binding.clearTouchLayout.layoutParams = layoutParams
                                    binding.clearTouchLayout.setPadding(10, 10, 10, 10)

                                    binding.searchEt.isFocusableInTouchMode = false
                                    binding.searchEt.isFocusable = false

                                    // 검색 후 키보드 숨기기
                                    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(binding.searchEt.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)


                                    searchInitView("경영공시", vo.disclosure.managementDisclosure.disclosure, vo.disclosure.investmentDisclosure.disclosure)

                                    disclosureSearchAdapter.notifyDataSetChanged()
                                    // 검색 화면 유지중일때 검색어 입력 터치시 이전 상태로 롤백
                                    binding.searchEt.onThrottleClick {

                                        emptyText()
                                        if (binding.searchEt.text.isNullOrEmpty()) {
                                            binding.clearTouchLayout.visibility = View.GONE
                                        } else {
                                            binding.clearTouchLayout.visibility = View.VISIBLE
                                        }

                                        // 검색어 입력값 실시간 감지
                                        binding.searchEt.addTextChangedListener {
                                            if (it != null) {
                                                if (it.isEmpty()) {
                                                    binding.clearTouchLayout.visibility = View.GONE
                                                } else {
                                                    binding.clearTouchLayout.visibility = View.VISIBLE
                                                    binding.clearTouchLayout.onThrottleClick {
                                                        binding.searchEt.text = null
                                                        tempSearchText = ""
                                                        emptyText()
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    binding.clearTouchLayout.onThrottleClick {
                                        binding.searchEt.text = null
                                        tempSearchText = ""
                                        emptyText()
                                    }
                                }
                            }

                            is DisclosureSearchViewModel.DisclosureListState.Failure -> {
                                Log.v("경영 공시 Fail : ", vo.message)
                            }

                            else -> {
                                Log.v("경영 공시 Loading : ", "$vo")
                            }
                        }
                    }
                }

                "투자공시" -> {
                    viewModel.disclosureList.collect { vo ->
                        when (vo) {
                            is DisclosureSearchViewModel.DisclosureListState.Success -> {
                                launch(Dispatchers.Main) {
                                    binding.searchEt.setText(searchText) // 검색어 setText
                                    binding.clearTouchLayout.visibility = View.VISIBLE // Text Clear Icon
                                    binding.noSearchLayout.visibility = View.GONE // 최근 검색한 내역이 없어요 Layout
                                    binding.tabParentLayout.visibility = View.VISIBLE // 경영공시 , 투자공시 Tab Layout
                                    binding.searchTouchLayout.visibility = View.GONE // 돋보기 모양 아이콘
                                    binding.searchRv.visibility = View.GONE // 검색어 저장된 리스트 RecyclerView

                                    // Reset margin end to default
                                    val layoutParams = binding.clearTouchLayout.layoutParams as ConstraintLayout.LayoutParams
                                    layoutParams.setMargins(
                                        layoutParams.leftMargin,
                                        layoutParams.topMargin,
                                        4,
                                        layoutParams.bottomMargin
                                    )
                                    binding.clearTouchLayout.layoutParams = layoutParams
                                    binding.clearTouchLayout.setPadding(10, 10, 10, 10)

                                    binding.searchEt.isFocusableInTouchMode = false
                                    binding.searchEt.isFocusable = false

                                    // 검색 후 키보드 숨기기
                                    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputManager.hideSoftInputFromWindow(binding.searchEt.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

                                    searchInitView("투자공시", vo.disclosure.managementDisclosure.disclosure, vo.disclosure.investmentDisclosure.disclosure)

                                    disclosureSearchAdapter.notifyDataSetChanged()
                                    disclosureRvAdapter.setItemClickListener(object : DisclosureTabRvAdapter.OnItemClickListener {
                                        override fun onClick(v: View, position: Int) {

                                            startActivity(
                                                getDisclosureDetailActivity(
                                                    context = this@DisclosureSearchActivity,
                                                    viewName = "투자공시 상세",
                                                    topTitle = "투자공시",
                                                    title = disclosureDataItem[position].title,
                                                    codeName = disclosureDataItem[position].codeName,
                                                    boardId = disclosureDataItem[position].boardId,
                                                    contents = disclosureDataItem[position].contents,
                                                    createAt = disclosureDataItem[position].createdAt.toBaseDateFormat(),
                                                    tabDvn = disclosureDataItem[position].tabDvn,
                                                    files = disclosureDataItem[position].files
                                                )
                                            )
                                        }
                                    })

                                    // 검색 화면 유지중일때 검색어 입력 터치시 이전 상태로 롤백
                                    binding.searchEt.onThrottleClick {

                                        emptyText()

                                        if (binding.searchEt.text.isNullOrEmpty()) {
                                            binding.clearTouchLayout.visibility = View.GONE
                                        } else {
                                            binding.clearTouchLayout.visibility = View.VISIBLE
                                        }

                                        // 검색어 입력값 실시간 감지
                                        binding.searchEt.addTextChangedListener {
                                            if (it != null) {
                                                if (it.isEmpty()) {
                                                    binding.clearTouchLayout.visibility = View.GONE
                                                } else {
                                                    binding.clearTouchLayout.visibility = View.VISIBLE
                                                    binding.clearTouchLayout.onThrottleClick {
                                                        binding.searchEt.text = null
                                                        tempSearchText = ""
                                                        emptyText()
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    binding.clearTouchLayout.onThrottleClick {
                                        binding.searchEt.text = null
                                        tempSearchText = ""
                                        emptyText()
                                    }
                                }


                            }

                            is DisclosureSearchViewModel.DisclosureListState.Failure -> {
                                Log.v("투자 공시 Fail : ", vo.message)
                            }

                            else -> {
                                Log.v("투자 공시 Loading : ", "$vo")
                            }
                        }
                    }
                }
            }


        }
    }

    private fun searchInitView(type: String, managementVo: List<ManagementDisclosureItemVo>, investmentVo: List<InvestmentDisclosureItemVo>) {
        val fragments = supportFragmentManager.fragments
        fragments.forEach { supportFragmentManager.beginTransaction().hide(it).commit() }

        when(type) {
            "경영공시" -> {
                searchViewStatus = "Y"
                coroutineScope.launch {
                    delay(200)
                    supportFragmentManager.commitNow {
                        setReorderingAllowed(true)
                        replace(R.id.management, DisclosureManagementFragment.newInstance(managementVo, portfolioId,"Y", binding.searchEt.text.toString()))
                    }
                }
            }
            "투자공시" -> {
                searchViewStatus = "Y"
                coroutineScope.launch {
                    delay(200)
                    supportFragmentManager.commitNow {
                        setReorderingAllowed(true)
                        replace(R.id.investment, DisclosureInvestmentFragment.newInstance(investmentVo , portfolioId,"Y", binding.searchEt.text.toString()))
                    }
                }

            }
        }
    }

    private fun getDisplayType(displayMetrics: DisplayMetrics): String {
        return when {
            displayMetrics.widthPixels > 1600 -> {
                displayType = "FOLD_DISPLAY_EXPAND"
                displayType
            }

            displayMetrics.widthPixels < 980 -> {
                displayType = "FOLD_DISPLAY_COLLAPSE"
                displayType
            }

            else -> {
                displayType = "BASIC_DISPLAY"
                displayType
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    companion object {
        private fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

        // 네트워크 화면 이동
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
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
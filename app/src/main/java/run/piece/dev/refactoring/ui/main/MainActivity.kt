package run.piece.dev.refactoring.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.ActivityMainBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.magazine.NewFragmentMagazine
import run.piece.dev.refactoring.ui.more.NewFragmentMore
import run.piece.dev.refactoring.ui.wallet.NewFragmentWallet
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.NewVibratorUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.view.common.LoginChkActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.view.fragment.FragmentHome
import run.piece.dev.view.main.dialog.EventSheet
import run.piece.dev.widget.utils.NetworkConnection

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val dataNexusViewModel by viewModels<DataNexusViewModel>()
    private var fragmentStateMap: MutableMap<String, Bundle?> = mutableMapOf()

    private var fragmentHome: FragmentHome? = null
    private var fragmentMagazine: NewFragmentMagazine? = null
    private var newFragmentWallet: NewFragmentWallet? = null
    private var fragmentMore: NewFragmentMore? = null

    private val fgMoreBundle = Bundle()
    private var eventSheet: EventSheet? = null
    private var lastFragmentTag: String? = null

    enum class MainMenuType {
        MENU1, MENU2, MENU3, MENU4;

        val tag: String
            get() = name.uppercase()
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        binding.lifecycleOwner = this@MainActivity
        binding.activity = this@MainActivity
        binding.viewModel = viewModel
        binding.dataStoreViewModel = dataNexusViewModel

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = 0x00000000  // transparent
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        lastFragmentTag = savedInstanceState?.getString(LAST_FRAGMENT_TAG)
        val mainMenuType = findMenuByTag(lastFragmentTag)
        bindMenu(mainMenuType)

        coroutineScope = lifecycleScope
        binding.apply {

            // Default Intent Setting
            this@MainActivity.intent?.let {
                updateResult()

                when (it.getStringExtra("requestCode")) {
                    // 피스 구매 후 지갑 이동
                    // 회원 탈퇴의 경우
                    // 내지갑 -> 계좌 변경 후 다시 내지갑으로 이동할 때
                    "100", "1000", "1001" -> {
                        // 처음 앱이 시작될 때 홈 아이콘을 선택된 상태로 설정
                        binding.bottomNav.selectedItemId = R.id.item_wallet // Select Icon
                        bindMenu(MainMenuType.MENU3) // Select Fragment
                    }

                    else -> {
                        binding.bottomNav.selectedItemId = R.id.item_home // Select Icon
                        bindMenu(MainMenuType.MENU1) // Select Fragment
                    }
                }

                when (it.getStringExtra("backStack")) {
                    "N" -> {
                        BackPressedUtil().activityClear(this@MainActivity, this@MainActivity)
                    }

                    "Y" -> {
                        BackPressedUtil().activityCreate(this@MainActivity, this@MainActivity)
                    }
                }
            }

            // Default API Setting
            if (this@MainActivity.viewModel.isLogin.isNotEmpty() && this@MainActivity.viewModel.memberId.isNotEmpty()) {
                this@MainActivity.viewModel.getMemberData()
                this@MainActivity.viewModel.memberDeviceChk()

                coroutineScope.launch(Dispatchers.Main) {
                    var result = ""
                    var btnTitle = ""
                    // 회원 정보 조회 API Response Data를 가져온다.
                    this@MainActivity.viewModel.memberInfo.collect { it ->
                        when (it) {
                            is MainViewModel.MemberInfoState.Success -> {

                                PrefsHelper.write("name", it.memberVo.name)
                                dataNexusViewModel.putName(it.memberVo.name)

                                // 투자 성향 분석을 한번도 진행하지 않은 회원
                                if (it.memberVo.preference == null) {
                                    result = getString(R.string.investment_more_item_null_title)
                                    btnTitle = getString(R.string.investment_more_item_null_btn_title)

                                    dataNexusViewModel.putInvestResult("")
                                    dataNexusViewModel.putInvestFinalScore(0)
                                }
                                // 투자 성향 분석을 한번이라도 진행한 회원
                                else {
                                    result = it.memberVo.preference?.result.toString()
                                    btnTitle = getString(R.string.investment_more_item_btn_title)

                                    dataNexusViewModel.putInvestResult(it.memberVo.preference?.result.default())
                                    dataNexusViewModel.putInvestFinalScore(it.memberVo.preference?.score.default())

                                    fgMoreBundle.putString("userName", it.memberVo.name)
                                    fgMoreBundle.putString("joinDay", it.memberVo.joinDay)
                                    fgMoreBundle.putString("birthDay", it.memberVo.birthDay)
                                    fgMoreBundle.putString("phoneNumber", it.memberVo.cellPhoneNo)
                                    fgMoreBundle.putString("baseAddress", it.memberVo.baseAddress)
                                    fgMoreBundle.putString("detailAddress", it.memberVo.detailAddress)
                                    fgMoreBundle.putString("email", it.memberVo.email)
                                    fgMoreBundle.putString("result", result)
                                    fgMoreBundle.putString("btnTitle", btnTitle)
                                }
                            }

                            is MainViewModel.MemberInfoState.Failure -> {
//                                LogUtil.e("MainViewModel : ${it.message}")
                            }

                            else -> {
//                                LogUtil.e("MainViewModel : $it")
                            }
                        }
                    }
                }

                coroutineScope.launch(Dispatchers.Main) {
                    if (this@MainActivity.viewModel.isLogin.isNotEmpty() && this@MainActivity.viewModel.memberId.isNotEmpty()) {
                        launch(Dispatchers.Main) {
                            try {
                                this@MainActivity.viewModel.deviceChk.collect {
                                    when (it) {
                                        is MainViewModel.MemberDeviceState.Success -> {
//                                            LogUtil.e("=== MemberDeviceChk Success === ${it.isSuccess}")
                                        }

                                        is MainViewModel.MemberDeviceState.Failure -> {
//                                            LogUtil.e("=== MemberDeviceChk Failure === ${it.message}")

                                            val statusCode = extractStatusCode(it.message)
                                            if (statusCode != 406) {
                                                PrefsHelper.removeKey("inputPinNumber")
                                                PrefsHelper.removeKey("memberId")
                                                PrefsHelper.removeKey("isLogin")
                                                startActivity(getIntroActivity(this@MainActivity))
                                                BackPressedUtil().activityFinish(this@MainActivity, this@MainActivity)
                                            }
                                        }

                                        else -> {
//                                            LogUtil.e("=== MemberDeviceChk More === $it")
                                        }
                                    }
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                            }
                        }
                    }
                }
            }


            // DeepLink Default Setting
            when (this@MainActivity.viewModel.deepLink) {
                getString(R.string.scheme_host_home) -> {
                    binding.bottomNav.selectedItemId = R.id.item_home // Select Icon
                    bindMenu(MainMenuType.MENU1)
                }

                getString(R.string.scheme_host_magazine) -> {
                    binding.bottomNav.selectedItemId = R.id.item_magazine // Select Icon
                    bindMenu(MainMenuType.MENU2)
                }

                getString(R.string.scheme_host_wallet) -> {
                    if (this@MainActivity.viewModel.isLogin.isEmpty() || this@MainActivity.viewModel.memberId.isEmpty()) {
                        startActivity(getLoginChkActivity(this@MainActivity))
                    } else {
                        binding.bottomNav.selectedItemId = R.id.item_wallet // Select Icon
                        bindMenu(MainMenuType.MENU3)
                    }
                }

                getString(R.string.scheme_host_more) -> {
                    if (this@MainActivity.viewModel.isLogin.isEmpty() || this@MainActivity.viewModel.memberId.isEmpty()) {
                        startActivity(getLoginChkActivity(this@MainActivity))
                    } else {
                        binding.bottomNav.selectedItemId = R.id.item_more // Select Icon
                        bindMenu(MainMenuType.MENU4)
                    }
                }
            }


            // BottomNav Default Setting ( rendering , onSelected )
            // BottomNavigation Selected 메소드 이므로 selectedItemId 는 따로 설정 해줄 필요 없다.
            bottomNav.run {
                setOnItemSelectedListener { item ->
                    when (item.itemId) {
                        R.id.item_home -> {
                            bindMenu(MainMenuType.MENU1)
                        }

                        R.id.item_magazine -> {
                            bindMenu(MainMenuType.MENU2)
                        }

                        R.id.item_wallet -> {
                            if (this@MainActivity.viewModel.isLogin.isEmpty() || this@MainActivity.viewModel.memberId.isEmpty()) {
                                startActivity(getLoginChkActivity(this@MainActivity))
                                return@setOnItemSelectedListener false
                            } else {
                                bindMenu(MainMenuType.MENU3)
                            }
                        }

                        R.id.item_more -> {
                            if (this@MainActivity.viewModel.isLogin.isEmpty() || this@MainActivity.viewModel.memberId.isEmpty()) {
                                startActivity(getLoginChkActivity(this@MainActivity))
                                return@setOnItemSelectedListener false
                            } else {
                                bindMenu(MainMenuType.MENU4)
                            }
                        }
                    }
                    NewVibratorUtil().run {
                        init(this@MainActivity)
                        oneShot(100, 50)
                    }
                    return@setOnItemSelectedListener true
                }
            }


            initPopupAPI()

            coroutineScope.launch(Dispatchers.Main) {
                this@MainActivity.viewModel.popupInfo.collect { vo ->
                    when (vo) {
                        is MainViewModel.PopupUIState.Success -> {
                            LogUtil.d("PopupUI Success : ${vo.popupVo.popupId}")

                            // 오늘은 보지 않기 를 누르지 않았을 경우 메인 진입시 팝업 BtSheet Show
                            if (eventSheet == null) {
                                eventSheet = EventSheet(
                                    this@MainActivity,
                                    vo.popupVo.popupImagePath,
                                    vo.popupVo.popupType,
                                    vo.popupVo.popupLinkType,
                                    vo.popupVo.popupLinkUrl
                                )
                            }

                            vo.popupVo.let {
                                var dismissPopUpDate = PrefsHelper.read("PopupDismiss", "0")

                                if (dismissPopUpDate == "0") {
                                    eventSheet?.show(supportFragmentManager, "메인 팝업")
                                } else {
                                    // 어제 날짜와 오늘 날짜가 같지 않을 경우 show
                                    if (this@MainActivity.viewModel.uiDate != dismissPopUpDate) {
                                        eventSheet?.show(supportFragmentManager, "메인 팝업")
                                    } else { }
                                }
                            }
                        }

                        is MainViewModel.PopupUIState.Failure -> {
//                            LogUtil.e("PopupUI Fail : ${vo.message}")
                        }

                        else -> {
//                            LogUtil.i("PopupUI Loading : $vo")
                        }
                    }
                }
            }
        }



        BackPressedUtil().activityCreate(this@MainActivity, this@MainActivity)
        BackPressedUtil().systemBackPressed(this@MainActivity, this@MainActivity)
    }


    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope

        if (viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
            viewModel.memberDeviceChk()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        BackPressedUtil().activityFinish(this@MainActivity, this@MainActivity)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(LAST_FRAGMENT_TAG, lastFragmentTag)
        super.onSaveInstanceState(outState)
    }

    private fun findMenuByTag(tag: String?): MainMenuType {
        if (tag == null) return MainMenuType.MENU1
        return MainMenuType.values()
            .firstOrNull {
                it.tag == tag
            } ?: MainMenuType.MENU1
    }

    private fun bindMenu(menu: MainMenuType) {
        val fragment = supportFragmentManager.findFragmentByTag(menu.tag) ?: when (menu) {
            MainMenuType.MENU1 -> FragmentHome.newInstance("HOME")
            MainMenuType.MENU2 -> NewFragmentMagazine.newInstance("MAGAZINE")
            MainMenuType.MENU3 -> NewFragmentWallet.newInstance("WALLET")
            MainMenuType.MENU4 -> NewFragmentMore.newInstance("MORE")
        }
        bindAction(fragment, menu.tag)
    }

    private fun bindAction(fragment: Fragment, tag: String) {
        supportFragmentManager.showWithLifecycle(fragment, tag, lastFragmentTag)
        lastFragmentTag = tag
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_main_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        updateResult(true)
    }

    private fun updateResult(isNewIntent: Boolean = false) {
        //true -> notification 으로 갱신된 것, false -> 아이콘 클릭으로 앱이 실행된 것
        (intent.getStringExtra("notificationType") ?: "앱 런처") + if (isNewIntent) "(으)로 갱신했습니다."
        else "(으)로 실행했습니다."
    }

    override fun onBackPressed() {
        when (binding.bottomNav.selectedItemId) {
            R.id.item_home -> fragmentHome?.let { saveFragmentState(it) }
            R.id.item_magazine -> fragmentMagazine?.let { saveFragmentState(it) }
            R.id.item_wallet -> newFragmentWallet?.let { saveFragmentState(it) }
            R.id.item_more -> fragmentMore?.let { saveFragmentState(it) }
        }

        super.onBackPressed()
    }

    private fun saveFragmentState(fragment: Fragment) {
        val fragmentTag = fragment.javaClass.simpleName
        val state = Bundle()
        fragment.onSaveInstanceState(state)
        fragmentStateMap[fragmentTag] = state
    }


    private fun initPopupAPI() {
        coroutineScope.launch {
            val popupAPI = async { getPopup() }
            popupAPI.join()
        }
    }

    private suspend fun getPopup() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            this@MainActivity.viewModel.getPopup()
        }
    }


    private fun FragmentManager.showWithLifecycle(
        fragment: Fragment,
        tag: String,
        lastFragmentTag: String? = null
    ) {
        commitNow {
            val transaction = beginTransaction()

            if (tag != lastFragmentTag) {
                val lastShowFragment = findFragmentByTag(lastFragmentTag)
                if (lastShowFragment != null && lastShowFragment.isVisible) {
                    transaction.hide(lastShowFragment)
                    transaction.setMaxLifecycle(lastShowFragment, Lifecycle.State.STARTED)
                }
            }

            setReorderingAllowed(true)

            if (fragment.isAdded) {
                transaction.show(fragment)
            } else {
                transaction.add(R.id.nav_main_fragment, fragment, tag)
            }
            setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            transaction.commitNow()
        }
    }

    private fun extractStatusCode(errorMessage: String): Int {
        val regex = Regex("""(\d{3})""")
        val matchResult = regex.find(errorMessage)
        return matchResult?.value?.toInt() ?: -1
    }


    private companion object {
        private const val LAST_FRAGMENT_TAG = "LAST_FRAGMENT_TAG"

        fun getLoginChkActivity(context: Context): Intent {
            return Intent(context, LoginChkActivity::class.java)
        }

        // 로그인, 서비스 둘러보기 화면 이동
        fun getIntroActivity(context: Context): Intent {
            val intent = Intent(context, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("another", "another")
            return intent
        }

    }
}

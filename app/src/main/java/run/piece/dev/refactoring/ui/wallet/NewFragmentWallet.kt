package run.piece.dev.refactoring.ui.wallet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.ui.deposit.model.NhBankHistoryState
import run.piece.dev.databinding.NewFragmentWalletBinding
import run.piece.dev.refactoring.ui.deposit.DepositViewModel
import run.piece.dev.refactoring.ui.deposit.NewDepositHistoryActivity
import run.piece.dev.refactoring.ui.deposit.NhAccountNotiBtDlg
import run.piece.dev.refactoring.ui.deposit.NhChargeBtDlg
import run.piece.dev.refactoring.ui.deposit.NhHistoryViewModel
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.purchase.NewPurchaseDetailActivity
import run.piece.dev.refactoring.ui.purchase.NewPurchaseListRvAdapter
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.FragmentLifecycleOwner
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.bank.BankSelectActivity
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.view.withdrawal.WithdrawalActivity
import run.piece.dev.widget.extension.SnackBarCommon
import run.piece.dev.widget.utils.ImgRotateAnimation
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.deposit.model.PurchaseVoV2

@AndroidEntryPoint
class NewFragmentWallet : Fragment(R.layout.new_fragment_wallet) {
    protected val visibleLifecycleOwner: FragmentLifecycleOwner by lazy {
        FragmentLifecycleOwner()
    }
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var mainActivity: MainActivity
    private val dvm: DepositViewModel by viewModels()
    private val nhHistoryViewModel: NhHistoryViewModel by viewModels()
    private var _binding: NewFragmentWalletBinding? = null
    private val binding get() = _binding ?: NewFragmentWalletBinding.inflate(layoutInflater).also { _binding = it }

    private var nhAccountBtDlg: NhAccountNotiBtDlg? = null
    private var nhChargeBtDlg: NhChargeBtDlg? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = NewFragmentWalletBinding.inflate(inflater, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.fragment = this@NewFragmentWallet
        _binding?.depositViewModel = dvm

        App()

        val networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(viewLifecycleOwner) { isConnected ->
            if (!isConnected) startActivity(getNetworkActivity(requireContext()))
        }

        nhAccountBtDlg = NhAccountNotiBtDlg(requireContext())
        nhChargeBtDlg = NhChargeBtDlg(requireContext(), "Wallet")


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coroutineScope = viewLifecycleOwner.lifecycleScope

        binding.apply {
            layout.setPadding(0, getStatusBarHeight(context = requireContext()), 0, 0)
            setStatusBarIconColor()
            requireActivity().window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

            initView()

            viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                @SuppressLint("SetTextI18n")
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME,
                        Lifecycle.Event.ON_PAUSE -> {
                            if (isHidden) {
                                // Fragment Wallet Hidden
                                visibleLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                            } else {
                                // Fragment Wallet Not Hidden

                                // 다른기기 로그아웃 처리
                                coroutineScope.launch(Dispatchers.Main) {
                                    try {
                                        this@NewFragmentWallet.dvm.deviceChk.collect {
                                            when (it) {
                                                is DepositViewModel.MemberDeviceState.Success -> {}
                                                is DepositViewModel.MemberDeviceState.Failure -> {
                                                    val statusCode = extractStatusCode(it.message)
                                                    if (statusCode != 406) {
                                                        PrefsHelper.removeKey("inputPinNumber")
                                                        PrefsHelper.removeKey("memberId")
                                                        PrefsHelper.removeKey("isLogin")
                                                        startActivity(getIntroActivity(requireContext()))
                                                        BackPressedUtil().activityFinish(mainActivity, mainActivity)
                                                    }
                                                }

                                                else -> {}
                                            }
                                        }
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }


                                val memberAccountBundle = Bundle()
                                // 계좌 정보 조회
                                coroutineScope.launch(Dispatchers.Main) {
                                    this@NewFragmentWallet.dvm.memberInfo.collect { vo ->
                                        when (vo) {
                                            is DepositViewModel.MemberInfoState.Success -> {
                                                // 등록된 가상 계좌 없음
                                                if (vo.memberVo.vran.isEmpty() || vo.memberVo.vran.isBlank()) {
                                                    with(binding) {

                                                        rotateLayout.visibility = View.GONE
                                                        emptyAccountLayout.visibility = View.VISIBLE
                                                        accountLayout.visibility = View.GONE

                                                        (btnLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                                                            topToBottom = emptyAccountLayout.id
                                                            btnLayout.requestLayout()
                                                        }

                                                        // 등록된 계좌가 없음. 계좌 등록 Flow 시작
                                                        emptyAccountLayout.onThrottleClick {
                                                            if (!nhAccountBtDlg!!.isAdded) {
                                                                nhAccountBtDlg?.show(childFragmentManager, getString(R.string.nh_bt_sheet_btn_txt))
                                                                nhAccountBtDlg?.setCallback(object : NhAccountNotiBtDlg.OnSendFromBottomSheetDialog {
                                                                    override fun sendValue() {
                                                                        mainActivity.startActivity(getBankSelectActivity(requireContext(), vranNo = ""))
                                                                        nhAccountBtDlg?.dismiss()
                                                                    }
                                                                })
                                                            }
                                                        }

                                                        // 출금하기 버튼
                                                        withdrawBtn.onThrottleClick {
                                                            if (!nhAccountBtDlg!!.isAdded) {
                                                                nhAccountBtDlg?.show(
                                                                    childFragmentManager,
                                                                    getString(R.string.nh_bt_sheet_btn_txt)
                                                                )
                                                                nhAccountBtDlg?.setCallback(object :
                                                                    NhAccountNotiBtDlg.OnSendFromBottomSheetDialog {
                                                                    override fun sendValue() {
                                                                        mainActivity.startActivity(getBankSelectActivity(requireContext(), vranNo = ""))
                                                                        nhAccountBtDlg?.dismiss()
                                                                    }
                                                                })
                                                            }
                                                        }

                                                        // 입금하기 버튼
                                                        chargeBtn.onThrottleClick {
                                                            if (!nhAccountBtDlg!!.isAdded) {
                                                                nhAccountBtDlg?.show(
                                                                    childFragmentManager,
                                                                    getString(R.string.nh_bt_sheet_btn_txt)
                                                                )
                                                                nhAccountBtDlg?.setCallback(object :
                                                                    NhAccountNotiBtDlg.OnSendFromBottomSheetDialog {
                                                                    override fun sendValue() {
                                                                        mainActivity.startActivity(getBankSelectActivity(requireContext(), vranNo = ""))
                                                                        nhAccountBtDlg?.dismiss()
                                                                    }
                                                                })
                                                            }
                                                        }

                                                        // 거래내역 버튼
                                                        historyBtn.onThrottleClick {
                                                            startActivity(getDepositHistoryActivity(requireContext(),""))
                                                        }
                                                    }
                                                }

                                                // 등록된 가상 계좌 있음
                                                else {
                                                    with(binding) {
                                                        rotateLayout.visibility = View.VISIBLE
                                                        rotateLayout.onThrottleClick {
                                                            getNhHistoryResponse("Y")
                                                        }

                                                        memberAccountBundle.putString("vranNo", vo.memberVo.vran)

                                                        // 출금하기 버튼
                                                        withdrawBtn.onThrottleClick {
                                                            startActivity(getWithdrawalActivity(requireContext()))
                                                        }

                                                        // 거래내역 버튼
                                                        historyBtn.onThrottleClick {
                                                            startActivity(getDepositHistoryActivity(requireContext(),vo.memberVo.vran))
                                                        }

                                                        launch(Dispatchers.Main) {
                                                            this@NewFragmentWallet.dvm.getAccountInfo.collect { accountVo ->
                                                                when (accountVo) {
                                                                    is DepositViewModel.GetMemberAccountState.Success -> {
                                                                        emptyAccountLayout.visibility = View.GONE
                                                                        accountLayout.visibility = View.VISIBLE

                                                                        (btnLayout.layoutParams as ConstraintLayout.LayoutParams).apply {
                                                                            topToBottom = accountLayout.id
                                                                            btnLayout.requestLayout()
                                                                        }

                                                                        // 계좌 등록 / 변경
                                                                        accountLayout.onThrottleClick {
                                                                            mainActivity.startActivity(getBankSelectActivity(requireContext(), vranNo = vo.memberVo.vran))
                                                                        }


                                                                        // 등록한 계좌 번호 스키마 처리
                                                                        vranTv.text = accountVo.data.bankName + " " + dvm.accountScheme(accountVo.data.accountNo)

                                                                        // 등록한 계좌 은행 아이콘 변환
                                                                        dvm.getBankIconInit(requireContext(), accountVo.data.bankCode, bankIconIv)

                                                                        // 입금 Flow
                                                                        chargeBtn.onThrottleClick {
                                                                            memberAccountBundle.putString("bankCode", accountVo.data.bankCode)
                                                                            memberAccountBundle.putString("bankName", accountVo.data.bankName)
                                                                            memberAccountBundle.putString("accountNo", accountVo.data.accountNo)
                                                                            nhChargeBtDlg?.show(childFragmentManager, getString(R.string.nh_charge_bt_dlg_tag))
                                                                            nhChargeBtDlg?.arguments = memberAccountBundle
                                                                            nhChargeBtDlg?.setCallback(object : NhChargeBtDlg.OnSendFromBottomSheetDialog {
                                                                                override fun sendValue() {
                                                                                    getNhHistoryResponse("Y")
                                                                                    nhChargeBtDlg?.dismiss()
                                                                                }
                                                                            })
                                                                        }


                                                                    }

                                                                    is DepositViewModel.GetMemberAccountState.Failure -> {}
                                                                    else -> {}
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            is DepositViewModel.MemberInfoState.Failure -> {}
                                            else -> {}
                                        }
                                    }
                                }

                                coroutineScope.launch(Dispatchers.Main) {
                                    this@NewFragmentWallet.dvm.depositBalance.collect { depositBalanceVo ->
                                        when (depositBalanceVo) {
                                            is DepositViewModel.DepositBalanceState.Success -> {
                                                LogUtil.e("depositBalanceVo : " + depositBalanceVo.data.depositBalance.decimalComma())
                                                if (depositBalanceVo.data.depositBalance.toInt() == 0) {
                                                    binding.depositTv.text = "0원"
                                                } else {
                                                    binding.depositTv.text = depositBalanceVo.data.depositBalance.decimalComma() + "원"
                                                }
                                            }

                                            is DepositViewModel.DepositBalanceState.Failure -> {}
                                            else -> {}
                                        }
                                    }
                                }

                                coroutineScope.launch(Dispatchers.Main) {
                                    this@NewFragmentWallet.dvm.purchaseListV2.collect { vo ->
                                        when (vo) {
                                            is DepositViewModel.DepositPurchaseV2State.Success -> {
                                                updateUIPurchaseList(vo.data)
                                            }

                                            is DepositViewModel.DepositPurchaseV2State.Failure -> {
                                                LogUtil.v("내 증권 목록 조회 Fail ${vo.message}")
                                            }

                                            else -> {
                                                binding.loadingIv.visibility = View.VISIBLE
                                                LogUtil.v("내 증권 목록 조회 Loading")
                                            }
                                        }
                                    }
                                }


                                // Swipe Refresh
                                refreshLayout.setOnRefreshListener {
                                    refreshLayout.isRefreshing = false
                                    initView()
                                    getNhHistoryResponse("Y")
                                }


                                // NH 입금 확인 조회
                                coroutineScope.launch(Dispatchers.Main) {
                                    getNhHistoryResponse("N")
                                }
                            }
                        }

                        else -> {
                            visibleLifecycleOwner.handleLifecycleEvent(event = event)
                        }
                    }
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = visibleLifecycleOwner.lifecycleScope
    }


    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else 0
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

    private fun initView() {
        coroutineScope.launch {
            val loadingUI = async { loadingView() }
            val memberDeviceChkAPI = async { deviceChk() }
            val getMemberAPI = async { getMemberData() }
            val getAccountAPI = async { getMemberAccount() }
            val getDepositAPI = async { getDepositBalance() }
            val nhDepositHistoryAPI = async { getNhBankHistory() }
            val getMemberPurchaseAPI = async { getMemberPurchase() }

            loadingUI.join()
            memberDeviceChkAPI.await()
            getMemberAPI.await()
            getAccountAPI.await()
            getDepositAPI.await()
            nhDepositHistoryAPI.await()
            getMemberPurchaseAPI.await()
        }
    }


    // 로딩 View
    private suspend fun loadingView() {
        binding.loadingIv.visibility = View.VISIBLE
        delay(500)
        binding.loadingIv.visibility = View.GONE
    }

    // 회원 디바이스 체크
    private suspend fun deviceChk() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            dvm.memberDeviceChk()
        }
    }

    // 회원 정보 조회 API
    private suspend fun getMemberData() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            dvm.getMemberData()
        }
    }

    // 회원 계좌 정보 조회 API
    private suspend fun getMemberAccount() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            dvm.getAccountInfo()
        }
    }

    private suspend fun getDepositBalance() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            dvm.getDepositBalance()
        }
    }

    // NH 입금 확인 조회
    private suspend fun getNhBankHistory() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            nhHistoryViewModel.nhBankHistory()
        }
    }

    private fun getNhHistoryResponse(refreshStatus: String) {
        coroutineScope.launch(Dispatchers.Main) {
            this@NewFragmentWallet.nhHistoryViewModel.nhHistory.collect { nhVo ->
                when (nhVo) {
                    is NhBankHistoryState.IsLoading -> {}
                    is NhBankHistoryState.Success -> {
                        if (refreshStatus == "Y") {
                            rotationAngle += 180f
                            ImgRotateAnimation.rotateImageAnim(
                                binding.rotateIv,
                                rotationAngle,
                                started = true
                            )

                            val nhSuccessDlg = SnackBarCommon(
                                binding.root,
                                getString(R.string.custom_snackbar_type_refresh_txt),
                                getString(R.string.custom_snackbar_type_refresh)
                            )
                            delay(700)
                            nhSuccessDlg.show(95)
                        }
                    }

                    is NhBankHistoryState.Failure -> {
                        if (refreshStatus == "Y") {
                            rotationAngle += 180f
                            ImgRotateAnimation.rotateImageAnim(
                                binding.rotateIv,
                                rotationAngle,
                                started = true
                            )

                            val nhErrorDlg = SnackBarCommon(
                                binding.root,
                                getString(R.string.nh_api_not_call),
                                getString(R.string.nh_deposit_refresh_txt)
                            )
                            delay(700)
                            nhErrorDlg.show(70)


                            nhErrorDlg.setItemClickListener(object : SnackBarCommon.OnItemClickListener {
                                override fun onClick(v: View) {
                                    getNhHistoryResponse("Y")
                                }
                            })
                        }
                    }

                    is NhBankHistoryState.Empty -> {
                        if (refreshStatus == "Y") {
                            rotationAngle += 180f
                            ImgRotateAnimation.rotateImageAnim(
                                binding.rotateIv,
                                rotationAngle,
                                started = true
                            )

                            val nhSuccessDlg = SnackBarCommon(
                                binding.root,
                                getString(R.string.custom_snackbar_type_refresh_txt),
                                getString(R.string.custom_snackbar_type_refresh)
                            )
                            delay(700)
                            nhSuccessDlg.show(95)
                        }
                    }


                    else -> {}
                }
                this.coroutineContext.job.cancel()

            }
        }
    }

    // 회원 구매 목록 조회 API
    private suspend fun getMemberPurchase() {
        delay(100)
        coroutineScope.launch(Dispatchers.IO) {
            dvm.getDepositPurchaseV2("v0.0.2")
        }
    }

    private fun updateUIPurchaseList(vo: List<PurchaseVoV2>) {
        binding.apply {
            coroutineScope.launch {
                delay(300)
                loadingIv.visibility = View.GONE

                // 소유한 조각이 없을때
                if (vo.isEmpty()) {
                    purchaseRv.visibility = View.GONE
                    purchaseItemGroup.visibility = View.VISIBLE
                    purchaseItemEmptyIv.visibility = View.VISIBLE
                    purchaseItemEmptyTv.visibility = View.VISIBLE
                }

                // 소유한 조각이 있을때
                else {
                    purchaseRv.visibility = View.VISIBLE
                    purchaseItemGroup.visibility = View.GONE
                    purchaseItemEmptyIv.visibility = View.GONE
                    purchaseItemEmptyTv.visibility = View.GONE

                    val purchaseRvAdapter = NewPurchaseListRvAdapter(requireContext())
                    purchaseRv.run {
                        adapter = purchaseRvAdapter
                        this.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
                        itemAnimator = null

                        purchaseRvAdapter.submitList(vo)

                        purchaseRvAdapter.setOnItemClickListener(object : NewPurchaseListRvAdapter.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int?, purchaseId: String?, memberId: String?) {
                                startActivity(
                                    newPurchaseActivity(
                                        requireContext(),
                                        purchaseId = purchaseId.default(),
                                        memberId = memberId.default(),
                                        position = position.default(),
                                    )
                                )
                            }
                        })
                    }
                }
            }
        }
    }


    companion object {
        // refresh Icon Rotate 각도 초기화
        private var rotationAngle = 0f

        fun newInstance(title: String) : NewFragmentWallet {
            return NewFragmentWallet().apply {
                arguments = bundleOf("title" to title)
            }
        }


        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }


        fun getIntroActivity(context: Context): Intent {
            val intent = Intent(context, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("another", "another")
            return intent
        }

        fun getBankSelectActivity(context: Context, vranNo: String): Intent {
            val intent = Intent(context, BankSelectActivity::class.java)
            intent.putExtra("vranNo", vranNo)
            return intent
        }


        fun getWithdrawalActivity(context: Context): Intent {
            return Intent(context, WithdrawalActivity::class.java)
        }

        fun getDepositHistoryActivity(context: Context,vranNo: String) : Intent {
            val intent = Intent(context, NewDepositHistoryActivity::class.java)
            intent.putExtra("vranNo", vranNo)
            return intent
        }

        fun newPurchaseActivity(context: Context, purchaseId: String, memberId: String, position: Int) : Intent {
            val intent = Intent(context, NewPurchaseDetailActivity::class.java)
            intent.run {
                putExtra("purchaseId", purchaseId)
                putExtra("memberId", memberId)
                putExtra("position", position)
            }
            return intent
        }

    }
}
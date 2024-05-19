package run.piece.dev.refactoring.ui.address

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.NewSlideupAddressDetailBinding
import run.piece.dev.refactoring.AppConfirmDF
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.model.request.NotificationModel
import run.piece.domain.refactoring.member.model.request.UpdateConsentItemModel

class NewAddressDetailBtDlg(
    private val context: Context,
    private val zipNo: String,
    private val roadAddr: String,
    private val jibunAddr: String

) : BottomSheetDialogFragment() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: NewSlideupAddressDetailBinding
    private lateinit var viewModel: AddressViewModel
    private var resultJob: Job? = null
    private var listener: BottomSheetListener? = null

    val sendConsentList = ArrayList<UpdateConsentItemModel>()
    var detailAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(requireActivity())[AddressViewModel::class.java]

        binding = NewSlideupAddressDetailBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = this@NewAddressDetailBtDlg
            viewModel = viewModel
            loading.visibility = View.GONE

            addressSearchEt.text = null
            closeTouchLayout.onThrottleClick {
                dismiss()
            }

            LogUtil.v("전달받은 주소 값 : $zipNo | $roadAddr | $jibunAddr")
        }

        coroutineScope = viewLifecycleOwner.lifecycleScope
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;

        listener = getContext() as BottomSheetListener?

        binding.addressSearchEt.addTextChangedListener { edit ->
            if(edit?.length == 0) {
                detailAddress = ""
            } else {
                detailAddress = edit?.toString().orEmpty()
            }
        }

        coroutineScope.launch {
            launch(Dispatchers.Main) {
                binding.roadAddressTv.text = roadAddr.default()
                binding.jibunAddressTv.text = jibunAddr.default()
            }

            launch(Dispatchers.IO) {
                this@NewAddressDetailBtDlg.viewModel.getMemberData()
                this@NewAddressDetailBtDlg.viewModel.getConsentMemberTermsList()
            }

            launch(Dispatchers.Main) {
                sendConsentList.clear()

                this@NewAddressDetailBtDlg.viewModel.consentMemberTerms.collect { vo ->
                    when (vo) {
                        is AddressViewModel.ConsentMemberTermsState.Success -> {
                            if (vo.termsMemberVo.required.consent.isNotEmpty()) {
                                vo.termsMemberVo.required.consent.forEach { data ->
                                    sendConsentList.add(UpdateConsentItemModel(
                                        memberId = viewModel.memberId,
                                        consentCode = data.consentCode,
                                        isAgreement = data.isAgreement))
                                }

                                sendConsentList.add(
                                    UpdateConsentItemModel(
                                        memberId = viewModel.memberId,
                                        consentCode = "CON1501",
                                        isAgreement = PrefsHelper.read("CON1501","N")
                                    )
                                )
                            }
                        }

                        is AddressViewModel.ConsentMemberTermsState.Failure -> {}
                        else -> {}
                    }
                }
            }
        }

        binding.confirmBtn.onThrottleClick {
            updateAddress()
        }


    }

    override fun onResume() {
        super.onResume()
        coroutineScope = viewLifecycleOwner.lifecycleScope
        detailAddress = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }
    private fun updateAddress() {

        coroutineScope.launch(Dispatchers.Main) {
            this@NewAddressDetailBtDlg.viewModel.memberInfo.collect { vo ->
                when (vo) {
                    is AddressViewModel.MemberInfoState.Success -> {

                        val notificationModel = NotificationModel(
                            memberId = viewModel.memberId,
                            "Y",
                            "Y",
                            "Y",
                            "Y",
                            PrefsHelper.read("isAd","N"),
                            PrefsHelper.read("isNotice","N")
                        )

                        val memberModel = MemberModifyModel(
                            memberId = viewModel.memberId,
                            name = vo.memberVo.name,
                            pinNumber = vo.memberVo.pinNumber.default(),
                            cellPhoneIdNo = vo.memberVo.cellPhoneIdNo,
                            birthDay = vo.memberVo.birthDay,
                            cellPhoneNo = vo.memberVo.cellPhoneNo,
                            zipCode = zipNo,
                            baseAddress = roadAddr,
                            detailAddress = detailAddress,
                            ci = vo.memberVo.ci,
                            di = vo.memberVo.di,
                            gender = vo.memberVo.gender,
                            email = vo.memberVo.email,
                            isFido = vo.memberVo.isFido,
                            notification = notificationModel,
                            consents = sendConsentList
                        )

                        launch(Dispatchers.IO) {
                            viewModel.putMember(memberModel = memberModel)
                        }

                        launch(Dispatchers.Main) {
                            Log.v("최종 상세주소 : ", detailAddress)
                            val appConfirmDF = AppConfirmDF.newInstance(
                                "주소 등록 완료",
                                "주소가 성공적으로 등록되었어요",
                                false,
                                R.string.confirm,
                                positiveAction = {
                                    listener?.onButtonClick(
                                        roadAddr,
                                        detailAddress
                                    )
                                    dismiss()
                                },
                                dismissAction = {}
                            )
                            appConfirmDF.show(
                                childFragmentManager,
                                "Address"
                            )
                        }
                    }
                    is AddressViewModel.MemberInfoState.Failure -> {}
                    else -> {}
                }
            }
        }
    }


    interface BottomSheetListener {
        fun onButtonClick(first: String?, end: String?)
    }

}

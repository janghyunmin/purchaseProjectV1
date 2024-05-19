package run.piece.dev.refactoring.ui.join.dialog

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.BdfPhoneSelectBinding
import run.piece.dev.databinding.BdfSmsBinding
import run.piece.dev.databinding.BdfTermsSelectBinding
import run.piece.dev.refactoring.base.BaseBDF
import run.piece.dev.refactoring.ui.consent.NewConsentRvAdapter
import run.piece.dev.refactoring.ui.join.CarrierRvAdapter
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.consent.model.ConsentVo


class JoinBDF : BaseBDF() {
    private var smsTimer: CountDownTimer? = null
    private var timerChk = false

    val smsErrorLiveData: MutableLiveData<String> = MutableLiveData()
    private val editTextContent: MutableLiveData<String> = MutableLiveData()

    var webLink = ""
    var isAllChecked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return when (arguments?.getString("viewType")) {
            "phone", "terms" -> { // 키보드 조정 옵션을 설정하고 전화번호 또는 약관 선택 레이아웃 로드
                updateBdfStyle(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                inflater.inflate(if (arguments?.getString("viewType") == "phone") R.layout.bdf_phone_select else R.layout.bdf_terms_select, container, false)
            }
            "sms" -> { // 키보드 조정 옵션을 설정하고 SMS 입력 레이아웃 로드
                updateBdfStyle(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                inflater.inflate(R.layout.bdf_sms, container, false)
            }
            else -> null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (arguments?.getString("viewType")) {
            "phone" -> { // 전화번호 선택 화면 초기화
                BdfPhoneSelectBinding.bind(view).apply {
                    dialog = this@JoinBDF
                    lifecycleOwner = this@JoinBDF

                    val carrierRvAdapter = CarrierRvAdapter(
                        context = requireActivity(),
                        checkedEvent = { carrier, code ->
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(300)
                                dismiss()
                            }

                            selectValue?.invoke(carrier, code)
                        }
                    ).apply {
                        submitList(
                            mutableListOf(
                                hashMapOf("carrier" to getString(R.string.agency_01), "code" to "01", "isChecked" to false),
                                hashMapOf("carrier" to getString(R.string.agency_02), "code" to "02", "isChecked" to false),
                                hashMapOf("carrier" to getString(R.string.agency_03), "code" to "03", "isChecked" to false),
                                hashMapOf("carrier" to getString(R.string.agency_04), "code" to "04", "isChecked" to false),
                                hashMapOf("carrier" to getString(R.string.agency_05), "code" to "05", "isChecked" to false),
                                hashMapOf("carrier" to getString(R.string.agency_06), "code" to "06", "isChecked" to false)
                            )
                        )
                    }

                    carrierRv.layoutManager = LinearLayoutManager(requireContext()).apply {
                        orientation = LinearLayoutManager.VERTICAL

                    }
                    carrierRv.setHasFixedSize(true)
                    carrierRv.itemAnimator = null
                    carrierRv.adapter = carrierRvAdapter
                }
            }

            "terms" -> {  // 약관 동의 화면 초기화
                arguments?.getString("webLink")?.let {
                    webLink = it
                }

                val consentAdapter = NewConsentRvAdapter(requireActivity(), webLink = webLink)
                val requiredList: ArrayList<ConsentVo> = ArrayList()
                val consentList: ArrayList<ConsentVo> = ArrayList()

                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arguments?.getParcelableArrayList("consentList", ConsentVo::class.java)
                } else arguments?.getParcelableArrayList("consentList")

                data?.let { it ->
                    consentAdapter.submitList(it)
                }

                BdfTermsSelectBinding.bind(view).apply {
                    dialog = this@JoinBDF
                    lifecycleOwner = this@JoinBDF

                    consentRv.adapter = consentAdapter
                    consentRv.itemAnimator = null

                    verification = false

                    allCheckBoxLayout.onThrottleClick {
                        requiredList.clear()
                        consentList.clear()

                        if (!isAllChecked) {
                            verification = true
                            isAllChecked = true
                            termsCv.isSelected = true // 버튼 클릭
                            Glide.with(requireContext()).load(R.drawable.ic_x28_check_circle_10cfc9).into(allCheckBoxIv)

                            requiredList.addAll(consentAdapter.currentList.filter { it.isMandatory == "Y" })
                            consentList.addAll(consentAdapter.currentList.filter { it.isMandatory == "N" })

                            confirmEvent?.invoke(termsCv, consentAdapter.currentList)

                        } else {
                            verification = false
                            isAllChecked = false
                            termsCv.isSelected = false
                            Glide.with(requireContext()).load(R.drawable.ic_x28_check_circle_dadce3).into(allCheckBoxIv)
                        }
                        consentAdapter.setAllCheckEvent(isAllChecked)
                    }

                    consentAdapter.setOnCheckEvent { position, vo ->
                        if (vo.isMandatory == "Y") { //필수
                            if (vo.isChecked) requiredList.add(vo)
                            else requiredList.remove(vo)
                        } else { //선택
                            if (vo.isChecked) consentList.add(vo)
                            else consentList.remove(vo)
                        }

                        /*isAllChecked = requiredList.size >= 4*/
                        isAllChecked = (requiredList.size + consentList.size >= 5)

                        if (requiredList.size == 4) {
                            if (consentList.isNotEmpty()) { //전체 클릭됨
                                Glide.with(requireContext()).load(R.drawable.ic_x28_check_circle_10cfc9).into(allCheckBoxIv)
                                verification = true
                                termsCv.isSelected = true
                            } else {
                                Glide.with(requireContext()).load(R.drawable.ic_x28_check_circle_dadce3).into(allCheckBoxIv)
                                verification = true
                                termsCv.isSelected = true
                            }
                            confirmEvent?.invoke(termsCv, consentAdapter.currentList)

                        } else if (requiredList.size < 4) {
                            Glide.with(requireContext()).load(R.drawable.ic_x28_check_circle_dadce3).into(allCheckBoxIv)
                            verification = false
                            termsCv.isSelected = false
                        }
                    }
                }
            }

            "sms" -> { // SMS 입력 화면 초기화
                var lastClickedTime = 0L

                BdfSmsBinding.bind(view).apply {
                    dialog = this@JoinBDF
                    lifecycleOwner = this@JoinBDF

                    smsTimer = initTimer(smsTimeTv)
                    smsTimer?.start()

                    smsRetryTv.onThrottleClick {
                        if (SystemClock.elapsedRealtime() - lastClickedTime > 1000) {
                            finishTimer(smsTimer)
                            smsTimeTv.text = ""
                            smsNumberEv.text = null

                            smsTimer = initTimer(smsTimeTv)
                            smsTimer?.start()

                            smsReTryEvent?.invoke(this@JoinBDF)
                        }
                        lastClickedTime = SystemClock.elapsedRealtime()
                    }

                    editTextContent.observe(requireActivity()) { data ->
                        if (data.isEmpty() || data.length != 6) {
                            smsErrorLiveData.value = ""
                            confirmSelected = false
                            confirmCv.onThrottleClick { }
                        } else {
                            if (timerChk) {
                                confirmSelected = true
                                confirmCv.onThrottleClick {
                                    smsNumberEv.clearFocus()
                                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                    inputMethodManager.hideSoftInputFromWindow(smsNumberEv.windowToken, 0)
                                    smsVerificationEvent?.invoke(data)
                                }
                            }
                        }
                    }

                    smsErrorLiveData.observe(requireActivity()) { data ->
                        errorText = data
                    }
                }
            }
            else -> {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        finishTimer(smsTimer)
    }

    // SMS 타이머를 초기화하는 메서드
    private fun initTimer(textView: AppCompatTextView): CountDownTimer {
        return object : CountDownTimer(180 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                var seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                seconds %= 60
                textView.text = String.format("%02d", minutes) + ":" + String.format("%02d", seconds)
                timerChk = true
            }

            override fun onFinish() {
                timerChk = false
                cancel()
            }
        }
    }

    // SMS 타이머를 종료하는 메서드
    private fun finishTimer(timer: CountDownTimer?) {
        timer?.let {
            timer.cancel()
            timer.onFinish()
        }
    }

    // EditText에서 발생한 이벤트에 따라 데이터를 업데이트하는 메서드를 정의합니다.
    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        editTextContent.value = s.toString() // LiveData 값을 업데이트합니다.
    }

    companion object {
        private var selectValue: ((String, String) -> Unit)? = null
        private var confirmEvent: ((CardView, List<ConsentVo>) -> Unit)? = null
        private var smsReTryEvent: ((JoinBDF) -> Unit)? = null
        private var smsVerificationEvent: ((String) -> Unit)? = null

        fun newPhoneInstance(selectValue: ((String, String) -> Unit)? = null): JoinBDF {
            val bdf = JoinBDF()
            this.selectValue = selectValue

            val bundle = Bundle()
            bundle.putString("viewType", "phone")

            bdf.arguments = bundle

            return bdf
        }

        fun newTermsInstance(webLink: String,
                             confirmEvent: ((CardView, List<ConsentVo>) -> Unit)? = null,
                             consentList: List<ConsentVo> = emptyList()
        ): JoinBDF {

            val bdf = JoinBDF()
            this.confirmEvent = confirmEvent

            val bundle = Bundle()
            bundle.putString("viewType", "terms")
            bundle.putString("webLink", webLink)

            if (consentList.isNotEmpty()) {
                bundle.putParcelableArrayList("consentList", consentList as ArrayList<out Parcelable>)
            }

            bdf.arguments = bundle
            return bdf
        }

        fun newSMSInstance(smsReTryEvent: ((JoinBDF) -> Unit)? = null, smsVerificationEvent: ((String) -> Unit)? = null): JoinBDF {

            val bdf = JoinBDF()
            this.smsReTryEvent = smsReTryEvent
            this.smsVerificationEvent = smsVerificationEvent

            val bundle = Bundle()
            bundle.putString("viewType", "sms")

            bdf.arguments = bundle
            return bdf
        }
    }
}

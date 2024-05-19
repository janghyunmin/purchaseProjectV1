package run.piece.dev.refactoring.ui.investment.btsheet

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.InvestmentBtSheetBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.portfolio.PortfolioNewViewModel
import run.piece.dev.refactoring.utils.onThrottleClick

class InvestMentBtSheet(context: Context, result: String) : BottomSheetDialogFragment() {
    private var _binding: InvestmentBtSheetBinding? = null
    private val binding get() = _binding ?: InvestmentBtSheetBinding.inflate(layoutInflater).also { _binding = it }
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var listener: InvestMentBtSheetListener
    private lateinit var viewModel: PortfolioNewViewModel
    private lateinit var dataNexusViewModel: DataNexusViewModel
    private val mContext: Context = context
    private var myResult = result


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InvestmentBtSheetBinding.inflate(inflater, container, false)
        _binding?.lifecycleOwner = this@InvestMentBtSheet

        viewModel = ViewModelProvider(requireActivity())[PortfolioNewViewModel::class.java]
        dataNexusViewModel = ViewModelProvider(requireActivity())[DataNexusViewModel::class.java]

        _binding?.viewModel = viewModel
        _binding?.dataStoreViewModel = dataNexusViewModel

        coroutineScope = viewLifecycleOwner.lifecycleScope
        dialog?.setCanceledOnTouchOutside(false)

        return binding.root
    }

    @SuppressLint("StringFormatMatches")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        coroutineScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                    with(binding) {

                        if(dataNexusViewModel.getFinalVulnerable() == "Y") {
                            myResult = "취약투자자"
                        }

                        val typeFace: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.pretendard_semibold)
                        val resultStr = String.format(requireContext().getString(R.string.investment_bottom_sheet_middle_title, myResult))
                        val spannable = SpannableString(resultStr)
                        val startIndex = resultStr.indexOf(myResult)
                        val endIndex = startIndex + (myResult.length ?: 0)

                        spannable.setSpan(
                            CustomTypefaceSpan(typeFace),
                            startIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        middleTv.text = spannable

                        // 살펴보기 OnClick
                        confirmBtn.onThrottleClick {
                            listener.goPortfolioDetail()
                        }

                        // 다음에 할게요 OnClick
                        nextBtn.onThrottleClick {
                            listener.onDismiss()
                        }
                    }
                }
            }
        }
    }

    class CustomTypefaceSpan(private val typeface: Typeface?) : MetricAffectingSpan() {

        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeface(ds)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeface(paint)
        }

        private fun applyCustomTypeface(paint: Paint) {
            paint.typeface = typeface
        }
    }

    fun setInvestMentBtSheetListener(listener: InvestMentBtSheetListener) {
        this.listener = listener
    }

    interface InvestMentBtSheetListener {
        fun goPortfolioDetail()
        fun onDismiss()
    }
}
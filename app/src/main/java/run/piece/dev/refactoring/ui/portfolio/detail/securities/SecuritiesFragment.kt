package run.piece.dev.refactoring.ui.portfolio.detail.securities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentSecuritiesBinding
import run.piece.dev.refactoring.utils.toDateTimeFormat
import run.piece.dev.refactoring.utils.toDecimalComma
import run.piece.domain.refactoring.portfolio.model.PortfolioStockItemVo

@AndroidEntryPoint
class SecuritiesFragment: Fragment(R.layout.fragment_securities) {
    private var _binding: FragmentSecuritiesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SecuritiesViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSecuritiesBinding.bind(view)
        _binding?.lifecycleOwner = this

        binding.apply {
            arguments?.let {
                val data = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable("data", PortfolioStockItemVo::class.java)
                } else it.getParcelable("data")

                data?.let { vo ->

                    onePriceTv.text = "${vo.faceValue.toDecimalComma()}원(1주당)"
                    recruitmentAmountTv.text = "${vo.recruitmentAmount.toDecimalComma()}원"
                    totalAmountTv.text = "${vo.totalPiece.toString().toDecimalComma()}주"
                    dateTimeTv.text = "${vo.recruitmentBeginDate.toDateTimeFormat("month")}부터\n${vo.recruitmentEndDate.toDateTimeFormat("month")}까지"
//                    val format = "yy년 M월 d일 HH:mm"
//                    dateTimeTv.text = "${vo.recruitmentBeginDate.outputDateFormat(format)}부터\n${vo.recruitmentEndDate.outputDateFormat(format)}까지"

                    stockRowTv.text = vo.stockDvnName
                    methodRowTv.text = vo.recruitmentMethod

                    if (vo.stockOperatePeriod.isEmpty()) {
                        securitiesTitleTv.text = "1년 후 만기되는\n${vo.stockDvnName}이에요"
                    } else securitiesTitleTv.text = "${vo.stockOperatePeriod}후 만기되는\n${vo.stockDvnName}이에요"
                }

                it.getString("recruitmentState")?.let { state ->
                    if (state == "PRS0108" || state == "PRS0111") { // 증권 만기 이후
                        dividendHideGroup.visibility = View.GONE
                        stockRow.visibility = View.VISIBLE

                        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0,0,0,0)
                            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                            topToBottom = securitiesIv.id
                        }

                        tabLayout.layoutParams = layoutParams
                    } else {
                        dividendHideGroup.visibility = View.VISIBLE
                        stockRow.visibility = View.GONE
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(data: PortfolioStockItemVo, recruitmentState: String) : SecuritiesFragment{
            val fragment = SecuritiesFragment()
            val bundle = Bundle().apply {
                putParcelable("data", data)
                putString("recruitmentState", recruitmentState)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
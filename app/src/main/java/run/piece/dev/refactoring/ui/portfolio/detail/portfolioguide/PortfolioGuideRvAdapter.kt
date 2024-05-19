package run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.databinding.RvPortfolioGuideEventItemBinding
import run.piece.dev.databinding.RvPortfolioGuideProtectionItemBinding
import run.piece.dev.databinding.RvPortfolioGuideWarningItemBinding
import run.piece.dev.refactoring.ui.faq.FaqTabActivity
import run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide.disclosure.DisclosureTabActivity
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.utils.ToggleAnimation

class PortfolioGuideRvAdapter(private val context: Context, private var portfolioId: String): ListAdapter<GuideItem, RecyclerView.ViewHolder>(diffUtil) {
    private val WARNING = 0
    private val PROTECTION = 1
    private val EVENT = 2

    private lateinit var warningVH: WarningVH
    private lateinit var protectionVH: ProtectionVH
    private lateinit var eventVH: EventVH

    val warningList = arrayListOf (
        WarningItem(
            true,
            "사업위험",
            "❶ 보관위험",
            "현물에 대한 소유 권리를 지분으로 나누어 판매하는 사업 형태에 따라 현물 보관에 따른 기초자산에 손・망실, 분실 및 도난 등의 위험이 있을 수 있습니다.",
            "❷ 투자손실",
            "투자계약 증권은 신종 증권으로 한국거래소에 상장되어 있지 않은 비정형 증권의 형태이므로 이에 따른 증권의 환금성에 큰 제약과 회수금액에 대한 일부 또는 전부를 회수할 수 없는 위험이 있습니다.",
            "❸ 법률규제",
            "자본시장과 금융투자업에 관한 법률 등 금융 관련 법령의 개정 및 제정에 의해 증권의 형태나 사업의 일부가 영향을 받을 위험이 있습니다."),

        WarningItem(false,
            "회사위험",
            "❶ 재무위험",
            "회사의 운영 및 기술 개발을 위해 적절한 시기에 지속적이고 필요한 만큼의 자본 투입이 이루어지지 못할 경우 회사 운영에 위험 요소가 될 수 있습니다.",
            "❷ 매출위험",
            "초기기업으로 정상적 매출이 발생하고 있지 않으며, 매출이 궤도에 오르기 전까지 일정 기간이 소요될 수 있습니다.",
            "❸ 인적위험",
            "현재의 임직원으로 해당 업무의 수행이 가능하나 사업이 확장되어 가면서 금융 전문 인력 및 기술개발 인력이 필요합니다."),

        WarningItem(
            false,
            "공동사업자위험",
            "❶ 파트너의 신뢰성과 경험",
            "공동사업의 경우에는 파트너의 신뢰성과 경험이 매우 중요합니다. 파트너가 금융시장과 해당 산업에 대한 전문지식과 경험을 가지고 있는 경우에도 위험이 있을 수 있습니다.",
            "❷ 목표 및 투자전략의 일치",
            "공동사업의 파트너와 목표 및 투자전략이 일치하는 경우에도, 투자 기간, 리스크 허용 수준 등 다양한 변수에 의해 실제 성과와 차이가 발생할 수 있습니다.",
            "❸ 재무상태와 신용도 평가",
            "사업 파트너의 재무상태와 신용도가 사업 성과에 큰 영향을 주게 됩니다. 꼼꼼하게 부채 비율 등의 재무상태와 신용평가 등을 검토하여 파트너를 선정하는 경우에도 위험이 있을 수 있습니다."),

        WarningItem(
            false,
            "운영자위험",
            "❶ 운영자의 전문성과 경험",
            "운영자의 전문성과 경험은 매우 중요합니다. 운영자가 금융시장에 대한 지식과 경험을 가지고 있는 경우에도 향후 유사한 구조를 성공적으로 운영할 수 있음을 보증할 수 없습니다.",
            "❷ 조직구조와 리소스",
            "해당 사업을 운영할 수 있는 조직 규모, 전문가와의 협업 등 충분한 리소스가 할당되어 있는지 확인하고 운영하는 경우에도, 실제 성과와 차이가 발생할 수 있습니다.",
            "❸ 내부통제 및 거래 실행 절차",
            "운영자의 내부통제 및 거래 실행 절차를 검토해야 합니다. 운영자가 높은 수준의 내부통제 체계를 구축하고 거래 실행 절차를 준수하는 있는지에 따라 투자 성과의 차이와 위험이 발생할 수 있습니다."),

        WarningItem(
            false,
            "기타위험",
            "❶ 발행일정",
            "청약기간이 연장되거나 투자판단과 밀접하게 연관된 주요내용이 변경될 경우 명시된 일정이 변경될 가능성이 존재합니다.",
            "❷ 과세제도",
            "투자에 따른 손익에 대한 세금 부과 방법이나 기준은 정부의 정책적 판단 등에 의해 변경될 수 있으며, 투자자의 지위에 따라 각기 다른 과세기준이 적용될 수 있습니다. 따라서 과세 관련 사항은 투자자 본인의 재산상태를 고려하시어 반드시 세무전문가의 조언 등 추가적인 확인을 권장합니다.",
            "❸ 집중투자에 따른 위험",
            "소수 종목에 선별적으로 투자되어 해당 실물시장 전체의 위험보다 상대적으로 클 수 있습니다.")
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            EVENT -> {
                eventVH = EventVH(RvPortfolioGuideEventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                eventVH
            }

            WARNING -> {
                warningVH = WarningVH(RvPortfolioGuideWarningItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                warningVH
            }

            PROTECTION -> {
                protectionVH = ProtectionVH(RvPortfolioGuideProtectionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                protectionVH
            }

            else -> {
                throw ClassCastException("Unknown viewType $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
            is WarningVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
            is ProtectionVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            1 -> WARNING
            2 -> PROTECTION
            else -> EVENT
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<GuideItem>(){
            override fun areItemsTheSame(oldItem: GuideItem, newItem: GuideItem): Boolean {
                return oldItem.content == newItem.content
            }

            override fun areContentsTheSame(oldItem: GuideItem, newItem: GuideItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class EventVH(private val binding: RvPortfolioGuideEventItemBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("CheckResult", "SetTextI18n")
        fun bind(data: GuideItem){
            binding.apply {
                item = data

                // 투자 공시 / 투자계약증권 FAQ
                when(layoutPosition) {
                    0 -> {
                        binding.clickTv.text = "공시 보기"
                    }
                    3 -> {
                        binding.clickTv.text = "투자계약증권 FAQ 보기"
                    }
                }

                binding.clickLayout.onThrottleClick {
                    if (layoutPosition == 0) {
                        data.boards?.let {
                            context.startActivity(DisclosureTabActivity.getIntent(context, data.boards!! , portfolioId))
                        } ?: run {
                            context.startActivity(DisclosureTabActivity.getIntent(context, portfolioId))
                        }
                    } else if (layoutPosition == 3) {
                        context.startActivity(FaqTabActivity.getIntent(context))
                    }

                }
            }
        }
    }

    inner class WarningVH(private val binding: RvPortfolioGuideWarningItemBinding): RecyclerView.ViewHolder(binding.root){
        private val guideWarningRvAdapter = GuideWarningRvAdapter(context, binding)

        fun bind(data: GuideItem) {
            val params = binding.contentLayout.layoutParams as ConstraintLayout.LayoutParams

            FlexboxLayoutManager(context).apply {
                flexWrap = FlexWrap.WRAP
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START

            }.let {
                binding.cardItemRv.layoutManager = it
                binding.cardItemRv.adapter = guideWarningRvAdapter
            }

            guideWarningRvAdapter.submitList(warningList)

            Glide.with(context).load(ContextCompat.getDrawable(context, data.image)).into(binding.titleIv)
            binding.titleTv.text = data.title
            binding.titleContentTv.text = data.content

            binding.warningLayout.onThrottleClick {
                TransitionManager.beginDelayedTransition(binding.warningLayout, AutoTransition())

                if (data.expandable) {
                    data.expandable = false
                    ToggleAnimation.collapseAction(binding.contentLayout)
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(310)
                        binding.openLayout.visibility = View.VISIBLE
                        binding.openLayout.animate().apply {
                            duration = 500
                            alpha(1f)
                        }
                    }
                }
                else {
                    data.expandable = true
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(100)
                        binding.openLayout.animate().apply {
                            duration = 150
                            alpha(0f)
                        }
                    }

                    params.topToBottom = binding.titleContentTv.id
                    binding.contentLayout.requestLayout()
                    ToggleAnimation.expandAction(binding.contentLayout)
                }
            }
        }
    }

    inner class ProtectionVH(private val binding: RvPortfolioGuideProtectionItemBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(data: GuideItem){
            val params = binding.contentLayout.layoutParams as ConstraintLayout.LayoutParams

            Glide.with(context).load(ContextCompat.getDrawable(context, data.image)).into(binding.titleIv)
            binding.titleTv.text = data.title
            binding.titleContentTv.text = data.content
            binding.protectionLayout.onThrottleClick {
                TransitionManager.beginDelayedTransition(binding.protectionLayout, AutoTransition())

                if (data.expandable) {
                    data.expandable = false
                    ToggleAnimation.collapseAction(binding.contentLayout)
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(310)
                        binding.openLayout.visibility = View.VISIBLE
                        binding.openLayout.animate().apply {
                            duration = 500
                            alpha(1f)
                        }
                    }
                }
                else {
                    data.expandable = true
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(100)
                        binding.openLayout.animate().apply {
                            duration = 150
                            alpha(0f)
                        }
                    }

                    params.topToBottom = binding.titleContentTv.id
                    binding.contentLayout.requestLayout()
                    ToggleAnimation.expandAction(binding.contentLayout)
                }
            }
        }
    }

}

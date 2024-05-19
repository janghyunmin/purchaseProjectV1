package run.piece.dev.refactoring.ui.portfolio.list

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.portfolio.PortfolioItem
import run.piece.dev.databinding.AchieveBasicBinding
import run.piece.dev.databinding.PortfolioItemBinding
import run.piece.dev.refactoring.ui.investment.btsheet.InvestMentBtSheet
import run.piece.dev.refactoring.ui.newinvestment.InvestmentActivity
import run.piece.dev.refactoring.ui.newinvestment.InvestmentIntroActivity
import run.piece.dev.refactoring.ui.portfolio.PortfolioNewViewModel
import run.piece.dev.refactoring.ui.portfolio.achieveInfo.AchieveInfoAdapter
import run.piece.dev.refactoring.ui.portfolio.detail.PortfolioDetailNewActivity
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.widget.custom.accordion.ChartItem
import run.piece.dev.widget.utils.ToggleAnimation
import java.text.SimpleDateFormat
import java.time.Duration

class NewPortfolioAdapter(
    private val context: Context,
    private val viewModel: PortfolioNewViewModel,
    private val achieveInfos: List<PortfolioItem.AchieveListModel>,
    private val portfolios: ArrayList<PortfolioItem.PortfolioListModel>,
    private val displayType: String,
    private val childFragmentManager: FragmentManager,
    private val result: String,
    private val score: Int,
    private val isVulnerableInvestors: String,
    private val name: String,
    private val webSocketArray: StateFlow<JsonArray?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var achieveInfoAdapter: AchieveInfoAdapter? = null
    private var chartToggle = ChartItem(expandable = false)
    private var rotate = 0F

    companion object {
        private const val VIEW_TYPE_ACHIEVE_INFO = 0
        private const val VIEW_TYPE_PORTFOLIO = 1
    }

    init {
        setHasStableIds(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ACHIEVE_INFO -> AchieveInfoViewHolder(
                AchieveBasicBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_PORTFOLIO -> PortfolioViewHolder(
                PortfolioItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_ACHIEVE_INFO -> {
                val achieveInfo = portfolios[0]
                (holder as AchieveInfoViewHolder).bind(context = context, achieveInfo)
                if (position == 0) {
                    holder.itemView.setOnClickListener {
                        if (chartToggle.expandable) {
                            chartToggle.expandable = false
                            rotate += 180F
                            holder.itemView.findViewById<RecyclerView>(R.id.achieve_rv).visibility = View.VISIBLE
                            ToggleAnimation.rotationAnim(holder.itemView.findViewById(R.id.arrow_down_iv), rotate)
                            ToggleAnimation.alphaAnimIv(context = context, holder.itemView.findViewById(R.id.chart_iv), "VISIBLE")
                            ToggleAnimation.recyclerCollapse(holder.itemView.findViewById(R.id.achieve_rv))
                            ToggleAnimation.moveAnim(
                                context = context,
                                holder.itemView.findViewById<TextView>(R.id.title_tv),
                                "RIGHT",
                                displayType
                            )
                            ToggleAnimation.moveAnim(
                                context = context,
                                holder.itemView.findViewById<TextView>(R.id.sub_title_tv),
                                "RIGHT",
                                displayType
                            )

                            holder.itemView.findViewById<View>(R.id.info_layout)?.visibility = View.GONE
                            holder.itemView.findViewById<View>(R.id.left_arrow)?.visibility = View.GONE
                            holder.itemView.findViewById<View>(R.id.right_arrow)?.visibility = View.GONE
                        } else {
                            chartToggle.expandable = true
                            rotate += 180F
                            holder.itemView.findViewById<RecyclerView>(R.id.achieve_rv).visibility = View.GONE
                            ToggleAnimation.rotationAnim(holder.itemView.findViewById(R.id.arrow_down_iv), rotate)
                            ToggleAnimation.alphaAnimIv(context = context, holder.itemView.findViewById(R.id.chart_iv), "GONE")
                            ToggleAnimation.recyclerExpand(holder.itemView.findViewById(R.id.achieve_rv))
                            ToggleAnimation.moveAnim(
                                context = context,
                                holder.itemView.findViewById<TextView>(R.id.title_tv),
                                "LEFT",
                                displayType
                            )
                            ToggleAnimation.moveAnim(
                                context = context,
                                holder.itemView.findViewById<TextView>(R.id.sub_title_tv),
                                "LEFT",
                                displayType
                            )
                            holder.itemView.findViewById<View>(R.id.info_layout)?.visibility = View.VISIBLE
                            holder.itemView.findViewById<View>(R.id.left_arrow)?.visibility = View.GONE
                            holder.itemView.findViewById<View>(R.id.right_arrow)?.visibility = View.VISIBLE
                        }
                    }
                }
            }

            VIEW_TYPE_PORTFOLIO -> {
                val portfolio = portfolios[position]

                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.responseArray.collect { json ->
                        json?.let {

                            (holder as PortfolioViewHolder).bind(
                                portfolio = portfolio,
                                fragmentManager = childFragmentManager,
                                portfolio.recruitmentState,
                                portfolio.achievementRate,
                                position = position
                            )

                        } ?: run {
                            (holder as PortfolioViewHolder).bind(
                                portfolio = portfolio,
                                fragmentManager = childFragmentManager,
                                portfolio.recruitmentState,
                                portfolio.achievementRate,
                                position = position
                            )
                        }
                    }
                }

                if(portfolio.recruitmentState == "PRS0101") {
                    val targetTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(portfolio.recruitmentBeginDate)?.time ?: return
                    val currentTime = System.currentTimeMillis()
                    val timeDifference = targetTime - currentTime

                    if ((holder as PortfolioViewHolder).countDownTimer == null) {
                        holder.countDownTimer = object : CountDownTimer(timeDifference, 1000) {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onTick(millisUntilFinished: Long) {
                                val duration = Duration.ofMillis(millisUntilFinished)

                                // Duration을 시간, 분, 초로 분해
                                val days = duration.toDays()
                                val hours = duration.minusDays(days).toHours()
                                val minutes = duration.minusDays(days).minusHours(hours).toMinutes()
                                val seconds = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).seconds

                                viewModel.portfolioTimerStatus(days, hours, minutes, seconds)

                                holder.days = days
                                holder.hours = hours
                                holder.minute = minutes
                                holder.seconds = seconds

                            }

                            override fun onFinish() {
                                holder.countDownTimer?.cancel()
                                holder.countDownTimer = null

                                CoroutineScope(Dispatchers.IO).launch {
                                    viewModel.getPortfolio()
                                }

                                CoroutineScope(Dispatchers.Main).launch {
                                    viewModel.responseArray.value?.size()?.let {
                                        this@NewPortfolioAdapter.notifyItemRangeChanged(0, it)
                                    } ?: run {
                                        this@NewPortfolioAdapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        }.start()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return portfolios.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VIEW_TYPE_ACHIEVE_INFO
            position <= portfolios.size -> VIEW_TYPE_PORTFOLIO
            else -> VIEW_TYPE_PORTFOLIO
        }
    }


    inner class AchieveInfoViewHolder(private val binding: AchieveBasicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, achieveInfo: PortfolioItem.PortfolioListModel) {
            binding.apply {
                if (achieveInfoAdapter == null) {
                    achieveInfoAdapter = AchieveInfoAdapter(binding.root.context, achieveData = achieveInfos)

                    achieveRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    achieveRv.setHasFixedSize(true)
                    achieveRv.adapter = achieveInfoAdapter
                }

                achieveRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        // 스크롤 Start Point 도달
                        if (!binding.achieveRv.canScrollHorizontally(-1)) {
                            binding.leftGradient.visibility = View.GONE
                            binding.leftArrow.visibility = View.GONE
                        }
                        // 스크롤 End Point 도달
                        else if (!binding.achieveRv.canScrollHorizontally(1)) {
                            binding.rightGradient.visibility = View.GONE
                            binding.rightArrow.visibility = View.GONE
                        }
                        // 스크롤 진행중
                        else {
                            binding.leftGradient.visibility = View.VISIBLE
                            binding.rightGradient.visibility = View.VISIBLE
                            binding.leftArrow.visibility = View.VISIBLE
                            binding.rightArrow.visibility = View.VISIBLE
                        }
                    }
                })

                achieveInfoAdapter?.achieveData = achieveInfos
                executePendingBindings()
            }
        }
    }

    inner class PortfolioViewHolder(private val binding: PortfolioItemBinding) : RecyclerView.ViewHolder(binding.root) {

        var countDownTimer: CountDownTimer? = null
        var days: Long = 0
        var hours: Long = 0
        var minute: Long = 0
        var seconds: Long = 0

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        fun bind(
            portfolio: PortfolioItem.PortfolioListModel,
            fragmentManager: FragmentManager,
            recruitmentState: String,
            achieveRate: String,
            position: Int
        ) {

            binding.apply {
                val requestOptions = RequestOptions().format(DecodeFormat.PREFER_RGB_565)
                Glide.with(itemView.context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(portfolio.representThumbnailImagePath)
                    .preload()

                Glide.with(itemView.context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(portfolio.representThumbnailImagePath)
                    .transform(CenterCrop(), RoundedCorners(20))
                    .dontAnimate()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.portfolioImg)

                //PRS0101 모집예정
                //RPS0102 모집 중
                //PRS0103 모집마감
                //PRS0104 분배예정
                //PRS0108 분배예정 - 만기
                //PRS0111 분배완료

                CoroutineScope(Dispatchers.Main).launch {
                    portfolioImg.clearColorFilter()
                    portfolioNumberTv.text = portfolio.subTitle
                    portfolioTitle.text = portfolio.title

                    if (viewModel.responseArray.value?.isEmpty == false || viewModel.responseArray.value != null) {
                        viewModel.responseArray.collect { array ->
                            array?.let { data ->
                                if (portfolio.recruitmentState == "PRS0101" || recruitmentState == "PRS0101") {
                                    if(days.toInt() == 0 && hours.toInt() == 0 && minute.toInt() == 0 && seconds.toInt() == 0) {
                                        portfolioStatus.text = "모집 중"
                                        portfolioText.text = "$achieveRate% \n모집되었어요"
                                    } else {
                                        portfolioStatus.text = "모집예정"
                                        portfolioText.text = viewModel.newResultText(
                                            days,
                                            hours,
                                            minute,
                                            seconds
                                        )
                                    }

                                } else if (portfolio.recruitmentState == "PRS0102" || recruitmentState == "PRS0102") {
                                    portfolioStatus.text = "모집 중"
                                    portfolioText.text = "${data[position-1].asJsonObject["achievementRate"].asString}% \n모집되었어요"

                                } else if (portfolio.recruitmentState == "PRS0103" || recruitmentState == "PRS0103") {
                                    portfolioStatus.text = "모집마감"
                                    portfolioText.text = "청약이 \n마감되었어요"
                                } else if (portfolio.recruitmentState == "PRS0104") {
                                    portfolioStatus.text = "분배예정"
                                    portfolioText.text = viewModel.getStatusText(
                                        portfolio.recruitmentBeginDate.default(),
                                        portfolio.achievementRate.default(),
                                        portfolio.recruitmentState.default(),
                                        portfolio.dividendsExpecatationDate.default()
                                    )
                                } else if (portfolio.recruitmentState == "PRS0108") {
                                    portfolioStatus.text = "분배예정"
                                    portfolioImg.clearColorFilter()
                                    portfolioText.text = viewModel.getStatusText(
                                        portfolio.recruitmentBeginDate.default(),
                                        portfolio.achievementRate.default(),
                                        portfolio.recruitmentState.default(),
                                        portfolio.dividendsExpecatationDate.default()
                                    )
                                } else if (portfolio.recruitmentState == "PRS0111") {
                                    portfolioStatus.text = "분배완료"
                                    portfolioImg.clearColorFilter()
                                    portfolioText.text = viewModel.getStatusText(
                                        portfolio.recruitmentBeginDate.default(),
                                        portfolio.achievementRate.default(),
                                        portfolio.recruitmentState.default(),
                                        portfolio.dividendsExpecatationDate.default()
                                    )
                                } else {
                                    portfolioStatus.text = "분배예정"
                                    portfolioImg.clearColorFilter()
                                    portfolioText.text = viewModel.getStatusText(
                                        portfolio.recruitmentBeginDate.default(),
                                        portfolio.achievementRate.default(),
                                        portfolio.recruitmentState.default(),
                                        portfolio.dividendsExpecatationDate.default()
                                    )
                                }
                            } ?: run {
                                when (portfolio.recruitmentState) {
                                    "PRS0102" -> {
                                        portfolioStatus.text = "모집 중"
                                        portfolioText.text = "$achieveRate% \n모집되었어요"
                                    }
                                    "PRS0103" -> {
                                        portfolioStatus.text = "모집마감"
                                        portfolioText.text = "청약이 \n마감되었어요"
                                    }

                                    "PRS0104" -> {
                                        portfolioStatus.text = "분배예정"
                                        portfolioText.text = viewModel.getStatusText(
                                            portfolio.recruitmentBeginDate.default(),
                                            portfolio.achievementRate.default(),
                                            portfolio.recruitmentState.default(),
                                            portfolio.dividendsExpecatationDate.default()
                                        )
                                    }

                                    "PRS0108" -> {
                                        portfolioStatus.text = "분배예정"
                                        portfolioText.text = viewModel.getStatusText(
                                            portfolio.recruitmentBeginDate.default(),
                                            portfolio.achievementRate.default(),
                                            portfolio.recruitmentState.default(),
                                            portfolio.dividendsExpecatationDate.default()
                                        )
                                    }

                                    "PRS0111" -> {
                                        portfolioStatus.text = "분배완료"
                                        portfolioText.text = viewModel.getStatusText(
                                            portfolio.recruitmentBeginDate.default(),
                                            portfolio.achievementRate.default(),
                                            portfolio.recruitmentState.default(),
                                            portfolio.dividendsExpecatationDate.default()
                                        )
                                    }
                                }
                            }
                        }
                    } else {

                        when (portfolio.recruitmentState) {
                            "PRS0102" -> {
                                portfolioStatus.text = "모집 중"
                                portfolioText.text = "$achieveRate% \n모집되었어요"
                            }
                            "PRS0103" -> {
                                portfolioStatus.text = "모집마감"
                                portfolioText.text = "청약이 \n마감되었어요"
                            }

                            "PRS0104" -> {
                                portfolioStatus.text = "분배예정"
                                portfolioText.text = viewModel.getStatusText(
                                    portfolio.recruitmentBeginDate.default(),
                                    portfolio.achievementRate.default(),
                                    portfolio.recruitmentState.default(),
                                    portfolio.dividendsExpecatationDate.default()
                                )
                            }

                            "PRS0108" -> {
                                portfolioStatus.text = "분배예정"
                                portfolioText.text = viewModel.getStatusText(
                                    portfolio.recruitmentBeginDate.default(),
                                    portfolio.achievementRate.default(),
                                    portfolio.recruitmentState.default(),
                                    portfolio.dividendsExpecatationDate.default()
                                )
                            }

                            "PRS0111" -> {
                                portfolioStatus.text = "분배완료"
                                portfolioText.text = viewModel.getStatusText(
                                    portfolio.recruitmentBeginDate.default(),
                                    portfolio.achievementRate.default(),
                                    portfolio.recruitmentState.default(),
                                    portfolio.dividendsExpecatationDate.default()
                                )
                            }
                        }
                    }
                }


                // 청약 상세 진입 CASE
                // CASE1. 투자 성향 진단 여부 판별 "Y" || "N"
                // "N" -> 투자 성향 진단 분석 시작
                // "Y" -> CASE2. 조건 판별

                // CASE2. 취약 투자자 여부 "Y" || "N"
                // "N" -> CASE3. 조건 판별
                // "Y" -> 투자 상품 위험 고지 BottomSheet 출력

                // CASE3. 진단 결과 점수 60점 이상 여부 result < 60 || result >= 60
                // 60점 미만 -> 투자 상품 위험 고지 BottomSheet 출력
                // 60점 이상 -> 상품 상세 진입
                parent.onThrottleClick {
                    // 비회원 , 비로그인의 경우
                    if (viewModel.isLogin.isEmpty() && viewModel.memberId.isEmpty()) {
                        //  청약 상세 바로 진입
                        itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                    }
                    // 로그인을 하였을 경우, 로그인이 되어있는 경우
                    else {
                        Log.i("결과값 : ", "$result")

                        val bottomSheet = InvestMentBtSheet(itemView.context, result)
                        when (portfolio.recruitmentState) {
                            "PRS0101",
                            "PRS0102" -> {
                                // 투자 성향 분석 진단 안한 사용자는 "투자 성향 분석 진단" 으로 이동
                                if (result.isEmpty()) {
                                    itemView.context.startActivity(InvestmentIntroActivity.getIntent(itemView.context, name))
                                }
                                // 투자 성향 분석 진단 진행한 사용자
                                else {
                                    // 성향 분석 완료자이지만 취약 투자자인 경우
                                    if (isVulnerableInvestors == "Y") {
                                        bottomSheet.setInvestMentBtSheetListener(object : InvestMentBtSheet.InvestMentBtSheetListener {
                                            override fun goPortfolioDetail() {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    viewModel.postInvestAgreement(portfolioId = portfolio.portfolioId)
                                                }

                                                CoroutineScope(Dispatchers.Main).launch {
                                                    viewModel.investAgreement.collect {
                                                        when (it) {
                                                            is PortfolioNewViewModel.MemberInvestAgreementState.Success -> {
                                                                itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                                                            }

                                                            is PortfolioNewViewModel.MemberInvestAgreementState.Failure -> {
                                                                LogUtil.e("투자 상품 위험 고지 Fail.. ${it.message}")
                                                            }

                                                            else -> {
                                                                LogUtil.e("투자 상품 위험 고지 Loading.. $it")
                                                            }
                                                        }
                                                    }
                                                }

                                                CoroutineScope(Dispatchers.Main).launch {
                                                    bottomSheet.dismiss()
                                                    delay(300)
                                                    itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                                                    bottomSheet.dismiss()
                                                }
                                            }

                                            override fun onDismiss() {
                                                LogUtil.e("onDismiss ! ")
                                                bottomSheet.dismiss()
                                            }
                                        })
                                        bottomSheet.show(fragmentManager, "투자상품 위험 고지")
                                    }
                                    // 취약 투자자에 해당하지 않지만 투자성향 60점 미만일 경우
                                    else {
                                        if (score < 60) {
                                            bottomSheet.setInvestMentBtSheetListener(object : InvestMentBtSheet.InvestMentBtSheetListener {
                                                override fun goPortfolioDetail() {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        viewModel.postInvestAgreement(portfolioId = portfolio.portfolioId)
                                                    }

                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        viewModel.investAgreement.collect {
                                                            when (it) {
                                                                is PortfolioNewViewModel.MemberInvestAgreementState.Success -> {
                                                                    LogUtil.e("투자 상품 위험 고지 동의 Success.. ${it.isSuccess?.message}")
                                                                    itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                                                                }

                                                                is PortfolioNewViewModel.MemberInvestAgreementState.Failure -> {
                                                                    LogUtil.e("투자 상품 위험 고지 Fail.. ${it.message}")
                                                                }

                                                                else -> {
                                                                    LogUtil.e("투자 상품 위험 고지 Loading.. $it")
                                                                }
                                                            }
                                                        }
                                                    }

                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        bottomSheet.dismiss()
                                                        delay(300)
                                                        itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                                                        bottomSheet.dismiss()
                                                    }
                                                }

                                                override fun onDismiss() {
                                                    LogUtil.e("onDismiss ! ")
                                                    bottomSheet.dismiss()
                                                }
                                            })
                                            bottomSheet.show(fragmentManager, "투자상품 위험 고지")
                                        }
                                        // 취약투자자가 아니며, 점수가 60점 이상인 경우 상세로 바로 진입
                                        else {
                                            itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                                        }
                                    }
                                }
                            }

                            else -> {
                                // 청약 상세 진입
                                itemView.context.startActivity(PortfolioDetailNewActivity.getIntent(itemView.context, portfolio.portfolioId.default()))
                            }
                        }
                    }
                }
                executePendingBindings()
            }
        }
    }


    fun notifyData() {
        notifyItemRangeChanged(1, portfolios.size)
    }
}
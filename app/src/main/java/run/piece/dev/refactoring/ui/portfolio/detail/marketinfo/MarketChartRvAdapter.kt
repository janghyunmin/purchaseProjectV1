package run.piece.dev.refactoring.ui.portfolio.detail.marketinfo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.databinding.ChartDayValueButtonBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.widget.custom.chart.DayValueBarChart
import run.piece.domain.refactoring.portfolio.model.PortfolioMarketInfoVo

class MarketChartRvAdapter(private val context: Context, private val fragment: MarketInfoFragment, private val cardView: CardView): ListAdapter<PortfolioMarketInfoVo, RecyclerView.ViewHolder>(diffUtil) {
    var oldPos = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ChartDayValueButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                getItem(position)?.let {
                    holder.bind(it, fragment)
                }
            }
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<PortfolioMarketInfoVo>(){
            override fun areItemsTheSame(oldItem: PortfolioMarketInfoVo, newItem: PortfolioMarketInfoVo): Boolean {
                return oldItem.priceInfoId == newItem.priceInfoId
            }

            override fun areContentsTheSame(oldItem: PortfolioMarketInfoVo, newItem: PortfolioMarketInfoVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ChartDayValueButtonBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: PortfolioMarketInfoVo, fragment: MarketInfoFragment){
            binding.apply {
                item = data

                if (!data.isClicked) {
                    tapButton.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g300_EAECF0))
                    tapNameTv.setTextColor(ContextCompat.getColor(context, R.color.g700_757983))
                } else {
                    tapButton.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g900_292A2E))
                    tapNameTv.setTextColor(ContextCompat.getColor(context, R.color.white))
                }

                itemView.onThrottleClick {
                    context.resources?.displayMetrics?.widthPixels?.let { width ->
                        DayValueBarChart.newInstance(cardView, data.marketPrices, width)
                    }

                    currentList[oldPos].isClicked = false
                    currentList[layoutPosition].isClicked = true
                    oldPos = layoutPosition

                    fragment.clickedItemPosition(layoutPosition)

                    notifyDataSetChanged()
                }
            }
        }
    }
}

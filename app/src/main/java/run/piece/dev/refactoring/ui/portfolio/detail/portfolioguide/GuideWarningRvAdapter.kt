package run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.databinding.RvPortfolioGuideWarningCardItemBinding
import run.piece.dev.databinding.RvPortfolioGuideWarningItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick

class GuideWarningRvAdapter(private val context: Context, private val parentView: RvPortfolioGuideWarningItemBinding): ListAdapter<WarningItem, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(RvPortfolioGuideWarningCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<WarningItem>(){
            override fun areItemsTheSame(oldItem: WarningItem, newItem: WarningItem): Boolean {
                return oldItem.title == newItem.title
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: WarningItem, newItem: WarningItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: RvPortfolioGuideWarningCardItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: WarningItem) {
            binding.apply {
                itemNameTv.text = data.title

                if (data.isClicked) {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g900_292A2E))
                    itemNameTv.setTextColor(ContextCompat.getColor(context, R.color.white))

                    parentView.oneTitleTv.text = data.oneTitle
                    parentView.oneContentTv.text = data.oneContent
                    parentView.twoTitleTv.text = data.twoTitle
                    parentView.twoContentTv.text = data.twoContent
                    parentView.threeTitleTv.text = data.threeTitle
                    parentView.threeContentTv.text = data.threeContent

                } else {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g300_EAECF0))
                    itemNameTv.setTextColor(ContextCompat.getColor(context, R.color.g700_757983))
                }

                itemView.onThrottleClick {

                    itemNameTv.text = data.title
                    currentList.forEachIndexed { index, warningItem ->
                        currentList[index].isClicked = index == layoutPosition
                        notifyItemChanged(index)
                    }
                }
            }
        }
    }
}
package run.piece.dev.refactoring.ui.portfolio.detail.business

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.RvBusinessItemBinding
import run.piece.domain.refactoring.portfolio.model.PortfolioJoinBizItemVo


class BusinessRvAdapter(private val context: Context): ListAdapter<PortfolioJoinBizItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(RvBusinessItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<PortfolioJoinBizItemVo>(){
            override fun areItemsTheSame(oldItem: PortfolioJoinBizItemVo, newItem: PortfolioJoinBizItemVo): Boolean {
                return oldItem.bizId == newItem.bizId
            }

            override fun areContentsTheSame(oldItem: PortfolioJoinBizItemVo, newItem: PortfolioJoinBizItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: RvBusinessItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: PortfolioJoinBizItemVo){
            Glide
                .with(context)
                .load(data.bizThumbnailPath)
                .placeholder(R.drawable.business_no_image)
                .error(R.drawable.business_no_image)
                .into(binding.titleIv)

            binding.titleTv.text = data.bizName

            if (data.portfolioJoinBizDetails.isNotEmpty()) {
                data.portfolioJoinBizDetails[0]?.let { item ->
                    binding.contentTv.text = item.description
                }
            }
        }
    }
}
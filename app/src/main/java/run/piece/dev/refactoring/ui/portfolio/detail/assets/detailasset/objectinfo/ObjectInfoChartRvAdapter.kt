package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.ObjectInfoChartRvItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick

class ObjectInfoChartRvAdapter(private val context: Context): ListAdapter<ObjectInfoItem, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ObjectInfoChartRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<ObjectInfoItem>(){
            override fun areItemsTheSame(oldItem: ObjectInfoItem, newItem: ObjectInfoItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: ObjectInfoItem, newItem: ObjectInfoItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ObjectInfoChartRvItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ObjectInfoItem){
            binding.apply {
                itemColorCardView.setCardBackgroundColor(ContextCompat.getColor(context, data.color))
                itemPercentTv.text = "${data.percent}%"

                item = data

                itemView.onThrottleClick {

                }
            }
        }
    }
}
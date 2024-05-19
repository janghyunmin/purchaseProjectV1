package run.piece.dev.refactoring.ui.question

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.databinding.QuestionItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.question.model.QuestionItemVo

class QuestionRvAdapter(val context: Context, val viewModel: QuestionViewModel): PagingDataAdapter<QuestionItemVo, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemVH(QuestionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                getItem(position)?.let {
                    holder.bind(it ,viewModel)
                }
            }
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<QuestionItemVo>() {
            override fun areItemsTheSame(oldItem: QuestionItemVo, newItem: QuestionItemVo): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: QuestionItemVo, newItem: QuestionItemVo): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    inner class ItemVH(private val binding: QuestionItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: QuestionItemVo, viewModel: QuestionViewModel){
            binding.apply {
                item = data

                executePendingBindings()

                itemView.onThrottleClick {
                    if(binding.contentLayout.visibility == View.GONE) {
                        binding.arrowIv.background = context.getDrawable(R.drawable.arrow_up)
                        binding.contentLayout.visibility = View.VISIBLE
                        binding.contentTitleTv.text = data.title
                        binding.contentsTv.text = data.contents
                    }
                    else {
                        binding.arrowIv.background = context.getDrawable(R.drawable.arrow_down)
                        binding.contentLayout.visibility = View.GONE
                    }
                }
            }
        }
    }
}

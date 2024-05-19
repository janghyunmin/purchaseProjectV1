package run.piece.dev.refactoring.ui.notice

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.NoticeItemBinding
import run.piece.dev.refactoring.base.html.BaseWebViewActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.dev.refactoring.utils.toBasicDateFormat
import run.piece.domain.refactoring.notice.model.NoticeItemVo

class NoticeRvAdapter(val context: Context, val viewModel: NoticeViewModel): PagingDataAdapter<NoticeItemVo, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemVH(NoticeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                getItem(position)?.let {
                    holder.bind(it, viewModel)
                }
            }
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<NoticeItemVo>() {
            override fun areItemsTheSame(oldItem: NoticeItemVo, newItem: NoticeItemVo): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: NoticeItemVo, newItem: NoticeItemVo): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    inner class ItemVH(private val binding: NoticeItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: NoticeItemVo, viewModel: NoticeViewModel){
            binding.apply {
                vm = viewModel
                item = data

                noticeDateTv.text = data.createdAt.toBaseDateFormat()
            }

            itemView.onThrottleClick (
                {
                    context.startActivity(
                        BaseWebViewActivity.getNoticeDetail(
                            context = context,
                            viewName = "공지사항 상세",
                            topTitle = "공지사항",
                            boardId = data.boardId,
                            title = data.title,
                            createAt = data.createdAt.toBasicDateFormat()
                        )
                    )
                },
                interval = 1000)
        }
    }
}
package run.piece.dev.refactoring.ui.event

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import run.piece.dev.databinding.EventItemBinding
import run.piece.dev.refactoring.base.html.BaseWebViewActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.event.model.EventItemVo

class EventRvAdapter(val context: Context, val viewModel: EventViewModel): PagingDataAdapter<EventItemVo, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemVH(EventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<EventItemVo>() {
            override fun areItemsTheSame(oldItem: EventItemVo, newItem: EventItemVo): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: EventItemVo, newItem: EventItemVo): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    inner class ItemVH(private val binding: EventItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: EventItemVo, viewModel: EventViewModel){
            binding.apply {
                item = data
                executePendingBindings()

                Glide.with(context)
                    .load(data.representThumbnailPath)
                    .apply(
                        RequestOptions()
                            .transform(
                                CenterCrop(),
                                RoundedCorners(20)
                            )
                    )
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(eventIv)

                if (data.isEnd == "Y") {
                    eventEndLayout.visibility = View.VISIBLE
                    endTitleLayout.visibility = View.VISIBLE
                } else {
                    eventEndLayout.visibility = View.GONE
                    endTitleLayout.visibility = View.GONE
                }

                itemView.onThrottleClick({
                    if (layoutPosition != RecyclerView.NO_POSITION) {
                        context.startActivity(BaseWebViewActivity.getEventDetail(context = context, viewName = "이벤트 상세", eventId = data.eventId))
                    }
                }, 1000)
            }
        }
    }
}

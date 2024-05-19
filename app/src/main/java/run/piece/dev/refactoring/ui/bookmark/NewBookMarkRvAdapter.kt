package run.piece.dev.refactoring.ui.bookmark

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.tbuonomo.viewpagerdotsindicator.setBackgroundCompat
import run.piece.dev.R
import run.piece.dev.databinding.NewBookmarkItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo

class NewBookMarkRvAdapter(val context: Context, val viewModel: NewBookMarkViewModel) : ListAdapter<BookMarkItemVo, RecyclerView.ViewHolder>(diffUtil) {
    companion object {
        private const val VIEW_TYPE_ITEM = 0

        val diffUtil = object : DiffUtil.ItemCallback<BookMarkItemVo>() {
            override fun areItemsTheSame(oldItem: BookMarkItemVo, newItem: BookMarkItemVo): Boolean {
                return oldItem.magazineId == newItem.magazineId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BookMarkItemVo, newItem: BookMarkItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> ItemVH(
                NewBookmarkItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                getItem(position)?.let { vo ->
                    holder.bind(position, vo, viewModel)

                    if(vo.isFavorite == "Y") {
                        holder.itemView.isSelected = true
                        holder.itemView.findViewById<AppCompatImageView>(R.id.bookmark).setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_select))
                    }

                    holder.itemView.onThrottleClick {
                        listener?.onItemClick(
                            v = it,
                            tag = "webView",
                            position = position,
                            isFavorite = "Y",
                            data = vo
                        )
                    }

                }
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }


    inner class ItemVH(private val binding: NewBookmarkItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")

        var isSelected = false


        fun bind(position: Int, data: BookMarkItemVo, viewModel: NewBookMarkViewModel) {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))


            binding.apply {
                pos = position
                vm = viewModel
                executePendingBindings()

                Glide.with(context)
                    .load(data.representThumbnailPath)
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(thumnailPath)

                title.text = data.title
                midTitle.text = data.midTitle
                smallTitle.text = data.smallTitle

                bookmark.onThrottleClick {
                    it.isSelected = false
                    bookmark.setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_non_select))

                    listener?.onItemClick(
                        binding.bookmark,
                        tag = "bookMark",
                        position = position,
                        isFavorite = "N",
                        data = data
                    )
                }
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(
            v: View,
            tag: String,
            position: Int,
            isFavorite: String,
            data : BookMarkItemVo)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun refreshItem(position: Int) {
        notifyItemChanged(position)
    }
}

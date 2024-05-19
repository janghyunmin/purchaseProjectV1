package run.piece.dev.refactoring.ui.magazine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.ActivityResultLauncher
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
import run.piece.dev.databinding.NewMagazineItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.view.common.LoginChkActivity
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo

class NewMagazineRvAdapter(val context: Context,
                           val viewModel: NewMagazineViewModel,
                           val magazineLauncher: ActivityResultLauncher<Intent>) : ListAdapter<MagazineItemVo, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> ItemVH(
                NewMagazineItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_LOADING -> LoadingVH(
                LayoutInflater.from(parent.context).inflate(R.layout.mgazine_scroll_item_loading, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                getItem(position)?.let {
                    holder.bind(position, it, viewModel)
                }
            }
            is LoadingVH -> {
                // Bind your loading view here if needed
                holder.bind(isLoading)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading && position == itemCount - 1) VIEW_TYPE_LOADING
        else VIEW_TYPE_ITEM
    }

    inner class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // You can initialize your loading view here if needed
        private val loadingIndicator: ProgressBar = itemView.findViewById(R.id.loadingIndicator)

        // You can add a bind method to update the loading view if needed
        fun bind(isLoading: Boolean) {
            // Update the visibility of the loading view
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    inner class ItemVH(private val binding: NewMagazineItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var isSelected = false

        fun bind(position: Int, data: MagazineItemVo, viewModel: NewMagazineViewModel) {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(30))

            binding.apply {
                pos = position
                magazineViewModel = viewModel
                executePendingBindings()

                Glide.with(context)
                    .load(data.representThumbnailPath)
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(thumnailPath)

                title.text = data.title
                midTitle.text = data.midTitle
                smallTitle.text = data.smallTitle

                // Set the initial state
                if(viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                    if (data.isFavorite == "N") {
                        isSelected = false
                        bookmark.setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_non_select))
                    } else {
                        isSelected = true
                        bookmark.setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_select))
                    }
                } else {
                    isSelected = false
                    bookmark.setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_non_select))
                }

                itemView.onThrottleClick {
                    val intent = Intent(context, NewMagazineDetailWebViewActivity::class.java)
                    intent.putExtra("magazineId", data.magazineId)
                    intent.putExtra("isFavorite", data.isFavorite)
                    intent.putExtra("pos", position)
                    magazineLauncher.launch(intent)
                }

                bookmark.onThrottleClick {
                    if(viewModel.isLogin.isEmpty()) {
                        val intent = Intent(context, LoginChkActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        isSelected = !isSelected

                        if (isSelected) {
                            bookmark.setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_select))
                        } else {
                            bookmark.setBackgroundCompat(ContextCompat.getDrawable(context, R.drawable.bookmark_non_select))
                        }
                        listener?.onItemClick(position = position, isSelected = isSelected, data = data)
                    }
                }
            }
        }
    }

    fun addItems(newItems: List<MagazineItemVo>) {
        val oldItems = currentList.toMutableList()
        oldItems.addAll(newItems)
        submitList(oldItems)
    }


    fun updateItems() {

    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, isSelected: Boolean, data : MagazineItemVo)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
        private var isLoading = false

        val diffUtil = object : DiffUtil.ItemCallback<MagazineItemVo>() {
            override fun areItemsTheSame(oldItem: MagazineItemVo, newItem: MagazineItemVo): Boolean {
                return oldItem.magazineId == newItem.magazineId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: MagazineItemVo, newItem: MagazineItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }
}


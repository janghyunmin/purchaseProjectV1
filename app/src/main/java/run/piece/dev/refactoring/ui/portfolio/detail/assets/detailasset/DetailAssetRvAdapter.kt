package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.content.Context
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.DetailAssetRvItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo

class DetailAssetRvAdapter(private val context: Context,
                           private val viewModel: PortfolioDetailAssetViewModel): ListAdapter<PortfolioProductVo, RecyclerView.ViewHolder>(diffUtil) {
    var oldPos = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(DetailAssetRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<PortfolioProductVo>(){
            override fun areItemsTheSame(oldItem: PortfolioProductVo, newItem: PortfolioProductVo): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: PortfolioProductVo, newItem: PortfolioProductVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: DetailAssetRvItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: PortfolioProductVo){
            binding.apply {

                itemTv.text = data.title

                Glide
                    .with(context)
                    .load(data.representThumbnailImagePath)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(itemIv)

                roundAll(itemIv, viewModel.dpToPixel(6))

                if (data.isClicked) {
                    itemCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black))
                } else {
                    itemCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }

                itemView.onThrottleClick {
                    if (context is PortfolioDetailAssetActivity) {
                        viewModel.recruitmentState?.let {
                            context.changeFragment(data, layoutPosition)
                            selectToPosition(layoutPosition)
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun roundAll(iv: AppCompatImageView, curveRadius : Int)  : AppCompatImageView {
        iv.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                view?.let {
                    outline?.setRoundRect(0, 0, it.width, view.height, curveRadius.toFloat())
                }
            }
        }

        iv.clipToOutline = true
        return iv
    }

    fun selectToPosition(position: Int) {
        currentList.forEachIndexed { index, vo ->
            currentList[index].isClicked = position == index
        }
    }
}
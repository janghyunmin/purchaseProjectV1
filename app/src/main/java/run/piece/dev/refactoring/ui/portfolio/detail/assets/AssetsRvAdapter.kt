package run.piece.dev.refactoring.ui.portfolio.detail.assets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.databinding.AssetsRvItemBinding
import run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.PortfolioDetailAssetActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.portfolio.model.ProductCompositionItemVo

class AssetsRvAdapter(private val context: Context, private val viewModel: AssetsViewModel): ListAdapter<ProductCompositionItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(AssetsRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AssetsRvAdapter.ViewHolder -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<ProductCompositionItemVo>(){
            override fun areItemsTheSame(oldItem: ProductCompositionItemVo, newItem: ProductCompositionItemVo): Boolean {
                return oldItem.productId == newItem.productId
            }

            override fun areContentsTheSame(oldItem: ProductCompositionItemVo, newItem: ProductCompositionItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: AssetsRvItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ProductCompositionItemVo){
            binding.apply {
                item = data

                percentTv.text = context.getString(R.string.percent_txt, data.rate?.toInt())

                colorCardView.setCardBackgroundColor(ContextCompat.getColor(context, data.colorId))

                itemView.onThrottleClick({
                    if (viewModel.portfolioId?.isNotEmpty() == true && viewModel.recruitmentState?.isNotEmpty() == true) {
                        context.startActivity(PortfolioDetailAssetActivity.getIntent(context, viewModel.recruitmentState, viewModel.portfolioId, data.productId))
                    }
                }, 2000)
            }
        }
    }
}
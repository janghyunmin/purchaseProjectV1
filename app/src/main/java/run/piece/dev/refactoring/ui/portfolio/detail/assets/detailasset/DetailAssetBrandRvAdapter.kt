package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.RvDetailAssetBrandBinding
import run.piece.domain.refactoring.portfolio.model.ProductJoinBizDetailVo

class DetailAssetBrandRvAdapter(): ListAdapter<ProductJoinBizDetailVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(RvDetailAssetBrandBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<ProductJoinBizDetailVo>(){
            override fun areItemsTheSame(oldItem: ProductJoinBizDetailVo, newItem: ProductJoinBizDetailVo): Boolean {
                return oldItem.description == newItem.description
            }

            override fun areContentsTheSame(oldItem: ProductJoinBizDetailVo, newItem: ProductJoinBizDetailVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: RvDetailAssetBrandBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ProductJoinBizDetailVo){
            binding.apply {
                titleTv.text = data.title
                contentTv.text = data.description
            }
        }
    }
}
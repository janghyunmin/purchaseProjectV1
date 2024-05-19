package run.piece.dev.refactoring.ui.deposit.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.NewPurchaseProductsRvItemBinding
import run.piece.domain.refactoring.deposit.model.ProductVo

class DepositPurchaseAdapter(private val context: Context) : ListAdapter<ProductVo, RecyclerView.ViewHolder>(diffUtil) {
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ProductVo>() {
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: ProductVo, newItem: ProductVo): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: ProductVo, newItem: ProductVo): Boolean {
                return oldItem.productId == newItem.productId
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProductViewHolder(NewPurchaseProductsRvItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is DepositPurchaseAdapter.ProductViewHolder -> {
                getItem(position)?.let { data ->
                    holder.bind(data)
                }
            }
        }
    }

    inner class ProductViewHolder(private val binding: NewPurchaseProductsRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: ProductVo) {
            binding.apply {
                itemTitleTv.text = "• ${item.title}"
                executePendingBindings()
            }
        }
    }

}
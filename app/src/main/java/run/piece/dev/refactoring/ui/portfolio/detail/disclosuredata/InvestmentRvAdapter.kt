package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.DisclosureRvTabItemBinding
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.domain.refactoring.board.model.InvestmentDisclosureItemVo

class InvestmentRvAdapter(private val context: Context): ListAdapter<InvestmentDisclosureItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(DisclosureRvTabItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is InvestmentRvAdapter.ViewHolder -> {
                getItem(position)?.let { data ->
                    holder.bind(data)
                    holder.itemView.setOnClickListener {
                        itemClickListener?.onClick(it, position)
                    }
                }
            }
        }
    }


    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<InvestmentDisclosureItemVo>() {
            override fun areItemsTheSame(oldItem: InvestmentDisclosureItemVo, newItem: InvestmentDisclosureItemVo): Boolean {
                return oldItem.boardId == newItem.boardId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: InvestmentDisclosureItemVo, newItem: InvestmentDisclosureItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: DisclosureRvTabItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: InvestmentDisclosureItemVo) {
            binding.apply {
                if(item.createdAt.isNullOrEmpty()) {
                    createAtTv.text  = "${item.codeName} | ${item.createdAt.default()}"
                } else {
                    createAtTv.text  = "${item.codeName} | ${item.createdAt.toBaseDateFormat()}"
                }
                titleTv.text = item.title
            }
        }
    }


    private var itemClickListener: OnItemClickListener? = null
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }
}
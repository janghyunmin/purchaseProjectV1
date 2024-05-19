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
import run.piece.domain.refactoring.board.model.ManagementDisclosureItemVo

class ManagementRvAdapter(private val context: Context): ListAdapter<ManagementDisclosureItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(DisclosureRvTabItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ManagementRvAdapter.ViewHolder -> {
                getItem(position)?.let {data ->
                    holder.bind(data)
                    holder.itemView.setOnClickListener {
                        itemClickListener?.onClick(it,position)
                    }
                }
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ManagementDisclosureItemVo>() {
            override fun areItemsTheSame(oldItem: ManagementDisclosureItemVo, newItem: ManagementDisclosureItemVo): Boolean {
                return oldItem.boardId == newItem.boardId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: ManagementDisclosureItemVo, newItem: ManagementDisclosureItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: DisclosureRvTabItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: ManagementDisclosureItemVo) {
            binding.apply {
                if(data.createdAt.isNullOrEmpty()) {
                    statusTv.text = "${data.codeName} | ${data.createdAt.default()}"
                } else {
                    statusTv.text = "${data.codeName} | ${data.createdAt.toBaseDateFormat()}"
                }
                titleTv.text = data.title
            }
        }
    }

    fun addItems(newItems: List<ManagementDisclosureItemVo>) {
        val currentList = currentList.toMutableList()
        currentList.clear()
        currentList.addAll(newItems)
        submitList(currentList)
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
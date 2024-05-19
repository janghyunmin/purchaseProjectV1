package run.piece.dev.refactoring.ui.portfolio.detail.portfolioguide.disclosure

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.DisclosureRvTabItemBinding
import run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata.DisclosureDataItem
import run.piece.dev.refactoring.utils.toBaseDateFormat

class DisclosureTabRvAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val INVESTMENT = 0 // 경영공시
    private val MANAGEMENT = 1 // 투자공시
    var datas = mutableListOf<DisclosureDataItem>()

    private var itemClickListener: OnItemClickListener? = null
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: DisclosureRvTabItemBinding

        return when (viewType) {
            INVESTMENT -> {
                binding = DisclosureRvTabItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
                InvestViewHolder(binding)
            }

            MANAGEMENT -> {
                binding = DisclosureRvTabItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
                ManagementViewHolder(binding)
            }

            else -> {
                binding = DisclosureRvTabItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
                InvestViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(datas[position].type) {
            INVESTMENT -> {
                (holder as InvestViewHolder).bind(datas[position])
                holder.itemView.setOnClickListener {
                    itemClickListener?.onClick(it, position)
                }
            }
            MANAGEMENT -> {
                (holder as ManagementViewHolder).bind(datas[position])
                holder.itemView.setOnClickListener {
                    itemClickListener?.onClick(it, position)
                }
            }
        }
    }

    override fun getItemCount(): Int = datas.size
    override fun getItemViewType(position: Int): Int {
        return datas[position].type
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(item: List<DisclosureDataItem>) {
        val currentSize = datas.size
        datas.addAll(item)
        notifyItemRangeInserted(currentSize, item.size)
    }
    fun addItemMore(item: List<DisclosureDataItem>) {
        datas.addAll(item)
        notifyItemRangeChanged(0, datas.size)
    }

    fun allClear() {
        datas.clear()
        notifyDataSetChanged()
    }


    inner class InvestViewHolder(private val binding: DisclosureRvTabItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: DisclosureDataItem) {
            binding.apply {
                statusTv.text = "${item.codeName} | ${item.createdAt.toBaseDateFormat()}"
                titleTv.text = item.title
            }
        }
    }


    inner class ManagementViewHolder(private val binding: DisclosureRvTabItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisclosureDataItem) {
            binding.apply {
                createAtTv.text = item.createdAt.toBaseDateFormat()
                titleTv.text = item.title
            }
        }
    }



}
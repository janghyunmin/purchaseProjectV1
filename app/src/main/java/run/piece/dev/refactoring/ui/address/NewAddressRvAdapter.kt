package run.piece.dev.refactoring.ui.address

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.NewAddressItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.common.model.JusoVo

class NewAddressRvAdapter(private val context: Context): ListAdapter<JusoVo, RecyclerView.ViewHolder>(diffUtil) {
    init {
        setHasStableIds(true)
    }

    companion object {
        val diffUtil = object :DiffUtil.ItemCallback<JusoVo>() {
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: JusoVo, newItem: JusoVo): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: JusoVo, newItem: JusoVo): Boolean {
                return oldItem.roadAddr == newItem.roadAddr
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchAddressViewHolder(NewAddressItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is NewAddressRvAdapter.SearchAddressViewHolder -> {
                getItem(position)?.let { data ->
                    holder.bind(data)
                    holder.itemView.onThrottleClick {
                        listener?.onItemClick(
                            it,
                            data.zipNo,
                            data.roadAddr,
                            data.jibunAddr
                        )
                    }
                }
            }
        }
    }


    fun addAll(list: List<JusoVo>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(list)
        submitList(currentList)
    }

    inner class SearchAddressViewHolder(private val binding: NewAddressItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: JusoVo) {
            binding.apply {
                roadAddressTv.text = item.roadAddr
                jibunAddressTv.text = item.jibunAddr
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(v: View, zipNo: String, roadAddr: String, jibunAddr: String)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
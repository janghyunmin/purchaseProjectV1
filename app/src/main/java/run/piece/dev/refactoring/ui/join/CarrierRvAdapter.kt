package run.piece.dev.refactoring.ui.join

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.databinding.CarrierRvItemBinding

class CarrierRvAdapter(private val context: Context,
                       private val checkedEvent: ((String, String) -> Unit)? = null): ListAdapter<HashMap<String, Any>, RecyclerView.ViewHolder>(diffUtil) {
    private var isFirstClicked = true


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemVH(CarrierRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    inner class ItemVH(private val binding: CarrierRvItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Map<String, Any>){
            binding.apply {
                adapter = this@CarrierRvAdapter
                carrier = "${data["carrier"]}"
                position = layoutPosition

                isChecked = if ((context as NewJoinActivity).selectedValue == "${data["carrier"]}") {
                    val font = ResourcesCompat.getFont(binding.root.context, R.font.pretendard_bold)
                    itemTv.setTypeface(font, Typeface.NORMAL)
                    itemTv.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))

                    (context as NewJoinActivity).selectedValue == "${data["carrier"]}"
                } else {
                    val font = ResourcesCompat.getFont(binding.root.context, R.font.pretendard_semibold)
                    itemTv.setTypeface(font, Typeface.NORMAL)
                    itemTv.setTextColor(ContextCompat.getColor(binding.root.context, R.color.g500_B8BCC8))

                    false
                }
            }
        }
    }

    fun setItemSelected(position: Int) {
        if (position != RecyclerView.NO_POSITION) {
            if (isFirstClicked) {
                getItem(position)?.let { item ->
                    currentList.forEachIndexed { index, hashMap ->
                        if (hashMap["carrier"] == (context as NewJoinActivity).selectedValue) {
                            getItem(index)["isChecked"] = false
                            notifyItemChanged(index)
                        }
                    }

                    item["isChecked"] = !(item["isChecked"] as Boolean)
                    (context as NewJoinActivity).selectedValue = "${item["carrier"]}"
                    checkedEvent?.invoke("${item["carrier"]}", "${item["code"]}")
                    notifyItemChanged(position)
                }
            }
            isFirstClicked = false
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<HashMap<String, Any>>(){
            override fun areItemsTheSame(oldItem: HashMap<String, Any>, newItem: HashMap<String, Any>): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: HashMap<String, Any>, newItem: HashMap<String, Any>): Boolean {
                return oldItem == newItem
            }
        }
    }
}


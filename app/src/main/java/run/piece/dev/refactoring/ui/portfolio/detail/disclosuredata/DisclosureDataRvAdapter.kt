package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.DisclosureDataRvItemBinding
import run.piece.dev.refactoring.ui.deed.DeedPdfActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo

class DisclosureDataRvAdapter(private val context: Context): ListAdapter<AttachFileItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(DisclosureDataRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<AttachFileItemVo>(){
            override fun areItemsTheSame(oldItem: AttachFileItemVo, newItem: AttachFileItemVo): Boolean {
                return oldItem.attachFileCode == newItem.attachFileCode
            }

            override fun areContentsTheSame(oldItem: AttachFileItemVo, newItem: AttachFileItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: DisclosureDataRvItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: AttachFileItemVo){
            binding.apply {
                when(data.attachFileCode) {
                    "PAF0201" -> {
                        cardItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.p500_10CFC9))
                        Glide.with(context).load(R.drawable.ic_disclosure_stamp).into(itemIv)
                        itemTv.text = "증권\n신고서"
                    }
                    "PAF0202" -> {
                        cardItem.setCardBackgroundColor(ContextCompat.getColor(context, R.color.s400_49A9FF))
                        Glide.with(context).load(R.drawable.ic_disclosure_chat_invest).into(itemIv)
                        itemTv.text = "투자\n설명서"
                    }
                    "PAF0203" -> {
                        cardItem.setCardBackgroundColor(Color.parseColor("#7C96FF"))
                        Glide.with(context).load(R.drawable.ic_disclosure_deposit).into(itemIv)
                        itemTv.text = "청약\n안내문"
                    }
                }

                itemView.onThrottleClick {
                    context.startActivity(DeedPdfActivity.getIntent(context, data.attachFilePath, data.codeName, data.attachFileCode))
                }
            }
        }
    }
}

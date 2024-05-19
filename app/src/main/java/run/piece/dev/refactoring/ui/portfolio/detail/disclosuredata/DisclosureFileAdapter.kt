package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.DisclosureRvFileItemBinding

class DisclosureFileAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var pdfFileItem = mutableListOf<DisclosureFileDataItem>()
    private var itemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun downLoad(v: View, position: Int, originFileName: String , cdnFilePath: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: DisclosureRvFileItemBinding = DisclosureRvFileItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(pdfFileItem[position])
        holder.itemView.setOnClickListener {
            itemClickListener?.downLoad(
                it,
                position = position ,
                originFileName = pdfFileItem[position].originFileName,
                cdnFilePath = pdfFileItem[position].cdnFilePath
            )
        }
    }

    override fun getItemCount(): Int = pdfFileItem.size

    inner class ViewHolder(private val binding: DisclosureRvFileItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisclosureFileDataItem) {
            binding.apply {
                Glide.with(context).load(ContextCompat.getDrawable(itemView.context, R.drawable.ic_x20_file_icon)).into(fileIv)
                originFileNameTv.text = item.originFileName

            }
        }
    }
}
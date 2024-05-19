package run.piece.dev.refactoring.ui.portfolio.detail.disclosuredata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.DisclosureRvSearchItemBinding

class DisclosureSearchRvAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var searchItem = ArrayList<DisclosureSearchDataItem>()

    private var itemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(v: View, tag: String, position: Int, title: String)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: DisclosureRvSearchItemBinding = DisclosureRvSearchItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)

        return SearchViewHolder (binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SearchViewHolder).bind(searchItem[position])
        holder.itemView.setOnClickListener {
            itemClickListener?.onClick(it, "검색 조회", position, title = searchItem[position].text)
        }

        holder.itemView.findViewById<ConstraintLayout>(R.id.delete_touch_layout).setOnClickListener {
            itemClickListener?.onClick(it, "개별 삭제" , position, title = searchItem[position].text)
        }
    }

    override fun getItemCount(): Int = searchItem.size

    inner class SearchViewHolder(private val binding: DisclosureRvSearchItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisclosureSearchDataItem) {
            binding.apply {
                Glide.with(context)
                    .load(ContextCompat.getDrawable(itemView.context,R.drawable.ic_x24_search))
                    .skipMemoryCache(true)
                    .dontAnimate().into(searchIv)

                searchTitleTv.text = item.text
                Glide.with(context).load(ContextCompat.getDrawable(itemView.context, R.drawable.ic_x16_close_icon)).into(deleteIv)
            }
        }
    }
}
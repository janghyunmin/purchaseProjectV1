package run.piece.dev.refactoring.ui.portfolio.detail.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import run.piece.dev.databinding.RvStoryItemBinding
import run.piece.dev.refactoring.widget.custom.icontext.IconTextView
import run.piece.domain.refactoring.portfolio.model.PurchaseGuideItemVo

class StoryRvAdapter(private val viewModel: StoryViewModel): ListAdapter<PurchaseGuideItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(RvStoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<PurchaseGuideItemVo>(){
            override fun areItemsTheSame(oldItem: PurchaseGuideItemVo, newItem: PurchaseGuideItemVo): Boolean {
                return oldItem.guideId == newItem.guideId
            }

            override fun areContentsTheSame(oldItem: PurchaseGuideItemVo, newItem: PurchaseGuideItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: RvStoryItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: PurchaseGuideItemVo) {
            binding.apply {
                /*val width: Int = viewModel.dpToPixel(24 + 32 + (14 * data.text.length)).toInt()

                val item = CustomIconTextView.newInstance(
                    binding.root.context,
                    data.icon,
                    data.text,
                    viewModel.dpToPixel(40).toInt(),
                    100,
                    viewModel.dpToPixel(40)
                )*/

                val item = IconTextView.newInstance(
                    binding.root.context,
                    data.guideIconPath,
                    data.guideName,
                    null,
                    null,
                    viewModel.dpToPixel(40)
                )

                itemLayout.addView(item)
            }
        }
    }
}
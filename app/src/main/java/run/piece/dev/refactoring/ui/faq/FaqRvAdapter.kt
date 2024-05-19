package run.piece.dev.refactoring.ui.faq

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import run.piece.dev.R
import run.piece.dev.databinding.FaqRvIemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.utils.ToggleAnimation
import run.piece.domain.refactoring.faq.model.FaqItemVo

class FaqRvAdapter(val context: Context, val viewModel: FaqTabViewModel): PagingDataAdapter<FaqItemVo, RecyclerView.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemVH(FaqRvIemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemVH -> {
                getItem(position)?.let {
                    holder.bind(it ,viewModel)
                }
            }
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<FaqItemVo>() {
            override fun areItemsTheSame(oldItem: FaqItemVo, newItem: FaqItemVo): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: FaqItemVo, newItem: FaqItemVo): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    inner class ItemVH(private val binding: FaqRvIemBinding): RecyclerView.ViewHolder(binding.root) {
        private val rotate = 90F

        fun bind(data: FaqItemVo, viewModel: FaqTabViewModel){
            binding.apply {

                val clickedTypeface = ResourcesCompat.getFont(context, R.font.pretendard_semibold)
                val normalTypeface = ResourcesCompat.getFont(context, R.font.pretendard_regular)

                titleTv.text = data.title
                contentTv.text = data.contents

                faqLayout.onThrottleClick {
                    TransitionManager.beginDelayedTransition(faqLayout, AutoTransition())

                    if (data.expandable) {
                        titleTv.typeface = normalTypeface
                        ToggleAnimation.rotationAnim(clickIv, rotate - 90F)
                        ToggleAnimation.collapseAction(contentTv)
                        data.expandable = false

//                        faqItemLayout.setPadding(0, 0, 0, viewModel.dpToPixel(16))

                    } else {
                        titleTv.typeface = clickedTypeface
                        ToggleAnimation.rotationAnim(clickIv, rotate + 90F)
                        ToggleAnimation.expandAction(contentTv)
                        data.expandable = true

//                        faqItemLayout.setPadding(0, 0, 0, 0)
                    }
                }
            }
        }
    }
}

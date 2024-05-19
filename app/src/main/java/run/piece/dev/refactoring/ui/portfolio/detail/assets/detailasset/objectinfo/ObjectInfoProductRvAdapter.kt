package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import run.piece.dev.databinding.ObjectInfoProductRvItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.utils.ToggleAnimation

class ObjectInfoProductRvAdapter(private val context: Context): ListAdapter<ObjectInfoItem, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ObjectInfoProductRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<ObjectInfoItem>(){
            override fun areItemsTheSame(oldItem: ObjectInfoItem, newItem: ObjectInfoItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: ObjectInfoItem, newItem: ObjectInfoItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ObjectInfoProductRvItemBinding): RecyclerView.ViewHolder(binding.root){
        private val rotate = 90F

        fun bind(data: ObjectInfoItem){
            binding.apply {
                Glide.with(context).load(ContextCompat.getDrawable(context, data.productImage)).into(binding.productIv)
                productTitleTv.text = data.productTitle
                productInfoTv.text = data.productInfo

                objectInfoLayout.onThrottleClick {

                    TransitionManager.beginDelayedTransition(binding.objectInfoLayout, AutoTransition())

                    if (data.expandable) {
                        ToggleAnimation.rotationAnim(binding.arrowIv, rotate - 90F)
                        ToggleAnimation.collapseAction(binding.contentCardView)
                        data.expandable = false
                    } else {
                        ToggleAnimation.rotationAnim(binding.arrowIv, rotate + 90F)
                        ToggleAnimation.expandAction(binding.contentCardView)
                        data.expandable = true
                    }
                }
            }
        }
    }
}
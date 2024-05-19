package run.piece.dev.refactoring.ui.purchase

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import run.piece.dev.databinding.NewPurchaseRvItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.domain.refactoring.deposit.model.PurchaseVoV2

class NewPurchaseListRvAdapter(private val context: Context) : ListAdapter<PurchaseVoV2, RecyclerView.ViewHolder>(diffUtil) {
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<PurchaseVoV2>() {
            override fun areContentsTheSame(oldItem: PurchaseVoV2, newItem: PurchaseVoV2): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: PurchaseVoV2, newItem: PurchaseVoV2): Boolean {
                return oldItem.purchaseId == newItem.purchaseId
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: NewPurchaseRvItemBinding = NewPurchaseRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.apply {
            if (context.resources.displayMetrics.widthPixels > 1600) {
                val width = (binding.parentLayout.layoutParams.width * 0.85).toInt()
                val height = (binding.parentLayout.layoutParams.height * 0.85).toInt()

                val params: ConstraintLayout.LayoutParams = ConstraintLayout.LayoutParams(width, height)
                params.marginEnd = dpToPixel(14)
                binding.parentLayout.layoutParams = params
            }
        }
        return PurchaseListViewHolder(binding = binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NewPurchaseListRvAdapter.PurchaseListViewHolder -> {
                getItem(position)?.let { data ->
                    holder.bind(data, position)
                }
            }
        }
    }

    inner class PurchaseListViewHolder(private val binding: NewPurchaseRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: PurchaseVoV2, position: Int) {
            binding.apply {
                val glide = Glide.with(itemView.context)
                val reqOptions = RequestOptions()
                    .transform(
                        CenterCrop(),
                        RoundedCorners(dpToPixel(16)))
                    .skipMemoryCache(false)

                glide.load(data.representThumbnailImagePath)
                    .skipMemoryCache(false)
                    .dontAnimate()
                    .apply(reqOptions)
                    .into(binding.purchaseIv)

                purchaseIdsTv.text = data.subTitle
                purchaseTitleTv.text = data.title
                purchaseAtTv.text = data.purchaseAt?.toBaseDateFormat()
                purchasePieceVolumeTv.text = data.purchasePieceVolume.toString()

                executePendingBindings()

                itemView.onThrottleClick {
                    listener?.onItemClick(
                        itemView,
                        position = position,
                        purchaseId = data.purchaseId,
                        memberId = data.memberId
                    )
                }
            }
        }
    }

    fun dpToPixel(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    interface OnItemClickListener {
        fun onItemClick(
            view: View,
            position: Int?,
            purchaseId: String?,
            memberId: String?
        )
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }
}
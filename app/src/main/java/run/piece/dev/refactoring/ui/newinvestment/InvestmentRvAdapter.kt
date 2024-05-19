package run.piece.dev.refactoring.ui.newinvestment

import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import run.piece.dev.R
import run.piece.dev.databinding.InvestmentRvNewItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.onThrottleFirstClick
import run.piece.dev.widget.utils.ToggleAnimation
import run.piece.domain.refactoring.investment.model.InvestmentAnswerVo

class InvestmentRvAdapter(private val context: Context,
                          private val viewModel: InvestmentViewModel,
                          private var callBackEvent: ((String, Int, InvestmentAnswerVo, InvestmentRvAdapter, AppCompatTextView, AppCompatImageView, MaterialCardView) -> Unit)? = null): ListAdapter<InvestmentAnswerVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(InvestmentRvNewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
    inner class ViewHolder(private val binding: InvestmentRvNewItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: InvestmentAnswerVo){
            binding.apply {
                item = data

                if (data.isSelected) {
                    investMentTextAnim(answerTv)
                    ToggleAnimation.alphaAnimIv(context = context, arrowIv, "VISIBLE")

                    arrowIv.visibility = View.VISIBLE
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    cardView.strokeWidth = viewModel.dpToPixel(2)
                } else {
                    arrowIv.visibility = View.GONE
                    cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.g200_F2F3F4))
                    cardView.strokeWidth = viewModel.dpToPixel(0)
                }

                callBackEvent?.invoke("bind", layoutPosition, data, this@InvestmentRvAdapter, answerTv, arrowIv, cardView)

                if (viewModel.getMultiple(viewModel.requestIndex)) {
                    //다중 클릭
                    itemView.onThrottleClick {
                        callBackEvent?.invoke("click", layoutPosition, data, this@InvestmentRvAdapter, answerTv, arrowIv, cardView)
                        Log.d("투자성향분석_클릭된_아이템_정보", "다중 선택 -> ${data.score}점, ${data.answer}")
                    }
                } else {
                    //단일 클릭
                    itemView.onThrottleFirstClick(interval = 1000) {
                        callBackEvent?.invoke("click", layoutPosition, data, this@InvestmentRvAdapter, answerTv, arrowIv, cardView)
                        Log.d("투자성향분석_클릭된_아이템_정보", "단일 선택 -> ${data.score}점, ${data.answer}")
                    }
                }
            }
        }
    }

    fun investMentTextAnim(textView: TextView) {
        ObjectAnimator.ofFloat(textView, "translationX", 0F, 64F).apply {
            duration = 400
            start()
        }
    }

    fun investMentTextAnimHide(textView: TextView) {
        ObjectAnimator.ofFloat(textView, "translationX", 64F, 0F).apply {
            duration = 600
            start()
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<InvestmentAnswerVo>(){
            override fun areItemsTheSame(oldItem: InvestmentAnswerVo, newItem: InvestmentAnswerVo): Boolean {
                return oldItem.displayOrder == newItem.displayOrder
            }

            override fun areContentsTheSame(oldItem: InvestmentAnswerVo, newItem: InvestmentAnswerVo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
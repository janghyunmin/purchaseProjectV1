package run.piece.dev.refactoring.ui.deposit.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.DepositNewHistoryItemBinding
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.domain.refactoring.deposit.model.HistoryItemVo
import java.text.SimpleDateFormat

class DepositHistoryRvAdapter(private val context: Context) : ListAdapter<HistoryItemVo, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<HistoryItemVo>() {
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: HistoryItemVo, newItem: HistoryItemVo): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: HistoryItemVo, newItem: HistoryItemVo): Boolean {
                return oldItem.seq == newItem.seq
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return HistoryViewHolder(DepositNewHistoryItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is DepositHistoryRvAdapter.HistoryViewHolder -> {
                getItem(position)?.let { data ->
                    holder.bind(data)
                }
            }
        }
    }

    inner class HistoryViewHolder(private val binding: DepositNewHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: HistoryItemVo) {
            binding.apply {
                val ivResId = when(item.changeReason) {
                    "MDR0101" -> R.drawable.balance_1 // 예치금 입금
                    "MDR0102" -> R.drawable.balance_2 // 예치금 출금
                    "MDR0103" -> R.drawable.balance_3 // 분배금 입금
//                    "MDR0104" -> R.drawable.balance_7 // 분배 수수료
                    "MDR0105" -> R.drawable.balance_3 // 분배금 입금
                    "MDR0106" -> R.drawable.ic_x40_piece_cancle // 증거금 환불
                    "MDR0201" -> R.drawable.balance_11 // 증거금 출금
                    "MDR0202" -> R.drawable.ic_x40_piece_cancle // 구매 취소
                    "MDR0203" -> R.drawable.balance_5 // 조각 판매
//                    "MDR0204" -> R.drawable.balance_9 // 부가가치세 지불
//                    "MDR0205" -> R.drawable.balance_8 // 부가가치세 환불
                    "MDR0206" -> R.drawable.balance_11 // 증거금 출금
                    "MDR0207" -> R.drawable.ic_x40_piece_cancle // 증거금 환불
//                    "MDR0301" -> R.drawable.balance_10 // 예치금 출금 신청
                    "MDR0306" -> R.drawable.balance_2 // 예치금 출금
                    else -> R.drawable.balance_1
                }

                Glide.with(context).load(ivResId).into(changeReasonIv)

                changeReasonNameTv.text = item.changeReasonName
                createdAtTv.text = if(item.changeReasonDetail.isEmpty()) getDateFormat(item.createdAt)
                else "${getDateFormat(item.createdAt)} | ${item.changeReasonDetail}"

                if(item.changeAmount.toString().contains("-")) {
                    changeAmountTv.setTextColor(ContextCompat.getColor(context,R.color.c_131313))
                } else {
                    changeAmountTv.setTextColor(ContextCompat.getColor(context,R.color.c_10cfc9))
                }
                changeAmountTv.text = item.changeAmount.decimalComma() + "원"
                remainAmountTv.text = item.remainAmount.decimalComma() + "원"

            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun getDateFormat(strDate: String): String {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
            val dateFormat2 = SimpleDateFormat("MM월 dd일")
            val objDate = dateFormat.parse(strDate)

            return dateFormat2.format(objDate)
        } catch (e: Exception) {
            return ""
        }
    }

}
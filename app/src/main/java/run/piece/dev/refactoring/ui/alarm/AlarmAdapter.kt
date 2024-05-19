package run.piece.dev.refactoring.ui.alarm

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.AlarmCreateAtItemBinding
import run.piece.dev.databinding.AlarmItemBinding
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.alarm.model.AlarmItemVo

class AlarmAdapter(private val context: Context): ListAdapter<AlarmItemVo, RecyclerView.ViewHolder>(diffUtil) {
    private val NOMAL = 0
    private val DATE = 1
    private lateinit var dateVH: DateVH
    private lateinit var nomalVH: NomalVH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            NOMAL -> {
                nomalVH = NomalVH(AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

                nomalVH.itemView.onThrottleClick {
                    if (context is AlarmActivity) {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("requestCode","100")
                        intent.putExtra("backStack","Y")
                        context.startActivity(intent)
                        context.finishAffinity()
                    }
                }
                nomalVH
            }

            DATE -> {
                dateVH = DateVH(AlarmCreateAtItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                dateVH
            }

            else -> {
                throw ClassCastException("Unknown viewType $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateVH -> {
                getItem(position)?.let {
                    //createdAt 비어있지 않고, message가 비어있고, memberId가 비어있는 경우
                    if (it.createdAt.isNotEmpty() && it.message.isEmpty() && it.memberId.isEmpty()) {
                        holder.bind(it.createdAt)
                    }
                }
            }
            is NomalVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position].viewType) {
            0 -> NOMAL
            1 -> DATE
            else -> NOMAL
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<AlarmItemVo>(){
            override fun areItemsTheSame(oldItem: AlarmItemVo, newItem: AlarmItemVo): Boolean {
                return oldItem.notificationId == newItem.notificationId
            }

            override fun areContentsTheSame(oldItem: AlarmItemVo, newItem: AlarmItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class DateVH(private val binding: AlarmCreateAtItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(date: String){
            binding.apply {
                createdAt = date
            }
        }
    }

    inner class NomalVH(private val binding: AlarmItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: AlarmItemVo){
            binding.apply {
                alarmItem = data
                if (data.isRead == "Y") {
                    notificationTypeIv.alpha = 0.5F
                    titleTv.alpha = 0.5F
                    messageTv.alpha = 0.5F
                } else {
                    notificationTypeIv.alpha = 1F
                    titleTv.alpha = 1F
                    messageTv.alpha = 1F
                }
            }
        }
    }
}

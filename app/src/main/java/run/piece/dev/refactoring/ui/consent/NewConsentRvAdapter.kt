
package run.piece.dev.refactoring.ui.consent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.databinding.NewConsentItemBinding
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.consent.model.ConsentVo

// 이용약관 및 마케팅 화면 어뎁터
class NewConsentRvAdapter(private val context: Context, private val webLink: String = ""): ListAdapter<ConsentVo, RecyclerView.ViewHolder>(diffUtil) {
    private var itemCheckEvent: ((Int, ConsentVo) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ConsentVH(NewConsentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ConsentVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    inner class ConsentVH(private val binding: NewConsentItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ConsentVo){
            binding.apply {
                adapter  = this@NewConsentRvAdapter
                vh = this@ConsentVH
                vo = data
                isMandatory = data.isMandatory == "Y"

                contentTv.text = data.consentTitle

                if (data.isChecked) checkBoxIv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_terms_check_selected))
                else checkBoxIv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_terms_check_select))

                clickLayout.onThrottleClick {
                    data.isChecked = !data.isChecked

                    itemCheckEvent?.invoke(layoutPosition, data)

                    if (data.isChecked) checkBoxIv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_terms_check_selected))
                    else checkBoxIv.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_terms_check_select))
                }
            }
        }
    }

    fun detailClick(data:ConsentVo) {
        if (webLink.isNotEmpty()) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("${webLink}${data.consentCode}"))
            )
        }
    }

    fun setAllCheckEvent(state: Boolean) {
        currentList.forEachIndexed { index, consentVo ->
            consentVo.isChecked = state
            notifyItemChanged(index)
        }
    }

    fun setOnCheckEvent(data: (Int, ConsentVo) -> Unit) {
        itemCheckEvent = data
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<ConsentVo>(){
            override fun areItemsTheSame(oldItem: ConsentVo, newItem: ConsentVo): Boolean {
                return oldItem.consentCode == newItem.consentCode
            }

            override fun areContentsTheSame(oldItem: ConsentVo, newItem: ConsentVo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
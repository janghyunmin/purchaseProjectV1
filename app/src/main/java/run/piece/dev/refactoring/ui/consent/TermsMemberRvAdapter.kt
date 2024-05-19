package run.piece.dev.refactoring.ui.consent

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.ItemTermsMemberArrowBinding
import run.piece.dev.databinding.ItemTermsMemberToggleBinding
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.domain.refactoring.consent.model.TermsMemberListItemVo

class TermsMemberRvAdapter(private val context: Context,
                           private val viewType: String = "Arrow",
                           private val webLink: String,
                           private val viewModel: NewConsentViewModel): ListAdapter<TermsMemberListItemVo, RecyclerView.ViewHolder>(diffUtil) {
    private val ARROW = 0
    private val TOGGLE = 1
    private lateinit var arrowVH: ArrowVH
    private lateinit var toggleVH: ToggleVH

    //선택, (필수, 방침절차) 2개 뷰를 만들고 제어하기
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ARROW -> {
                arrowVH = ArrowVH(ItemTermsMemberArrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                arrowVH
            }

            TOGGLE -> {
                toggleVH = ToggleVH(ItemTermsMemberToggleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                toggleVH
            }

            else -> {
                arrowVH = ArrowVH(ItemTermsMemberArrowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                arrowVH
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArrowVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
            is ToggleVH -> {
                getItem(position)?.let {
                    holder.bind(it)
                }
            }
        }
    }

    inner class ArrowVH(private val binding: ItemTermsMemberArrowBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TermsMemberListItemVo){
            binding.apply {
                adapter = this@TermsMemberRvAdapter
                vh = this@ArrowVH
                vo = data
            }
        }
    }

    inner class ToggleVH(private val binding: ItemTermsMemberToggleBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: TermsMemberListItemVo){
            binding.apply {
                adapter = this@TermsMemberRvAdapter
                vh = this@ToggleVH
                vo = data

                agreeSwitch.isChecked = data.isAgreement == "Y"
                contentTv.paintFlags = contentTv.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                contentSubTv.text = "${data.date.toBaseDateFormat()} ${if (agreeSwitch.isChecked) "동의" else "철회"}"

                agreeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        if (viewModel.isShowSnackBar) {
                            (context as NewConsentActivity).sendConsent(consentCode = data.consentCode, isAgreement = "Y")
                            context.showSnackBar("광고성 정보 활용 및 수신에 동의했어요!")
                            contentSubTv.text = "${data.date.toBaseDateFormat()} ${if (agreeSwitch.isChecked) "동의" else "철회"}"
                        }
                    } else {
                        (context as NewConsentActivity).sendConsent(
                            consentCode = data.consentCode,
                            isAgreement = "N",
                            isCancel = {
                                viewModel.isShowSnackBar = it
                                if (it) { //철회하기 클릭 콜백
                                    context.showSnackBar("광고성 정보 활용 및 수신 동의를 철회했어요!")
                                    contentSubTv.text = "${data.date.toBaseDateFormat()} ${if (agreeSwitch.isChecked) "동의" else "철회"}"
                                } else agreeSwitch.isChecked = !it //닫기 클릭 콜백
                            }
                        )
                    }
                }
            }
        }
    }

    fun detailClick(data:TermsMemberListItemVo) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${webLink}${data.consentCode}")))
    }

    override fun getItemViewType(position: Int): Int {
        return when (viewType) {
            "Arrow" -> ARROW
            "Toggle" -> TOGGLE
            else -> ARROW
        }
    }

    companion object{
        val diffUtil = object: DiffUtil.ItemCallback<TermsMemberListItemVo>(){
            override fun areItemsTheSame(oldItem: TermsMemberListItemVo, newItem: TermsMemberListItemVo): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: TermsMemberListItemVo, newItem: TermsMemberListItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }
}
package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.DetailAssetFileInfoRvItemBinding
import run.piece.dev.refactoring.base.BasePdfActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.utils.ImageCloseListener
import run.piece.dev.widget.utils.ImageDialogManager
import run.piece.domain.refactoring.portfolio.model.ProductAttachFileItemVo

class DetailAssetFileInfoRvAdapter(val context: Context): ListAdapter<ProductAttachFileItemVo, RecyclerView.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(DetailAssetFileInfoRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
        val diffUtil = object: DiffUtil.ItemCallback<ProductAttachFileItemVo>(){
            override fun areItemsTheSame(oldItem: ProductAttachFileItemVo, newItem: ProductAttachFileItemVo): Boolean {
                return oldItem.attachFileCode == newItem.attachFileCode
            }

            override fun areContentsTheSame(oldItem: ProductAttachFileItemVo, newItem: ProductAttachFileItemVo): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: DetailAssetFileInfoRvItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(data: ProductAttachFileItemVo){
            binding.apply {

//                PAF0101	PAF01	증권신고서
//                PAF0102	PAF01	감정평가서
//                PAF0103	PAF01	소유권 증명서(공증)
//                PAF0104	PAF01	상품설명서
//                PAF0105	PAF01	보험증권
//                PAF0106	PAF01	보증서
//                PAF0107	PAF01	영수증
//                PAF0108	PAF01	친필사인
//                PAF0109	PAF01	정품인증서

                Glide.with(binding.root.context).load(R.drawable.ic_x20_document).into(attachFileIv)
                attachFileTv.text = "${data.attachFileCodeName} 확인하기"

                attachFileLayout.onThrottleClick(
                    {
                        when(data.attachFileCode) {
                            "PAF0106" -> { // 보증서 이미지
                                ImageDialogManager.getDialog(context, data.attachFilePath,
                                    object : ImageCloseListener {
                                        override fun onClickCancelButton() {}
                                })
                            }
                            else -> {
                                binding.root.context.startActivity(
                                    BasePdfActivity.getBasePdfIntent(binding.root.context, data.attachFilePath, data.attachFileCodeName, "PortfolioDetailAssetActivity")
                                )
                            }
                        }
                    }, 3000)
            }
        }
    }

    fun removePAF0105Item(){
        currentList.forEachIndexed { index, vo ->
            if (vo.attachFileCode == "PAF0105") {
                val currentList = this.currentList.toMutableList()
                currentList.removeAt(index)
                submitList(currentList)
            }
        }
    }
}
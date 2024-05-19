package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.databinding.RvDetailAssetProductBinding
import run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo.DetailAssetProductItem
import run.piece.dev.refactoring.widget.custom.textView.CustomTextView

class DetailAssetProductRvAdapter(private val viewModel: DetailAssetViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var productList = ArrayList<DetailAssetProductItem>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(RvDetailAssetProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(data = productList[position] , position)
    }

    override fun getItemCount(): Int = productList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: ArrayList<DetailAssetProductItem>) {
        productList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: RvDetailAssetProductBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: DetailAssetProductItem, position: Int) {
            binding.apply {

                val item = CustomTextView.newInstance(
                    binding.root.context,


                    productList[position].productTitle,
                    null,
                    null,
                    4
                )

                // 값이 "" 또는 공백일 경우는 addView를 하지 않는다
                if(productList[position].productTitle.isNotEmpty()) {
                    itemLayout.addView(item)
                }
            }
        }
    }
}
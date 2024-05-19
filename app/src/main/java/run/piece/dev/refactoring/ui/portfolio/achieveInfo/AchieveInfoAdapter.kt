package run.piece.dev.refactoring.ui.portfolio.achieveInfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R
import run.piece.dev.data.db.datasource.remote.datamodel.dmodel.portfolio.PortfolioItem
import run.piece.dev.databinding.AchieveItemBinding
import kotlin.math.roundToInt

class AchieveInfoAdapter(context: Context, achieveData: List<PortfolioItem.AchieveListModel>) : RecyclerView.Adapter<AchieveInfoAdapter.ViewHolder>() {
    var achieveData = achieveData // Remove unnecessary initialization
    val mContext = context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AchieveInfoAdapter.ViewHolder {
        val mBinding: AchieveItemBinding = AchieveItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(mBinding)
    }

    override fun onBindViewHolder(holder: AchieveInfoAdapter.ViewHolder, position: Int) {
        holder.bind(achieveItem = achieveData[position], position =  position, mContext)
    }

    override fun getItemCount(): Int {
        return achieveData.size
    }

    inner class ViewHolder(binding: AchieveItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val mBinding = binding;
        @SuppressLint("SetTextI18n")
        fun bind(achieveItem: PortfolioItem.AchieveListModel, position: Int, context: Context) {
            mBinding.pos = position

            val percentHeightParams: ViewGroup.LayoutParams = mBinding.rateIv.layoutParams
            percentHeightParams.height = achieveItem.achieveProfitRate.toDouble().times(0.8).roundToInt().dp
            mBinding.rateIv.layoutParams = percentHeightParams

            if(achieveData[position].achieveProfitRate == "0") {
                mBinding.arrowIv.visibility = View.GONE
                mBinding.percentTv.setTextColor(ContextCompat.getColor(mContext,R.color.c_8c919f))
                mBinding.rateIv.visibility = View.GONE
                mBinding.defaultIv.background = ContextCompat.getDrawable(mContext,R.drawable.layout_round_top_eaecf0_4dp)
            } else {
                mBinding.arrowIv.visibility = View.VISIBLE
                mBinding.percentTv.setTextColor(ContextCompat.getColor(mContext,R.color.c_F95D5D))
                mBinding.rateIv.visibility = View.VISIBLE
                mBinding.defaultIv.setBackgroundColor(ContextCompat.getColor(mContext,R.color.c_eaecf0))
            }

            mBinding.portfolioNumberTv.text = achieveData[position].subTitle + "호"
            mBinding.percentTv.text = achieveData[position].achieveProfitRate + "%"
            mBinding.executePendingBindings()
        }
    }
    inline val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
}
package run.piece.dev.refactoring.widget.custom.chart

import android.animation.ValueAnimator
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.TextViewCompat
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.widget.utils.ConvertMoney
import run.piece.domain.refactoring.portfolio.model.PortfolioMarketInfoPriceVo


class DayValueBarChart(context: Context) : View(context) {
    companion object {
        fun newInstance(view: View,
                        items: List<PortfolioMarketInfoPriceVo>,
                        displayWidth: Int): DayValueBarChart {
            val chart = DayValueBarChart(view.context)
            chart.init(view, items, displayWidth)
            return chart
        }
    }

    private var parentLayout = ConstraintLayout(context)
    private var innerLayout = LinearLayoutCompat(context)
    var dpWidth = 8

    private fun init(view: View, items: List<PortfolioMarketInfoPriceVo>, displayWidth: Int) {
        val marginStartEnd = dpToPixel(16)
        val marginTopBottom = dpToPixel(24)

        val viewGroup = view as ViewGroup

        //parent
        val parentParams = ConstraintLayout.LayoutParams(view.layoutParams.width, view.layoutParams.height)
        parentLayout = ConstraintLayout(context)
        parentLayout.layoutParams = parentParams

        //inner
        val innerWidth = view.layoutParams.width - (marginStartEnd * 2)
        val innerHeight = view.layoutParams.height - (marginTopBottom * 2)

        val innerParams = ConstraintLayout.LayoutParams(innerWidth, innerHeight)

        innerParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        innerParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        innerParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        innerParams.bottomToBottom =ConstraintLayout.LayoutParams.PARENT_ID

        innerParams.setMargins(marginStartEnd, marginTopBottom, marginStartEnd, marginTopBottom)

        innerLayout = LinearLayoutCompat(context)
        innerLayout.orientation = LinearLayoutCompat.HORIZONTAL

        innerLayout.layoutParams = innerParams

        //dataArea
        initItem(items, displayWidth)

        viewGroup.addView(parentLayout)
    }

    private fun initItem(items: List<PortfolioMarketInfoPriceVo>, displayWidth: Int) {
        val itemParentWidth = innerLayout.layoutParams.width / items.size

        val valueList = ArrayList<Long>()

        items.forEach {
            valueList.add(it.price.toLong())
        }

        val maxValue = valueList.max()
        val percentData = percentage(maxValue) // 최대 값의 120분의 1 value

        items.forEachIndexed { index, item ->
            var itemHeight = (item.price.toLong() / percentData).toInt()

            when(itemHeight) {
                in 0 .. 4 -> {
                    itemHeight = 4
                }
            }

            val imageView = AppCompatImageView(context)
            imageView.id = ViewCompat.generateViewId()

            val valueTv = valueTvInit(item, displayWidth)
            val dateTv = dateTvInit(item)

            //valueTv
            val valueParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            valueParams.bottomToTop = imageView.id
            valueParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            valueParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            valueParams.setMargins(0, 0, 0, dpToPixel(4))
            valueTv.layoutParams = valueParams

            //dateTv
            val dateParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            dateParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            dateParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            dateParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            dateTv.layoutParams = dateParams

            if (itemParentWidth >= dpToPixel(64 + (dpWidth * 2))) {
                //imageView
                val imageParams = ConstraintLayout.LayoutParams(dpToPixel(64), dpToPixel(itemHeight))
                imageParams.bottomToTop = dateTv.id
                imageParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                imageParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                imageParams.setMargins(0, 0, 0, dpToPixel(4))
                imageView.layoutParams = imageParams

            } else {
                val imageParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, dpToPixel(itemHeight))
                imageParams.bottomToTop = dateTv.id
                imageParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                imageParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                imageParams.setMargins(dpToPixel(dpWidth), 0, dpToPixel(dpWidth), dpToPixel(4))
                imageView.layoutParams = imageParams
            }

            if (index == items.size -1) Glide.with(context).load(R.drawable.back_top_round_radius_4_49a9ff).into(imageView)
            else Glide.with(context).load(R.drawable.back_top_round_radius_4_dadce3).into(imageView)

            imageView.animateViewHeight(700, 0, dpToPixel(itemHeight)) //anim

            //itemLayout
            val itemParentLayout = ConstraintLayout(context)

            itemParentLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            val itemParentParams = ConstraintLayout.LayoutParams(itemParentWidth, innerLayout.layoutParams.height)
            itemParentLayout.layoutParams = itemParentParams

            itemParentLayout.addView(valueTv)
            itemParentLayout.addView(imageView)
            itemParentLayout.addView(dateTv)

            innerLayout.addView(itemParentLayout)
        }
        parentLayout.addView(innerLayout)
    }

    private fun valueTvInit(item: PortfolioMarketInfoPriceVo, width: Int): AppCompatTextView {
        val textView = AppCompatTextView(context)
        textView.text = ConvertMoney().getMoneyUnitToString(item.price.toLong(), width)
        textView.gravity = Gravity.CENTER
        textView.id = ViewCompat.generateViewId()
        textView.setTextColor(ContextCompat.getColor(context, R.color.g900_292A2E))
        TextViewCompat.setTextAppearance(textView, R.style.body_3)
        return textView
    }

    private fun dateTvInit(item: PortfolioMarketInfoPriceVo): AppCompatTextView {
        val textView = AppCompatTextView(context)
        textView.text = item.pricePeriod
        textView.gravity = Gravity.CENTER
        textView.id = ViewCompat.generateViewId()
        textView.setTextColor(ContextCompat.getColor(context, R.color.g900_292A2E))
        TextViewCompat.setTextAppearance(textView, R.style.label)
        return textView
    }

    private fun dpToPixel(dp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()

    private fun percentage(value: Long): Long = value / 120

    fun View.animateViewHeight(duration: Long, startHeight: Int, endHeight: Int) {
        val valueAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        valueAnimator.addUpdateListener { animation ->
            val newHeight = animation.animatedValue as Int
            val params = this.layoutParams as ViewGroup.LayoutParams
            params.height = newHeight
            this.layoutParams = params
        }
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

package run.piece.dev.refactoring.widget.custom.chart

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import run.piece.dev.R
import run.piece.domain.refactoring.portfolio.model.ProductCompositionItemVo

class PercentBarChart : CardView {
    companion object {
        fun newInstance(context: Context,
                        height: Int,
                        width: Int,
                        radius: Int,
                        items: List<ProductCompositionItemVo>): PercentBarChart {

            val chart = PercentBarChart(context)
            chart.init(context, height, width, radius, items)
            return chart
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init(context: Context, height: Int, width: Int, radius: Int, items: List<ProductCompositionItemVo>) {
        this.cardElevation = 0F
        this.radius = radius.toFloat()

        val view = LayoutInflater.from(context).inflate(R.layout.chart_percent_bar, this, true)

        val itemLayout = view.findViewById<LinearLayoutCompat>(R.id.item_layout)

        val itemLayoutParams = itemLayout.layoutParams
        itemLayoutParams.width = width
        itemLayoutParams.height = height
        itemLayout.layoutParams = itemLayoutParams

        var rateValue = 0

        if (items.isNotEmpty()) {
            items.forEachIndexed { index, item ->
                val itemView = ImageView(context)

                rateValue += item.rate.toInt()

                val rate = if (rateValue >= 100) {
                    itemView.setBackgroundColor(ContextCompat.getColor(context, item.colorId))
                    100F
                } else if (rateValue == 0) {
                    itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.g100_F9F9F9))
                    100F
                } else {
                    itemView.setBackgroundColor(ContextCompat.getColor(context, item.colorId))
                    item.rate.toFloat()
                }

                val percent = (width * (rate * 0.01)).toInt()

                itemView.layoutParams = LinearLayoutCompat.LayoutParams(percent, LayoutParams.MATCH_PARENT)

                itemLayout.addView(itemView)
            }
        }
    }
}

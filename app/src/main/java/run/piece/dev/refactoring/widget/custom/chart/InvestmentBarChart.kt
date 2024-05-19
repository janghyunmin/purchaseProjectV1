package run.piece.dev.refactoring.widget.custom.chart
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import run.piece.dev.R

class InvestmentBarChart : MaterialCardView {
    private var itemWidth = 0
    private var itemHeight = 0
    private var nowWidth = 0

    companion object {
        fun newInstance(context: Context, radius: Int, width: Int, height: Int, size: Int): InvestmentBarChart {
            val chart = InvestmentBarChart(context)
            chart.init(context, radius, width, height, size)
            return chart
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init(context: Context, radius: Int, width: Int, height: Int, size: Int) {

        this.cardElevation = 0F
        this.radius = radius.toFloat()
        this.itemWidth = width / size
        this.itemHeight = height

        setCardBackgroundColor(ContextCompat.getColor(context, R.color.g300_EAECF0))
    }

    fun addStack(count: Int) {
        nowWidth = itemWidth * (count)

        val itemView = AppCompatImageView(context)
        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.p500_10CFC9))
        itemView.layoutParams = LinearLayoutCompat.LayoutParams(nowWidth, itemHeight)

        //removeAllViewsInLayout()

        addView(itemView)

        ObjectAnimator.ofFloat(itemView, View.ALPHA, 0f, 1f).apply {
            this.duration = 500
        }.start()
    }

    fun removeStack(count: Int) {
        nowWidth = itemWidth * (count - 1)

        val itemView = AppCompatImageView(context)
        itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.p500_10CFC9))
        itemView.layoutParams = LinearLayoutCompat.LayoutParams(nowWidth, itemHeight)

        removeAllViewsInLayout()

        addView(itemView)
    }
}


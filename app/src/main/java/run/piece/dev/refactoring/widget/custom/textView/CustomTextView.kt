package run.piece.dev.refactoring.widget.custom.textView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import run.piece.dev.R

class CustomTextView : CardView {
    companion object {
        fun newInstance(context: Context,
                        text: String,
                        height: Int?,
                        width: Int?,
                        radius: Int): CustomTextView {

            val textView = CustomTextView(context)
            textView.init(context, text, height, width, radius)
            return textView
        }
    }

    lateinit var view: View

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init(context: Context, text: String, height: Int?, width: Int?, radius: Int) {
        view = LayoutInflater.from(context).inflate(R.layout.custom_textview, this, true)

//        val param = FrameLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//        view.layoutParams = param

        this.cardElevation = 0F
        this.radius = radius.toFloat()

        val textView = view.findViewById<AppCompatTextView>(R.id.product_tv)

        textView.text = text
    }
}
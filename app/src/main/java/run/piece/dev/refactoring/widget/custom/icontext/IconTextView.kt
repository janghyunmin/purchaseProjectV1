package run.piece.dev.refactoring.widget.custom.icontext

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import run.piece.dev.R

class IconTextView : CardView {
    companion object {
        fun newInstance(context: Context,
                        icon: String,
                        text: String,
                        height: Int?,
                        width: Int?,
                        radius: Int): IconTextView {

            val iconTextView = IconTextView(context)
            iconTextView.init(context, icon, text, height, width, radius)
            return iconTextView
        }
    }

    lateinit var view: View

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init(context: Context, icon: String, text: String, height: Int?, width: Int?, radius: Int) {
        view = LayoutInflater.from(context).inflate(R.layout.icon_text_button, this, true)

        val param = FrameLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        view.layoutParams = param

        this.cardElevation = 0F
        this.radius = radius.toFloat()

        val itemLayout = view.findViewById<ConstraintLayout>(R.id.item_layout)
        val imageView = view.findViewById<AppCompatImageView>(R.id.image_view)
        val textView = view.findViewById<AppCompatTextView>(R.id.text_view)

        Glide.with(context).load(icon).into(imageView)

        textView.text = text
    }
}
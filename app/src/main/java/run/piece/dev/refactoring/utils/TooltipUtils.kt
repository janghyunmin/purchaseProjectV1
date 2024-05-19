package run.piece.dev.refactoring.utils

import android.content.Context
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonHighlightAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.OnBalloonClickListener
import run.piece.dev.R

// ToolTip Util
object TooltipUtils {
    fun basicTooltip(context: Context, lifecycleOwner: LifecycleOwner, text: String): Balloon {
        return Balloon.Builder(context = context)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setMarginBottom(4)
            .setText(text)
            .setTextSize(12f)
            .setTextColorResource(R.color.c_ffffff)
            .setTextTypeface(ResourcesCompat.getFont(context,R.font.pretendard_extrabold)!!)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(8)
            .setArrowPosition(0.5f)
            .setPadding(8)
            .setDismissWhenTouchOutside(false)
            .setCornerRadius(8f)
            .setBackgroundColorResource(R.color.black)
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            .setLifecycleOwner(lifecycleOwner)
            .build()
    }

    fun investMentTooltip(context: Context, lifecycleOwner: LifecycleOwner, text: String): Balloon {
        return Balloon.Builder(context = context)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setMarginBottom(4)
            .setText(text)
            .setTextSize(12f)
            .setTextColorResource(R.color.c_757983)
            .setTextTypeface(ResourcesCompat.getFont(context,R.font.pretendard_extrabold)!!)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(16)
            .setArrowPosition(0.5f)
            .setCornerRadius(4f)
            .setPaddingTop(6)
            .setPaddingBottom(6)
            .setPaddingRight(8)
            .setPaddingLeft(8)
            .setDismissWhenTouchOutside(false)
            .setBackgroundColorResource(R.color.c_ffffff)
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            .setBalloonHighlightAnimation(BalloonHighlightAnimation.HEARTBEAT)
            .setLifecycleOwner(lifecycleOwner)
            .build()
    }



    fun iconToolTip(context: Context, lifecycleOwner: LifecycleOwner , text: String): Balloon {
        return Balloon.Builder(context = context)
            .setText(text)
            .setArrowSize(10)
            .setWidthRatio(1.0f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setMarginRight(12)
            .setMarginLeft(12)
            .setTextSize(15f)
            .setCornerRadius(8f)
            .setTextColorResource(R.color.c_ffffff)
            .setBackgroundColorResource(R.color.black)
            .setOnBalloonDismissListener {
                Toast.makeText(context.applicationContext, "Dismiss !", Toast.LENGTH_SHORT).show()
            }
            .setBalloonAnimation(BalloonAnimation.ELASTIC)
            .setLifecycleOwner(lifecycleOwner)
            .build()
    }

    fun toolTipNagative(
        context: Context,
        onBalloonClickListener: OnBalloonClickListener,
        lifecycleOwner: LifecycleOwner,
        text: String
    ): Balloon {
        return Balloon.Builder(context)
            .setText(text)
            .setArrowSize(10)
            .setArrowPosition(0.62f)
            .setWidthRatio(1.0f)
            .setHeight(BalloonSizeSpec.WRAP)
            .setTextSize(15f)
            .setPadding(10)
            .setMarginRight(12)
            .setMarginLeft(12)
            .setCornerRadius(4f)
            .setAlpha(0.9f)
            .setTextColorResource(R.color.c_ffffff)
            .setIconDrawableResource(R.drawable.app_icon_re)
            .setBackgroundColorResource(R.color.black)
            .setOnBalloonClickListener(onBalloonClickListener)
            .setBalloonAnimation(BalloonAnimation.FADE)
            .setLifecycleOwner(lifecycleOwner)
            .build()
    }
}
package run.piece.dev.widget.utils

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import run.piece.dev.R


// Expandable Animation Util
class ToggleAnimation {
    companion object {
        // 화살표 돌리는 애니메이션
        fun rotationAnim(imageView: AppCompatImageView, rotate: Float) {
            imageView.animate()
                .withLayer()
                .rotation(rotate)
                .setDuration(300)
                .start()
        }


        // 펼침 애니메이션
        fun expandAction(view: View) {
            val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec((view.parent as View).width, View.MeasureSpec.EXACTLY)
            val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST)

            view.measure(matchParentMeasureSpec, wrapContentMeasureSpec)

            val targetHeight = view.measuredHeight

            view.layoutParams.height = 0
            view.visibility = View.VISIBLE

            val animation = object: Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    when(interpolatedTime.toInt() == 1){
                        true-> view.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                        false-> view.layoutParams.height = (targetHeight * interpolatedTime).toInt()
                    }
                    view.requestLayout()
                }
                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            animation.duration = (targetHeight / view.context.resources.displayMetrics.density).toLong()
            view.startAnimation(animation)
        }


        // 접기 애니메이션
        fun collapseAction(view: View) {
            val initialHeight = view.measuredHeight
            val animation = object: Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    when(interpolatedTime.toInt() == 1){
                        true-> view.visibility = View.GONE
                        false-> {
                            view.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                            view.requestLayout()
                        }
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            animation.duration = (initialHeight / view.context.resources.displayMetrics.density).toLong()
            view.startAnimation(animation)
        }




        // 만기상품 수익률 아이콘 서서히 사라지고 보여지는 애니메이션
        fun alphaAnimIv(context: Context, view: ImageView, viewType: String) {
            when(viewType) {
                "GONE" -> {
                    val animation = AnimationUtils.loadAnimation(context, R.anim.image_alpha_anim_gone)
                    view.startAnimation(animation)
                }
                "VISIBLE" -> {
                    val animation = AnimationUtils.loadAnimation(context, R.anim.image_alpha_anim_visible)
                    view.startAnimation(animation)
                }
            }
        }

        // 만기상품 수익률 레이아웃 글자 이동 애니메이션
        fun textMoveAnim(context: Context, view: TextView, direction: String) {
            when(direction) {
                "LEFT" -> {
                    val animation = AnimationUtils.loadAnimation(context, R.anim.text_left_move)
                    view.startAnimation(animation)
                }
                "RIGHT" -> {
                    val animation = AnimationUtils.loadAnimation(context, R.anim.text_right_move)
                    view.startAnimation(animation)
                }
            }
        }

        // Default , Fold , Mini Display 분기 후 애니메이션 처리
        fun moveAnim(context: Context, view: TextView, direction: String ,viewType: String) {
            when(viewType) {
                "BASIC_DISPLAY" -> {
                    if(direction == "LEFT") {
                        textMoveAnim(context,view,direction)
                    } else {
                        textMoveAnim(context,view,direction)
                    }
                }
                "FOLD_DISPLAY_EXPAND" -> {
                    if(direction == "LEFT") {
                        val animation = AnimationUtils.loadAnimation(context, R.anim.fold_text_left_move)
                        view.startAnimation(animation)
                    } else {
                        val animation = AnimationUtils.loadAnimation(context, R.anim.fold_text_right_move)
                        view.startAnimation(animation)
                    }
                }
                "FOLD_DISPLAY_COLLAPSE" -> {
                    if(direction == "LEFT") {
                        textMoveAnim(context,view,direction)
                    } else {
                        textMoveAnim(context,view,direction)
                    }
                }

            }
        }


        fun showCheckBoxAnim(view: AppCompatImageView) {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.animate().apply {
                alpha(1f)
                duration = 1200
                start()
            }
        }

        fun hideCheckBox(view: AppCompatImageView) {
            view.visibility = View.GONE
            view.alpha = 1f
            view.animate().apply {
                alpha(0f)
                duration = 1000
                start()
            }
        }

        fun investMentTextAnim(context: Context, textView: TextView) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.rv_item_text_right_move)
            textView.startAnimation(animation)
        }

        fun investMentTextAnimHide(context: Context, textView: TextView) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.rv_item_text_left_move)
            textView.startAnimation(animation)
        }



        /**************************************************************************************************/
        // Recycler Expand && Collapse
        fun recyclerExpand(v: RecyclerView){
            v.measure(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT)
            val targetHeight: Int = v.measuredHeight

            v.layoutParams.height = 0
            v.visibility = View.VISIBLE

            val a: Animation = object : Animation(){
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    v.layoutParams.height = if (interpolatedTime == 1f)
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    else
                        (targetHeight * interpolatedTime).toInt()
                    v.requestLayout()

                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            v.layoutAnimation = AnimationUtils.loadLayoutAnimation(v.context,R.anim.rv_layout_visible)
            v.startLayoutAnimation()

            a.duration = (targetHeight / v.context.resources.displayMetrics.density).toInt().toLong()
            v.startAnimation(a)
        }

        fun recyclerCollapse(v: RecyclerView) {
            val initialHeight : Int = v.measuredHeight
            val a : Animation = object : Animation(){
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if (interpolatedTime == 1f){
                        v.visibility = View.GONE
                    }else{
                        v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                        v.requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }

            v.layoutAnimation = AnimationUtils.loadLayoutAnimation(v.context,R.anim.rv_layout_gone)
            v.startLayoutAnimation()

            a.duration = (initialHeight / v.context.resources.displayMetrics.density).toInt().toLong()
            v.startAnimation(a)
        }
        /**************************************************************************************************/

    }
}

package run.piece.dev.widget.utils

import android.widget.ImageView

class ImgRotateAnimation {
    companion object {
        fun rotateImageAnim(imageView: ImageView, rotate: Float, started: Boolean) {
            if(!started) imageView.animate().cancel()
            else imageView.animate().withLayer().rotation(rotate).setDuration(500).start()
        }
    }
}
package run.piece.dev.refactoring

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import run.piece.dev.R
import run.piece.dev.refactoring.utils.LogUtil


object BindingAdapter {
    @BindingAdapter("imageUrl")
    @JvmStatic
    fun bindImageToView(imageView: ImageView, url: String?) =
        Glide.with(imageView.context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView)

    @BindingAdapter("imageId")
    @JvmStatic
    fun bindImageToView(imageView: ImageView, id: Int?) =
        Glide.with(imageView.context)
            .load(id)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView)

    @BindingAdapter("imageDrawable")
    @JvmStatic
    fun bindImageToView(imageView: ImageView, drawable: Drawable?) =
        Glide.with(imageView.context)
            .load(drawable)
            .into(imageView)

    @BindingAdapter("imageUrlAlarmType")
    @JvmStatic
    fun bindImageUrlAlarmType(imageView: ImageView, notificationType: String) {
        when(notificationType) {
            // 알림 신청
            "NTT0101" -> Glide.with(imageView.context).load(R.drawable.ntt0101).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 오픈 예정
            "NTT0102" -> Glide.with(imageView.context).load(R.drawable.ntt0102).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 입금 완료
            "NTT0103" -> Glide.with(imageView.context).load(R.drawable.ntt0103).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 출금 완료
            "NTT0104" -> Glide.with(imageView.context).load(R.drawable.ntt0104).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 구매 완료
            "NTT0105" -> Glide.with(imageView.context).load(R.drawable.ntt0105).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 구매 취소
            "NTT0106" -> Glide.with(imageView.context).load(R.drawable.ntt0106).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 분배 완료
            "NTT0107" -> Glide.with(imageView.context).load(R.drawable.ntt0107).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 오픈 알림
            "NTT0108" -> Glide.with(imageView.context).load(R.drawable.ntt0108).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 출금 신청
            "NTT0109" -> Glide.with(imageView.context).load(R.drawable.ntt0104).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 출금 신청 취소
            "NTT0110" -> Glide.with(imageView.context).load(R.drawable.ntt0104).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            // 보관 장소 변경
            "NTT0111" -> Glide.with(imageView.context).load(R.drawable.ntt0104).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)

            else -> {
                Glide.with(imageView.context).load(R.drawable.ntt0101).transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
            }
        }
    }
}
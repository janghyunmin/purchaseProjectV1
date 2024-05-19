package run.piece.dev.refactoring

import android.animation.Animator
import android.animation.ObjectAnimator

object PortfolioDetailAnimUtils {
    // 스크롤 애니메이션 초기화
    fun initAnimScrollY(objectAnimator: ObjectAnimator, animationEnd: ((Unit) -> Unit)?= null): ObjectAnimator {
        return objectAnimator.apply {
            duration = 500L // 애니메이션 기간 설정
            start()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    animationEnd?.invoke(Unit) // 애니메이션 종료 콜백 호출
                }
            })
        }
    }

    // 이미지 크기 조절 애니메이션
    fun animImageScale(scrollY: Int, portfolioIvScale: Float = 1F, size: Int): Float {
        val value = (scrollY / 2) * 0.001
        when (scrollY) {
            in 0..size -> {
                return (portfolioIvScale - value).toFloat()
            }
        }
        return 1F
    }

    // 상태 버튼 가시성 애니메이션 메서드
    fun animStatusButtonVisibility(scrollY: Int, cardHeight: Int, isButtonShow: Boolean, buttonStateEvent: ((Boolean) -> Unit)?= null) {
        if (scrollY >= cardHeight) {
            if (!isButtonShow) buttonStateEvent?.invoke(true) // 버튼 표시
        } else buttonStateEvent?.invoke(false) // 버튼 숨김
    }

    // 이미지 Y축 이동 애니메이션
    fun animImageTranslationY(scrollY: Int, reachPosition: Int, event: ((Boolean, Float) -> Unit)?= null) {
        val minPosition = reachPosition - 1000

        when (scrollY) {
            in minPosition..reachPosition -> { // 도달하면 0
                val data = (1000 - (reachPosition - scrollY)) * 0.1
                event?.invoke(true, data.toFloat())
            }
            else -> {
                event?.invoke(false, 1F)
            }
        }
    }

    // 스크롤 투명도 애니메이션
    fun animScrollAlpha(scrollY: Int, reachPosition: Int, deviceWidth: Int, event: ((Float) -> Unit)?= null) {
        var minPosition = reachPosition - 1000
        var newReachPosition = reachPosition

        if (deviceWidth > 1600) {
            newReachPosition = reachPosition + 100
            minPosition = (reachPosition - 1000) + 100
        }

        when (scrollY) {
            in minPosition..newReachPosition -> { // 도달하면 0
                when (val data = newReachPosition - scrollY) {
                    in 1000 downTo 0 -> {
                        event?.invoke((data * 0.001).toFloat())
                    }
                }
            }
        }
    }

    // 오버스크롤 차단 애니메이션
    fun overScrollBlock(scrollState: Boolean, scrollY: Int, viewHeight: Int, event: ((Boolean) -> Unit)?= null) {
        if (scrollState) {
            if (scrollY < viewHeight) event?.invoke(true) else event?.invoke(false)
        }
    }

    // 헤더 변경 애니메이션
    fun changeHeader(scrollY: Int, reachPosition: Int, hideBarHeight: Int, headerAlpha: ((Float) -> Unit)? = null, viewChangeEvent: ((Boolean) -> Unit)? = null) {
        var showHeaderPosition = (reachPosition - hideBarHeight)

        val divideValue: Float = ((1000F / hideBarHeight.toFloat()) * 0.001).toFloat()

        when (scrollY) {
            in showHeaderPosition..reachPosition -> {
                when (val data = hideBarHeight - (reachPosition - scrollY)) {
                    in 1000 downTo 0 -> {
                        headerAlpha?.invoke(data * divideValue)
                    }
                }
            }
        }
        if (scrollY >= reachPosition) viewChangeEvent?.invoke(true) else viewChangeEvent?.invoke(false)
    }
}

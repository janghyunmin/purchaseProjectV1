package run.piece.dev.refactoring.base.common

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import run.piece.dev.R

abstract class AnimationActivity(
    private val transitionMode: TransitionMode = TransitionMode.NONE
) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createTransitionAnimation()
    }

    override fun finish() {
        super.finish()
        exitTransitionAnimation()
    }

    private fun exitTransitionAnimation() {
        val exitAnimResId = when (transitionMode) {
            TransitionMode.HORIZON -> R.anim.activity_horizon_enter
            TransitionMode.VERTICAL -> R.anim.activity_vertical_enter
            else -> return
        }

        applyAnimationClose(R.anim.activity_none, exitAnimResId)
    }

    private fun createTransitionAnimation() {
        val enterAnimResId = when (transitionMode) {
            TransitionMode.HORIZON -> R.anim.activity_horizon_enter
            TransitionMode.VERTICAL -> R.anim.activity_vertical_enter
            else -> return
        }

        applyAnimationOpen(enterAnimResId, R.anim.activity_none)
    }


    private fun applyAnimationOpen(enterResId: Int, exitResId: Int) {
        if (Build.VERSION.SDK_INT >= 34) {
            overridePendingTransition(
                Activity.OVERRIDE_TRANSITION_OPEN, enterResId, exitResId
            )
        } else {
            overridePendingTransition(enterResId, exitResId)
        }
    }

    private fun applyAnimationClose(enterResId: Int, exitResId: Int) {
        if (Build.VERSION.SDK_INT >= 34) {
            overridePendingTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE, enterResId, exitResId
            )
        } else {
            overridePendingTransition(enterResId, exitResId)
        }
    }

    enum class TransitionMode {
        NONE,
        HORIZON,
        VERTICAL
    }
}
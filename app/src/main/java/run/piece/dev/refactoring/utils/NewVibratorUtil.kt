package run.piece.dev.refactoring.utils

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

// 기기 진동 유틸
class NewVibratorUtil {
    private lateinit var vib: Vibrator

    fun init(context: Context) {
        vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun oneShot(time: Long, strength: Int) {
        vib.vibrate(
            VibrationEffect.createOneShot(
                time,
                strength
            )
        )
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun twoShot() {
        val vibrationEffect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
        vib.vibrate(
            vibrationEffect
        )
    }
}
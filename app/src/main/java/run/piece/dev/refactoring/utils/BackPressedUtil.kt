package run.piece.dev.refactoring.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import run.piece.dev.R

class BackPressedUtil {
    // 화면 최초 진입시
    @SuppressLint("WrongConstant")
    fun activityCreate(activity: Activity, appCompatActivity: AppCompatActivity) {
        if(Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,R.anim.activity_horizon_enter, R.anim.activity_none)
        } else {
            appCompatActivity.overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
        }
    }

    @SuppressLint("WrongConstant")
    fun activityCreateFinish(activity: Activity, appCompatActivity: AppCompatActivity) {
        if(Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,R.anim.activity_horizon_enter, R.anim.activity_none)
            activity.finish()
        } else {
            appCompatActivity.overridePendingTransition(R.anim.activity_horizon_enter, R.anim.activity_none)
            appCompatActivity.finish()
        }
    }

    // 화면 왼쪽으로 슬라이드 처리 ( finish 미포함 )
    @SuppressLint("WrongConstant")
    fun activityClear(activity: Activity, appCompatActivity: AppCompatActivity) {
        if(Build.VERSION.SDK_INT >= 34) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
        } else {
            appCompatActivity.overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
        }
    }


    // 화면 왼쪽으로 슬라이드 처리 ( finish 포함 )
    @SuppressLint("WrongConstant")
    fun activityFinish(activity: Activity, appCompatActivity:AppCompatActivity) {
        if(Build.VERSION.SDK_INT >= 34) {
            activity.finish()
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
        } else {
            appCompatActivity.finish()
            appCompatActivity.overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
        }
    }

    // 화면 왼쪽으로 슬라이드 처리 ( Stack에 남아있는 모든 화면 종료 후 가장 상단에 위치 )
    @SuppressLint("WrongConstant")
    fun activityAllFinish(activity: Activity, appCompatActivity:AppCompatActivity) {
        if(Build.VERSION.SDK_INT >= 34) {
            activity.finishAffinity()
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
        } else {
            appCompatActivity.finishAffinity()
            appCompatActivity.overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
        }
    }


    // 시스템 백키 화면 왼쪽으로 슬라이드 처리 ( finish 포함 )
    fun systemBackPressed(activity: Activity, appCompatActivity: AppCompatActivity) {
        val backPressCallBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                appCompatActivity.finish()
                appCompatActivity.overridePendingTransition(R.anim.activity_left_right_enter, R.anim.activity_right_left_exit)
            }
        }
        appCompatActivity.onBackPressedDispatcher.addCallback(appCompatActivity, backPressCallBack)
    }
}
package run.piece.dev.refactoring.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    val REQUEST_ACCESS_NETWORK_STATE_PERMISSION = 2001
    val ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE

    val REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 2002
    val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

    val REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 2003
    val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

//    val REQUEST_MANAGE_EXTERNAL_STORAGE_PERMISSION = 2014
//    val MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE

    val REQUEST_USE_BIOMETRIC_PERMISSION = 2004
    val USE_BIOMETRIC = Manifest.permission.USE_BIOMETRIC // 28 ~

    val REQUEST_USE_FINGERPRINT_PERMISSION = 2005
    val USE_FINGERPRINT = Manifest.permission.USE_FINGERPRINT // 29 ~ deprecated

    val REQUEST_ACCESS_WIFI_STATE_PERMISSION = 2006
    val ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE

    val REQUEST_FOREGROUND_SERVICE_PERMISSION = 2007
    val FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE // 28 ~

    val REQUEST_USE_FULL_SCREEN_INTENT_PERMISSION = 2008
    val USE_FULL_SCREEN_INTENT = Manifest.permission.USE_FULL_SCREEN_INTENT // 29 ~

    val REQUEST_VIBRATE_PERMISSION = 2009
    val VIBRATE = Manifest.permission.VIBRATE

    val REQUEST_POST_NOTIFICATIONS_PERMISSION = 2010
    val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS

    val REQUEST_READ_MEDIA_IMAGES_PERMISSION = 2011
    val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

    val REQUEST_READ_MEDIA_VIDEO_PERMISSION = 2012
    val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO

    val REQUEST_READ_MEDIA_AUDIO_PERMISSION = 2013
    val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

    val ALL_PERMISSION_CODE = 2090

    fun hasAllPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(context, ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, USE_FULL_SCREEN_INTENT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, VIBRATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
//                    ContextCompat.checkSelfPermission(context, MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            /*ContextCompat.checkSelfPermission(context, READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED*/
        } else {
            return ContextCompat.checkSelfPermission(context, ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//                    ContextCompat.checkSelfPermission(context, MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, USE_BIOMETRIC) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, USE_FULL_SCREEN_INTENT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, VIBRATE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestAllPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(activity, arrayOf(
                ACCESS_NETWORK_STATE,
                USE_BIOMETRIC,
                USE_FINGERPRINT,
                ACCESS_WIFI_STATE,
                FOREGROUND_SERVICE,
                USE_FULL_SCREEN_INTENT,
                VIBRATE,
                POST_NOTIFICATIONS,
                READ_MEDIA_IMAGES,
                READ_MEDIA_VIDEO,
//                MANAGE_EXTERNAL_STORAGE
                /*READ_MEDIA_AUDIO*/
            ), ALL_PERMISSION_CODE)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(
                ACCESS_NETWORK_STATE,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                USE_BIOMETRIC,
                USE_FINGERPRINT,
                ACCESS_WIFI_STATE,
                FOREGROUND_SERVICE,
//                MANAGE_EXTERNAL_STORAGE,
                USE_FULL_SCREEN_INTENT,
                VIBRATE
            ), ALL_PERMISSION_CODE)
        }
    }
}
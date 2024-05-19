package run.piece.dev.firebase

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.refactoring.ui.main.MainActivity
import kotlin.random.Random

/**
 * 레거시 고도화 전 by v2.1.0
 * */

class MyFirebaseMessagingService:FirebaseMessagingService() {
    private val TAG = "FirebaseService"

    override fun onNewToken(token: String) {
        Log.d(TAG, "new Token: $token")
    }

    /**
     * this method will be triggered every time there is new FCM Message.
     **/
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "전달받은 푸시: " + remoteMessage.from)

        if (remoteMessage.notification != null) {
            Log.d(TAG, "Notification Message Title: ${remoteMessage.notification?.title}")
            Log.d(TAG, "Notification Message Body: ${remoteMessage.notification?.body}")
            Log.d(TAG, "Notification Message Body: ${remoteMessage.notification?.body.toString()}")
            sendNotification(this, remoteMessage.notification?.title, remoteMessage.notification?.body)
            PrefsHelper.write("noti", "Y")
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(context: Context, title: String?, body: String?) {
        try {
            //android 12 대응해야함

            // 푸시 클릭시 보낼 화면 정의
            val intent = Intent(this, MainActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("Notification", title)
            intent.putExtra("Notification", body)

            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE) // 푸시 왔을때 진동
                .setContentTitle(title) // 푸시 제목
                .setContentText(body) // 푸시 내용
                .setSmallIcon(R.drawable.app_icon_re) // 푸시 아이콘
                .setAutoCancel(true) // 알림 클릭시 알림 제거 여부
                .setFullScreenIntent(pendingIntent,true)
                .setContentIntent(pendingIntent) // 클릭시 pendinIntent의 Activity로 이동
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }

            notificationManager.notify(notificationID, notification) // background notificaion action
        } catch (e: Exception) {
            e.message?.let {
                Log.e("sendNotification-error : ", it)
            } ?: kotlin.run {
                Log.e("sendNotification-error : ", "오류입니다 후....")
            }
        }
    }


    /**
     * Notification 진동
     * IMPORTANCE_HIGH : 알림음이 울리며 헤드업 알림
     * IMPORTANCE_DEFAULT : 알림음이 울립니다.
     * IMPORTANCE_LOW : 알림음이 없습니다.
     * IMPORTANCE_MIN : 알림음이 없고 상태표시줄에 표시되지 않습니다.
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        try {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_NAME,
                IMPORTANCE_HIGH
            ).apply {
                description = "Description"
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        } catch (e: Exception) {
            e.message?.let {
                Log.e("createNotificationChannel-error : ", it)
            } ?: kotlin.run {
                Log.e("createNotificationChannel-error : ", "오류입니다...")
            }
        }
    }
}
object Constants {
    const val CHANNEL_ID = "96071973609"
    const val CHANNEL_NAME = "piece_noti"
}

/**
 * deepLink 1차 고도화 Push by v2.1.3
 * */
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//    private val TAG = "FirebaseService"
//
//    override fun onNewToken(token: String) {
//        Log.d(TAG, "new Token: $token")
//    }
//
//    /**
//     * this method will be triggered every time there is new FCM Message.
//     **/
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.d(TAG, "전달받은 푸시: " + remoteMessage.from)
//
//        // body=PUSH TEST 내용 이상훈, image=, title=PUSH TEST 제목, pushLinkId=b6465dc8-f883-4537-8996-0e8807fa6fe6, pushLink=NOTICE
//
//        if (remoteMessage.data.isNotEmpty()) {
//            val title = remoteMessage.data["title"]
//            val body = remoteMessage.data["body"]
//            val image = remoteMessage.data["image"]
//            val pushLinkId = remoteMessage.data["pushLinkId"]
//            val pushLink = remoteMessage.data["pushLink"]
//
//            Log.e("push-data-payload", "${remoteMessage.data}")
//
//            sendNotification(this, title, body, image, pushLinkId, pushLink)
//        }
//    }
//
//    @SuppressLint("UnspecifiedImmutableFlag")
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun sendNotification(context: Context, title: String?, body: String?, image: String?, pushLinkId: String?, pushLink: String?) {
//        try {
//            val intent = Intent(this, SchemeActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            intent.data = "piece://$pushLink/$pushLinkId".toUri()
//
//            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
//            } else {
//                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            }
//
//            val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID).setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위
//                .setDefaults(NotificationCompat.DEFAULT_VIBRATE) // 푸시 왔을때 진동
//                .setContentTitle(title) // 푸시 제목
//                .setContentText(body) // 푸시 내용
//                .setSmallIcon(R.drawable.app_icon_re) // 푸시 아이콘
//                .setAutoCancel(true) // 알림 클릭시 알림 제거 여부
//                .setFullScreenIntent(pendingIntent, true).setContentIntent(pendingIntent) // 클릭시 pendinIntent의 Activity로 이동
//                .build()
//
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val notificationID = Random.nextInt()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                createNotificationChannel(notificationManager)
//            }
//
//            notificationManager.notify(notificationID, notification) // background notificaion action
//        } catch (e: Exception) {
//            e.message?.let {
//                Log.e("sendNotification-error : ", it)
//            }
//        }
//    }
//
//    /**
//     * Notification 진동
//     * IMPORTANCE_HIGH : 알림음이 울리며 헤드업 알림
//     * IMPORTANCE_DEFAULT : 알림음이 울립니다.
//     * IMPORTANCE_LOW : 알림음이 없습니다.
//     * IMPORTANCE_MIN : 알림음이 없고 상태표시줄에 표시되지 않습니다.
//     * */
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(notificationManager: NotificationManager) {
//        try {
//            val channel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, IMPORTANCE_HIGH).apply {
//                description = "Description"
//                enableLights(true)
//                lightColor = Color.GREEN
//                enableVibration(true)
//            }
//            notificationManager.createNotificationChannel(channel)
//        } catch (e: Exception) {
//            e.message?.let {
//                Log.e("createNotificationChannel-error : ", it)
//            } ?: kotlin.run {
//                Log.e("createNotificationChannel-error : ", "오류입니다...")
//            }
//        }
//    }
//}
//
//object Constants {
//    const val CHANNEL_ID = "96071973609"
//    const val CHANNEL_NAME = "piece_noti"
//}

/**
 * deepLink 마지막 고도화 fcmService by v2.1.4
 * */
//class MyFirebaseMessagingService : FirebaseMessagingService() {
//    private val TAG = "FirebaseService"
//
//    override fun onNewToken(token: String) {
//        Log.d(TAG, "new Token: $token")
//    }
//
//    /**
//     * this method will be triggered every time there is new FCM Message.
//     **/
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        Log.d(TAG, "전달받은 푸시: " + remoteMessage.from)
//
//        /*{
//            “memberId” : “b763a819-bea4-4d72-a3f2-77e5c7bc9753",
//            “message” : “push 내용입니다.“,
//            “title” : “push 제목입니다.“,
//            “pushLink” : “PLC0101",
//            “pushLinkId” : “”,
//            “notificationType” : “NTT0101"
//        }*/
//
//        //pushLinkId가 존재 하는 경우 상세 페이지 이동
//
//        if (remoteMessage.data.isNotEmpty()) {
//            val memberId = remoteMessage.data["memberId"]
//            val message = remoteMessage.data["message"]
//            val title = remoteMessage.data["title"]
//            val pushLink = remoteMessage.data["pushLink"]
//            val pushLinkId = remoteMessage.data["pushLinkId"]
//            val notificationType = remoteMessage.data["notificationType"]
//
//            Log.e("push-data-payload", "${remoteMessage.data}")
//
//            sendNotification(this, memberId, message, title, pushLink, pushLinkId, notificationType)
//        }
//    }
//
//    @SuppressLint("UnspecifiedImmutableFlag")
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun sendNotification(context: Context, memberId: String?, message: String?, title: String?, pushLink: String?, pushLinkId: String?, notificationType: String?) {
//        try {
//
//            val intent = Intent(this, SchemeActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//
//            pushLink?.let {
//                if (it.contains("PLC")) {
//                    intent.data = "piece://$pushLink/$pushLinkId".toUri()
//                }
//            }
//
//            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
//            } else {
//                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//            }
//
//            val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID).setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위
//                .setDefaults(NotificationCompat.DEFAULT_VIBRATE) // 푸시 왔을때 진동
//                .setContentTitle(title) // 푸시 제목
//                .setContentText(message) // 푸시 내용
//                .setSmallIcon(R.drawable.app_icon_re) // 푸시 아이콘
//                .setAutoCancel(true) // 알림 클릭시 알림 제거 여부
//                .setFullScreenIntent(pendingIntent, true).setContentIntent(pendingIntent) // 클릭시 pendinIntent의 Activity로 이동
//                .build()
//
//            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val notificationID = Random.nextInt()
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                createNotificationChannel(notificationManager)
//            }
//
//            notificationManager.notify(notificationID, notification) // background notificaion action
//        } catch (e: Exception) {
//            e.message?.let {
//                Log.e("sendNotification-error : ", it)
//            }
//        }
//    }
//
//    /**
//     * Notification 진동
//     * IMPORTANCE_HIGH : 알림음이 울리며 헤드업 알림
//     * IMPORTANCE_DEFAULT : 알림음이 울립니다.
//     * IMPORTANCE_LOW : 알림음이 없습니다.
//     * IMPORTANCE_MIN : 알림음이 없고 상태표시줄에 표시되지 않습니다.
//     * */
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(notificationManager: NotificationManager) {
//        try {
//            val channel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, IMPORTANCE_HIGH).apply {
//                description = "Description"
//                enableLights(true)
//                lightColor = Color.GREEN
//                enableVibration(true)
//            }
//            notificationManager.createNotificationChannel(channel)
//        } catch (e: Exception) {
//            e.message?.let {
//                Log.e("createNotificationChannel-error : ", it)
//            } ?: kotlin.run {
//                Log.e("createNotificationChannel-error : ", "오류입니다...")
//            }
//        }
//    }
//}
//
//object Constants {
//    const val CHANNEL_ID = "96071973609"
//    const val CHANNEL_NAME = "piece_noti"
//}
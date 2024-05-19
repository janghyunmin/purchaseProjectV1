package run.piece.dev

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.naver.maps.map.NaverMapSdk
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import run.piece.dev.data.api.NetworkInfo
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.base.ErrorResponseDto
import run.piece.dev.data.refactoring.module.NetModule
import run.piece.dev.refactoring.ui.common.ServerCheckActivity
import run.piece.dev.refactoring.utils.LogUtil
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean


@HiltAndroidApp
class App : Application(), NetModule.HttpStatusCodeListener, NetworkInfo.HttpStatusCodeListener {
    private external fun isCheckDebugToolJNI(): String

    companion object {
        private lateinit var application: Application

        fun getInstance(): Application = application
        /**
         * JNI 디버깅 툴 연결 감지
         * */
        init {
            when(BuildConfig.FLAVOR) {
                "real" -> {
                    System.loadLibrary("piece-android2")
                }
                else -> {}
            }
        }

        fun getServerChkIntent(context: Context, data: ErrorResponseDto) {
            val intent = Intent(context, ServerCheckActivity::class.java).apply {
                putExtra("inspectionBeginDate", data.inspectionBeginDate)
                putExtra("inspectionEndDate", data.inspectionEndDate)
                putExtra("inspectionTitle", data.inspectionTitle)
                putExtra("inspectionContent", data.inspectionContent)
                putExtra("inspectionDate", data.inspectionDate)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        application = this

        NetModule.setHttpStatusCodeListener(this)
        NetworkInfo.setHttpStatusCodeListener(this)

        PrefsHelper.init(applicationContext)

        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("7ovr6him0s")

        when(BuildConfig.FLAVOR) {
            "real" -> {
//                LogUtil.e("======= Device 루팅 체크 ${isRootedDevice()} =======")
//                LogUtil.e("======= Debugging 체크 ${isDebugToolDevice()} =======")
//                LogUtil.e("======= Debugging 디버깅 파일 유무 검사 ${isCheckPathDevice()} =======")
//                LogUtil.e("======= USB 연결 체크 ${isUsbConnectedDevice(applicationContext)} =======")
//                LogUtil.e("======= ADB 연결 체크 ${isAdbConnectedDevice(applicationContext)} =======")
//                LogUtil.e("======= JNI : ${isCheckDebugToolJNI()} =======")
//                LogUtil.e("======= GooglePlay Install 체크 : " + isInstalledAppDevice(applicationContext,applicationContext.packageName))

                if(BuildConfig.DEBUG) {
                    CoroutineScope(Dispatchers.Main).launch {
                        EventBus.post("APP_RUNNING")
                    }
                } else {
                    if(!isRootedDevice() &&
                        !isDebugToolDevice() &&
                        !isCheckPathDevice() &&
                        isCheckDebugToolJNI() == "NONE" &&
                        isInstalledAppDevice(applicationContext,applicationContext.packageName))
                    {
                        // 앱 정상 진행
                        CoroutineScope(Dispatchers.Main).launch {
                            EventBus.post("APP_RUNNING")
                        }
                    } else {
                        // 앱 종료
                        CoroutineScope(Dispatchers.Main).launch {
                            EventBus.post("APP_FINISH")
                        }
                    }
                }

            }
            else -> {
                CoroutineScope(Dispatchers.Main).launch {
                    EventBus.post("APP_RUNNING")
                }
            }
        }
    }

    override fun netWorkInfoOkHttpStatus406(data: ErrorResponseDto) {
        CoroutineScope(Dispatchers.Main).launch {
            getServerChkIntent(applicationContext, data)
        }
    }

    override fun netWorkInfoOkHttpStatus() {
        // netWorkInfo 정상 동작
    }


    override fun netModuleOkHttpStatus406(data: ErrorResponseDto) {
        CoroutineScope(Dispatchers.Main).launch {
            getServerChkIntent(applicationContext, data)
        }
    }
    override fun netModuleOkHttpStatus() {
        // netModule 정상 동작
    }


    /**
     * Device 루팅 체크
     * */
    private fun isRootedDevice(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        if (File("/system/app/Superuser.apk").exists()) {
            return true
        }

        try {
            Runtime.getRuntime().exec("su")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }



    /**
     * Device 디버깅 탐지 체크
     * */
    private fun isDebugToolDevice(): Boolean {
        val f = File("/proc/self/status")
        try {
            val fio = FileInputStream(f)
            try {
                BufferedReader(InputStreamReader(fio)).use { br ->
                    var line: String
                    while (br.readLine().also { line = it } != null) {
                        if (line.contains("TracerPid:")) {
                            return line.substring(11).toInt() != 0
                        }
                    }
                    fio.close()
                }
            } catch (ignore: IOException) {
            }
        } catch (ignore: FileNotFoundException) {
        }
        return false
    }

    /**
     * 루팅 파일 의심 Path를 조회하여 한가지라도 조건에 충족할 경우 true 리턴
     * */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isCheckPathDevice(): Boolean {
        // AccessDeniedException
        val dirName = "/data/local/tmp"
        try {
            val found = AtomicBoolean(false)
            Files.list(File(dirName).toPath())
                .limit(200)
                .forEach { path ->
                    if (path.toString().toLowerCase(Locale.ROOT).contains("gdb")) found.set(true)
                }
            if (found.get()) {
                return true
            }
        } catch (ignored: IOException) {
        }
        return false
    }

    /**
     * USB Connect 탐지
     * */
    private fun isUsbConnectedDevice(context: Context): Boolean {
        val intent = context.registerReceiver(null, IntentFilter("android.hardware.usb.action.USB_STATE"))
        return intent != null && intent.extras != null && intent.getBooleanExtra(
            "connected",
            false
        )
    }

    /**
     * 개발자 모드 상태 탐지
     * */
    private fun isAdbConnectedDevice(context: Context) : Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) != 0;
    }


    /**
     * PlayStore 에서 다운 받은 앱 인지 탐지
     * */
    private fun isInstalledAppDevice(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return intent != null
    }


    object EventBus {
        private val events = MutableSharedFlow<Any>(replay = 1, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        val mutableEvents = events.asSharedFlow()

        suspend fun post(event: Any) {
            events.emit(event)
        }

        inline fun <reified T> subscribe(): Flow<T> {
            return mutableEvents.filter { it is T }.map { it as T }
        }

        fun remove(event: Any) {
            events.resetReplayCache()
        }
    }



}
package run.piece.dev.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import run.piece.dev.data.BuildConfig
import run.piece.dev.data.refactoring.base.ErrorResponseDto
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

object NetworkInfo {
    private lateinit var httpStatusCodeListener: HttpStatusCodeListener
    interface HttpStatusCodeListener {
        fun netWorkInfoOkHttpStatus406(data: ErrorResponseDto)
        fun netWorkInfoOkHttpStatus()
    }
    fun setHttpStatusCodeListener(listener: HttpStatusCodeListener) {
        httpStatusCodeListener = listener
    }

    @Singleton
    val nullOnEmptyConverterFactory = object : Converter.Factory() {
        fun converterFactory() = this
        override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit) = object : Converter<ResponseBody, Any?> {
            val nextResponseBodyConverter = retrofit.nextResponseBodyConverter<Any?>(converterFactory(), type, annotations)
            override fun convert(value: ResponseBody) = if (value.contentLength() != 0L) {
                Log.e("NetworkInfo : ", "${value.contentLength()}")
                nextResponseBodyConverter.convert(value)
            } else {
                null
            }
        }
    }

    @Singleton
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder().apply {
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(nullOnEmptyConverterFactory)
            addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            client(getOkHttpClient(getLoggingInterceptor()))
            baseUrl(BuildConfig.PIECE_API_V3)
        }.build()
    }

    @Singleton
    fun getOkHttpClient(
        LoggerInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient().newBuilder()
            .retryOnConnectionFailure(true)
        okHttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
        okHttpClientBuilder.readTimeout(30, TimeUnit.SECONDS) // 읽기 타임아웃
        okHttpClientBuilder.writeTimeout(15, TimeUnit.SECONDS) // 쓰기 타임아웃
        okHttpClientBuilder.pingInterval(30, TimeUnit.SECONDS) // Ping TimeInterval
        okHttpClientBuilder.retryOnConnectionFailure(true)
        okHttpClientBuilder.addInterceptor(LoggerInterceptor)
        okHttpClientBuilder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val response = chain.proceed(originalRequest)

            if(!response.isSuccessful) {
                if (response.code == 406) {
                    Log.v("NetworkInfo Error : ${response.code}  ", "Response : ${response.body?.string()}")
                    val errorBody = response.body?.string()
                    val errorObject = Gson().fromJson(errorBody, ErrorResponseDto::class.java)

                    CoroutineScope(Dispatchers.IO).launch {
                        HttpStatusCodeFlow.emitHttpStatusCode(errorObject)
                        httpStatusCodeListener.netWorkInfoOkHttpStatus406(errorObject)
                    }
                    throw StatusCode406Exception()
                }
            }

            httpStatusCodeListener.netWorkInfoOkHttpStatus()
            response
        }

        return okHttpClientBuilder.build()
    }

    @Singleton
    fun getLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Log.e("NetworkModule : ", "$message")
        }.let {
            if (BuildConfig.DEBUG) {
                it.setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                it.setLevel(HttpLoggingInterceptor.Level.NONE)
            }
        }
    }

    @Throws(StatusCode406Exception::class)
    private fun throwError(exception: StatusCode406Exception) {
        throw exception
    }

    class StatusCode406Exception : IOException("Received HTTP error 406")

    object HttpStatusCodeFlow {
        private val _httpStatusCodeFlow = MutableSharedFlow<ErrorResponseDto>()
        val httpStatusCodeFlow: Flow<ErrorResponseDto> = _httpStatusCodeFlow

        suspend fun emitHttpStatusCode(response: ErrorResponseDto) {
            _httpStatusCodeFlow.emit(response)
        }
    }


}

package run.piece.dev.data.refactoring.module

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Interceptor
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

@Module
@InstallIn(SingletonComponent::class)
object NetModule {
    private lateinit var httpStatusCodeListener: HttpStatusCodeListener
    interface HttpStatusCodeListener {
        fun netModuleOkHttpStatus406(data: ErrorResponseDto)
        fun netModuleOkHttpStatus()
    }
    fun setHttpStatusCodeListener(listener: HttpStatusCodeListener) {
        httpStatusCodeListener = listener
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().apply {
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            client(okHttpClient)
            baseUrl(BuildConfig.PIECE_API_V3)
        }.build()
    }

    @Singleton
    @Provides
    fun provideOkHttp(
        loggingInterceptor: HttpLoggingInterceptor,
        application: Application
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(15, TimeUnit.SECONDS)
            pingInterval(30, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor(loggingInterceptor)
            addInterceptor { chain ->
                val originalRequest = chain.request()
                val response = chain.proceed(originalRequest)

                if(!response.isSuccessful) {
                    if (response.code == 406) {
                        val errorBody = response.body?.string() // Read the response body only once
                        val errorObject = Gson().fromJson(errorBody, ErrorResponseDto::class.java)

                        CoroutineScope(Dispatchers.IO).launch {
                            HttpStatusCodeFlow.emitHttpStatusCode(errorObject)
                            httpStatusCodeListener.netModuleOkHttpStatus406(errorObject)
                        }
                        throw StatusCode406Exception()
                    }
                }

                httpStatusCodeListener.netModuleOkHttpStatus()
                response
            }
        }.build()
    }


    @Singleton
    @Provides
    fun provideAuthorizationInterceptor(): Interceptor = AuthorizationInterceptor()

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
        }
        return httpLoggingInterceptor
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

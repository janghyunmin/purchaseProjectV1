package run.piece.dev.data.refactoring.ui.alarm.api

import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmBaseDto
import run.piece.dev.data.refactoring.ui.alarm.dto.AlarmDto
import run.piece.dev.data.refactoring.ui.alarm.dto.PortfolioGetAlarmDto
import run.piece.dev.data.utils.WrappedResponse

interface AlarmApi {
    @GET("member/alarm") // 유저 알림 설정 목록 조회 요청
    suspend fun getAlarm(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Query("length") length: Int = 500,
        @Query("notificationType") notificationType: String,
    ): WrappedResponse<AlarmDto>
    @PUT("member/alarm")
    suspend fun putAlarm(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
    ): BaseDto
    @POST("member/portfolio/notification/{portfolioId}")
    suspend fun sendPortfolioAlarm(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Path("portfolioId") portfolioId: String
    ): AlarmBaseDto
    @HTTP(method = "DELETE", path = "member/portfolio/notification/{portfolioId}", hasBody = false)
    suspend fun deletePortfolioAlarm(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Path("portfolioId") portfolioId: String?
    ): AlarmBaseDto

    @GET("member/portfolio/notification/{portfolioId}")
    suspend fun getPortfolioAlarm(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Path("portfolioId") portfolioId: String
    ): WrappedResponse<PortfolioGetAlarmDto>
}
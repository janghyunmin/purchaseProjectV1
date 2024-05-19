package run.piece.dev.data.refactoring.ui.member.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.PUT
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.dev.data.refactoring.ui.member.model.dto.AuthPinDto
import run.piece.dev.data.refactoring.ui.member.model.dto.PostSmsAuthDto
import run.piece.dev.data.refactoring.ui.member.model.dto.JoinDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDeleteDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDto
import run.piece.dev.data.refactoring.ui.member.model.dto.PostSmsVerificationDto
import run.piece.dev.data.refactoring.ui.member.model.dto.SsnDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.investment.model.request.InvestRiskModel
import run.piece.domain.refactoring.member.model.JoinBodyVo
import run.piece.domain.refactoring.member.model.MemberPinModel
import run.piece.domain.refactoring.member.model.request.MemberDeleteModel
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.model.request.NewMemberNotificationModel
import run.piece.domain.refactoring.member.model.request.PostSmsVerificationModel
import java.util.HashMap

interface MemberApi {
    // 회원가입
    @POST("member/join")
    suspend fun postMemberJoin(@Body joinModel: JoinBodyVo): WrappedResponse<JoinDto>

    // accessToken 검증
    @GET("member/auth")
    suspend fun getAccessToken(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("grantType") grantType: String?,
        @Header("memberId") memberId: String?,
    ): WrappedResponse<BaseDto?>

    // PIN 번호 검증
    @GET("member/auth/pin")
    suspend fun getAuthPin(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Header("pinNumber") pinNumber: String?
    ): WrappedResponse<AuthPinDto>

    // PIN 번호 변경
    @PUT("member/auth/pin")
    suspend fun putAuthPin(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body memberPinModel: MemberPinModel?
    ): WrappedResponse<BaseDto?>

    // 실명인증 여부 조회
    @GET("member/ssn")
    suspend fun getSsnCheck(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?
    ): WrappedResponse<SsnDto>

    // 다른기기 로그아웃 처리
    @GET("member/device/check")
    suspend fun getMemberDeviceCheck(
        @Header("memberId") memberId: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberAppVersion") memberAppVersion: String?,
    ): WrappedResponse<BaseDto?>

    // 회원 정보 조회
    @GET("member")
    suspend fun getMemberInfo(@Header("accessToken") accessToken: String,
                              @Header("deviceId") deviceId: String,
                              @Header("memberId") memberId: String): WrappedResponse<MemberDto>

    // 회원 알림 설정 변경
    @PUT("member/notification")
    suspend fun putNotification(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Body model: NewMemberNotificationModel
    ): WrappedResponse<BaseDto?>

    // 고객 투자 위험 동의 요청
    @POST("member/risk/consent")
    suspend fun postInvestAgreement(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body investRiskModel: InvestRiskModel
    ) : WrappedResponse<BaseDto?>

    // 문자 발송 요청
    @POST("member/call_sms_auth")
    suspend fun postSmsAuth(@Body model: PostSmsAuthModel): WrappedResponse<PostSmsAuthDto>

    @POST("member/call_sms_auth_retry")
    suspend fun postReSmsAuth(@Body model: PostSmsAuthModel): WrappedResponse<PostSmsAuthDto>

    // SMS 검증요청
    @POST("member/call_sms_verification")
    suspend fun postSmsVerification(@Body model: PostSmsVerificationModel): WrappedResponse<PostSmsVerificationDto>

    // 회원 정보 수정 요청
    @PUT("member")
    suspend fun putMember(@HeaderMap headers: HashMap<String, String>, @Body model: MemberModifyModel): WrappedResponse<MemberDto>

    @HTTP(method = "DELETE", path = "member", hasBody = true)
    suspend fun deleteMember(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body memberDeleteModel: MemberDeleteModel
    ): Response<MemberDeleteDto>
}
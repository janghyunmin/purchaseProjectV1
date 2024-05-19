package run.piece.dev.data.refactoring.ui.member.repository.remote

import retrofit2.Response
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.dev.data.refactoring.ui.member.model.dto.AuthPinDto
import run.piece.dev.data.refactoring.ui.member.model.dto.JoinDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDeleteDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDto
import run.piece.dev.data.refactoring.ui.member.model.dto.PostSmsAuthDto
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

interface MemberRemoteDataSource {
    // 회원가입 / 로그인
    suspend fun postJoin(joinModel: JoinBodyVo): WrappedResponse<JoinDto>

    // 토큰 검증
    suspend fun getAccessToken(accessToken: String, deviceId: String, grantType: String, memberId: String): WrappedResponse<BaseDto?>

    // 핀번호(간편비밀번호) 검증
    suspend fun getAuthPin(accessToken: String, deviceId: String, memberId: String, pinNumber: String): WrappedResponse<AuthPinDto>

    // 핀번호 변경
    suspend fun putAuthPin(accessToken: String, deviceId: String, memberId: String, memberPinModel: MemberPinModel): WrappedResponse<BaseDto?>

    // 실명인증 여부 조회
    suspend fun getSsnCheck(accessToken: String, deviceId: String, memberId: String): WrappedResponse<SsnDto>

    // 다른기기 디바이스 로그인 체크
    suspend fun getMemberDeviceChk(memberId: String, deviceId: String, memberAppVersion: String): WrappedResponse<BaseDto?>

    // 회원 정보 조회
    suspend fun getMemberInfo(accessToken: String, deviceId: String, memberId: String): WrappedResponse<MemberDto>

    suspend fun putMemberNotification(accessToken: String, deviceId: String, memberId: String, model: NewMemberNotificationModel): WrappedResponse<BaseDto?>

    suspend fun postInvestAgreement(accessToken: String, deviceId: String, memberId: String, investRiskModel: InvestRiskModel) : WrappedResponse<BaseDto?>
    suspend fun postSmsAuth(model: PostSmsAuthModel): WrappedResponse<PostSmsAuthDto>

    suspend fun postReSmsAuth(model: PostSmsAuthModel): WrappedResponse<PostSmsAuthDto>

    suspend fun postSmsVerification(model: PostSmsVerificationModel): WrappedResponse<PostSmsVerificationDto>

    suspend fun putMember(headers: HashMap<String, String>, model: MemberModifyModel): WrappedResponse<MemberDto>

    suspend fun deleteMember(accessToken: String, deviceId: String, memberId: String, memberDeleteModel: MemberDeleteModel): Response<MemberDeleteDto>
}
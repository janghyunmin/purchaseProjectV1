package run.piece.dev.data.refactoring.ui.member.repository.remote

import retrofit2.Response
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.dev.data.refactoring.ui.member.api.MemberApi
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

class MemberRemoteDataSourceImpl(private val api: MemberApi): MemberRemoteDataSource {
    override suspend fun postJoin(joinModel: JoinBodyVo): WrappedResponse<JoinDto> = api.postMemberJoin(joinModel = joinModel)
    override suspend fun getAccessToken(accessToken: String, deviceId: String, grantType: String, memberId: String): WrappedResponse<BaseDto?> = api.getAccessToken(accessToken = accessToken, deviceId = deviceId, grantType = grantType, memberId = memberId)
    override suspend fun getAuthPin(accessToken: String, deviceId: String, memberId: String, pinNumber: String) : WrappedResponse<AuthPinDto> = api.getAuthPin(accessToken = accessToken, deviceId = deviceId, memberId = memberId, pinNumber = pinNumber)
    override suspend fun putAuthPin(accessToken: String, deviceId: String, memberId: String, memberPinModel: MemberPinModel): WrappedResponse<BaseDto?> = api.putAuthPin(accessToken = accessToken, deviceId = deviceId , memberId = memberId, memberPinModel = memberPinModel)
    override suspend fun getSsnCheck(accessToken: String, deviceId: String, memberId: String): WrappedResponse<SsnDto> = api.getSsnCheck(accessToken = accessToken, deviceId = deviceId, memberId = memberId)
    override suspend fun getMemberDeviceChk(memberId: String, deviceId: String, memberAppVersion: String): WrappedResponse<BaseDto?> = api.getMemberDeviceCheck(memberId = memberId, deviceId = deviceId, memberAppVersion = memberAppVersion)
    override suspend fun getMemberInfo(accessToken: String, deviceId: String, memberId: String): WrappedResponse<MemberDto> = api.getMemberInfo(accessToken = accessToken, deviceId = deviceId, memberId = memberId)
    override suspend fun putMemberNotification(accessToken: String, deviceId: String, memberId: String, model: NewMemberNotificationModel): WrappedResponse<BaseDto?> = api.putNotification(accessToken = accessToken, deviceId =  deviceId, memberId = memberId, model = model)
    override suspend fun postInvestAgreement(accessToken: String, deviceId: String, memberId: String, investRiskModel: InvestRiskModel): WrappedResponse<BaseDto?> = api.postInvestAgreement(accessToken = accessToken, deviceId = deviceId, memberId = memberId, investRiskModel = investRiskModel)
    override suspend fun postSmsAuth(model: PostSmsAuthModel): WrappedResponse<PostSmsAuthDto> = api.postSmsAuth(model = model)
    override suspend fun postReSmsAuth(model: PostSmsAuthModel): WrappedResponse<PostSmsAuthDto> = api.postReSmsAuth(model = model)
    override suspend fun postSmsVerification(model: PostSmsVerificationModel): WrappedResponse<PostSmsVerificationDto> = api.postSmsVerification(model = model)
    override suspend fun putMember(headers: HashMap<String, String>, model: MemberModifyModel): WrappedResponse<MemberDto> = api.putMember(headers = headers, model = model)
    override suspend fun deleteMember(accessToken: String, deviceId: String, memberId: String, memberDeleteModel: MemberDeleteModel): Response<MemberDeleteDto> = api.deleteMember(accessToken = accessToken, deviceId = deviceId, memberId, memberDeleteModel = memberDeleteModel)
}
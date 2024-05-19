package run.piece.domain.refactoring.member.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.investment.model.request.InvestRiskModel
import run.piece.domain.refactoring.member.model.AuthPinVo
import run.piece.domain.refactoring.member.model.JoinBodyVo
import run.piece.domain.refactoring.member.model.JoinVo
import run.piece.domain.refactoring.member.model.MemberDeleteVo
import run.piece.domain.refactoring.member.model.MemberPinModel
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.PostSmsAuthVo
import run.piece.domain.refactoring.member.model.PostSmsVerificationVo
import run.piece.domain.refactoring.member.model.SsnVo
import run.piece.domain.refactoring.member.model.request.MemberDeleteModel
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.model.request.NewMemberNotificationModel
import run.piece.domain.refactoring.member.model.request.PostSmsAuthModel
import run.piece.domain.refactoring.member.model.request.PostSmsVerificationModel

interface MemberRepository {
    fun postJoin(joinModel: JoinBodyVo): Flow<JoinVo>
    fun getAccessToken(accessToken: String, deviceId: String, grantType: String, memberId: String): Flow<BaseVo?>
    fun getAuthPin(accessToken: String, deviceId: String, memberId: String, pinNumber: String): Flow<AuthPinVo>
    fun putAuthPin(accessToken: String, deviceId: String, memberId: String, memberPinModel: MemberPinModel): Flow<BaseVo?>
    fun getSsnCheck(accessToken: String, deviceId: String, memberId: String): Flow<SsnVo>
    fun getMemberDeviceCheck(memberId: String, deviceId: String, memberAppVersion: String) : Flow<BaseVo?>
    fun getMemberInfo(accessToken: String, deviceId: String, memberId: String): Flow<MemberVo>
    fun memberPutNotification(accessToken: String, deviceId: String, memberId: String, model: NewMemberNotificationModel): Flow<BaseVo>

    fun postInvestAgreement(accessToken: String, deviceId: String, memberId: String, investRiskModel: InvestRiskModel) : Flow<BaseVo?>
    fun postSmsAuth(model: PostSmsAuthModel): Flow<PostSmsAuthVo>
    fun postReSmsAuth(model: PostSmsAuthModel): Flow<PostSmsAuthVo>
    fun postSmsVerification(model: PostSmsVerificationModel): Flow<PostSmsVerificationVo>

    fun putMember(headers: HashMap<String, String>, model: MemberModifyModel): Flow<MemberVo>

    fun deleteMember(accessToken: String, deviceId: String, memberId: String, memberDeleteModel: MemberDeleteModel): Flow<MemberDeleteVo>
}
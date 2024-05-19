package run.piece.dev.data.refactoring.ui.member.repository

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToAuthPinVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToBaseVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToJoinVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToMemberDeleteVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToMemberVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToPostAuthVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToPostSmsVerificationVo
import run.piece.dev.data.refactoring.ui.member.mapper.mapperToSsnVo
import run.piece.dev.data.refactoring.ui.member.repository.local.MemberLocalDataSource
import run.piece.dev.data.refactoring.ui.member.repository.remote.MemberRemoteDataSource
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
import run.piece.domain.refactoring.member.repository.MemberRepository

@ActivityRetainedScoped
class MemberRepositoryImpl(private val remoteDataSource: MemberRemoteDataSource,
                           private val localDataSource: MemberLocalDataSource): MemberRepository {
    override fun postJoin(joinModel: JoinBodyVo
    ): Flow<JoinVo> = flow {
        emit(remoteDataSource.postJoin(joinModel = joinModel).data.mapperToJoinVo())
    }

    override fun getAccessToken(accessToken: String, deviceId: String, grantType: String, memberId: String): Flow<BaseVo?> = flow {
        emit(remoteDataSource.getAccessToken(accessToken = accessToken, deviceId = deviceId, grantType = grantType, memberId = memberId).data?.mapperToBaseVo())
    }

    override fun getAuthPin(accessToken: String, deviceId: String, memberId: String, pinNumber: String): Flow<AuthPinVo> = flow {
        emit(remoteDataSource.getAuthPin(accessToken = accessToken, deviceId = deviceId, memberId = memberId, pinNumber = pinNumber).data.mapperToAuthPinVo())
    }

    override fun putAuthPin(accessToken: String, deviceId: String, memberId: String, memberPinModel: MemberPinModel): Flow<BaseVo?> = flow {
        emit(remoteDataSource.putAuthPin(accessToken = accessToken, deviceId = deviceId, memberId = memberId , memberPinModel = memberPinModel).data?.mapperToBaseVo())
    }

    override fun getSsnCheck(accessToken: String, deviceId: String, memberId: String): Flow<SsnVo> = flow {
        emit(remoteDataSource.getSsnCheck(accessToken = accessToken, deviceId = deviceId , memberId = memberId).data.mapperToSsnVo())
    }

    override fun getMemberDeviceCheck(memberId: String, deviceId: String, memberAppVersion: String): Flow<BaseVo?> = flow {
        emit(remoteDataSource.getMemberDeviceChk(memberId = memberId, deviceId = deviceId, memberAppVersion = memberAppVersion).data?.mapperToBaseVo())
    }


    override fun getMemberInfo(accessToken: String, deviceId: String, memberId: String): Flow<MemberVo> = flow {
        emit(remoteDataSource.getMemberInfo(accessToken = accessToken, deviceId = deviceId, memberId = memberId).data.mapperToMemberVo())
    }

    override fun memberPutNotification(accessToken: String, deviceId: String, memberId: String, model: NewMemberNotificationModel): Flow<BaseVo> = flow {
        remoteDataSource.putMemberNotification(accessToken = accessToken, deviceId = deviceId, memberId = memberId, model = model).data?.mapperToBaseVo()?.let { emit(it) }
    }

    override fun postInvestAgreement(accessToken: String, deviceId: String, memberId: String, investRiskModel: InvestRiskModel): Flow<BaseVo?> = flow {
        remoteDataSource.postInvestAgreement(accessToken = accessToken, deviceId = deviceId , memberId = memberId, investRiskModel = investRiskModel).data?.mapperToBaseVo()
    }

    override fun postSmsAuth(model: PostSmsAuthModel): Flow<PostSmsAuthVo> = flow {
        emit(remoteDataSource.postSmsAuth(model = model).data.mapperToPostAuthVo())
    }

    override fun postReSmsAuth(model: PostSmsAuthModel): Flow<PostSmsAuthVo> = flow {
        emit(remoteDataSource.postSmsAuth(model = model).data.mapperToPostAuthVo())
    }

    override fun postSmsVerification(model: PostSmsVerificationModel): Flow<PostSmsVerificationVo> = flow {
        emit(remoteDataSource.postSmsVerification(model = model).data.mapperToPostSmsVerificationVo())
    }

    override fun putMember(headers: HashMap<String, String>, model: MemberModifyModel): Flow<MemberVo> = flow {
        emit(remoteDataSource.putMember(headers = headers, model = model).data.mapperToMemberVo())
    }

    override fun deleteMember(accessToken: String, deviceId: String, memberId: String, memberDeleteModel: MemberDeleteModel): Flow<MemberDeleteVo> = flow {
        val code = remoteDataSource.deleteMember(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberDeleteModel = memberDeleteModel).code()
        emit(remoteDataSource.deleteMember(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberDeleteModel = memberDeleteModel).body().mapperToMemberDeleteVo(code))
        //emit(remoteDataSource.deleteMember(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberDeleteModel = memberDeleteModel).mapperToMemberDeleteVo())
    }
}
package run.piece.dev.data.refactoring.ui.deposit.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToNhAccount
import run.piece.dev.data.refactoring.ui.deposit.mapper.mapperToNhBankVo
import run.piece.dev.data.refactoring.ui.deposit.repository.local.NhBankLocalDataSource
import run.piece.dev.data.refactoring.ui.deposit.repository.remote.NhBankRemoteDataSource
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deposit.model.NhBankHistoryVo
import run.piece.domain.refactoring.deposit.model.NhBankRegisterVo
import run.piece.domain.refactoring.deposit.model.WithDrawVo
import run.piece.domain.refactoring.deposit.repository.NhBankRepository

class NhBankRepositoryImpl (
    private val nhBankRemoteDataSource: NhBankRemoteDataSource,
    private val nhBankLocalDataSource: NhBankLocalDataSource
) : NhBankRepository {
    override fun createAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: NhBankRegisterVo
    ) : Flow<BaseVo> = flow {
        emit(nhBankRemoteDataSource.createAccount(accessToken, deviceId, memberId, registerNhBankModel.mapperToNhAccount()).mapperToNhBankVo())
    }

    override fun changeAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: NhBankRegisterVo
    ) : Flow<BaseVo> = flow {
        emit(nhBankRemoteDataSource.changeAccount(accessToken, deviceId, memberId, registerNhBankModel.mapperToNhAccount()).mapperToNhBankVo())
    }

    override suspend fun nhHistory(
        accessToken: String,
        deviceId: String,
        memberId: String
    ): Response<NhBankHistoryVo> =
        nhBankRemoteDataSource.nhHistory(accessToken, deviceId, memberId)


    override fun nhBankWithDraw(
        accessToken: String,
        deviceId: String,
        memberId: String,
        withDrawVo: WithDrawVo
    ): Flow<BaseVo> = flow {
        emit(nhBankRemoteDataSource.nhWithDraw(accessToken, deviceId, memberId, withDrawVo).mapperToNhBankVo())
    }
}
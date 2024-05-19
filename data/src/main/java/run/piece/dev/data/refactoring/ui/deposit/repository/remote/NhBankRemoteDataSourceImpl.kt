package run.piece.dev.data.refactoring.ui.deposit.repository.remote

import retrofit2.Response
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deposit.api.NhApi
import run.piece.dev.data.refactoring.ui.deposit.dto.RegisterNhBankDto
import run.piece.domain.refactoring.deposit.model.NhBankHistoryVo
import run.piece.domain.refactoring.deposit.model.WithDrawVo

class NhBankRemoteDataSourceImpl(private val api: NhApi) : NhBankRemoteDataSource {

    override suspend fun createAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: RegisterNhBankDto
    ) : BaseDto = api.postNhAccount(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        registerNhBankModel = registerNhBankModel
    )

    override suspend fun changeAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: RegisterNhBankDto
    ) : BaseDto = api.changeAccount(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        registerNhBankModel = registerNhBankModel
    )

    override suspend fun nhHistory(
        accessToken: String,
        deviceId: String,
        memberId: String
    ): Response<NhBankHistoryVo> = api.postNhHistory(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId
    )

    override suspend fun nhWithDraw(
        accessToken: String,
        deviceId: String,
        memberId: String,
        withDrawVo: WithDrawVo
    ): BaseDto = api.withDrawNH(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        withDrawVo = withDrawVo
    )

}
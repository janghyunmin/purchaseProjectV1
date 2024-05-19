package run.piece.dev.data.refactoring.ui.deposit.repository.remote

import retrofit2.Response
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deposit.dto.RegisterNhBankDto
import run.piece.domain.refactoring.deposit.model.NhBankHistoryVo
import run.piece.domain.refactoring.deposit.model.WithDrawVo

interface NhBankRemoteDataSource {

    // 연동계좌 및 가상계좌 생성
    suspend fun createAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: RegisterNhBankDto
    ): BaseDto


    // 연동계좌 변경
    suspend fun changeAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: RegisterNhBankDto
    ): BaseDto

    // 고객 입금 확인 조회
    suspend fun nhHistory(
        accessToken: String,
        deviceId: String,
        memberId: String
    ): Response<NhBankHistoryVo>

    // 출금 신청
    suspend fun nhWithDraw(
        accessToken: String,
        deviceId: String,
        memberId: String,
        withDrawVo: WithDrawVo
    ): BaseDto
}
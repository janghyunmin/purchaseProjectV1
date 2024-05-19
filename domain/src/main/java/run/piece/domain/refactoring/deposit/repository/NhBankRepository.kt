package run.piece.domain.refactoring.deposit.repository

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deposit.model.NhBankHistoryVo
import run.piece.domain.refactoring.deposit.model.NhBankRegisterVo
import run.piece.domain.refactoring.deposit.model.WithDrawVo

interface NhBankRepository {
    // 연동계좌 및 가상계좌 생성
    fun createAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: NhBankRegisterVo) : Flow<BaseVo>

    // 연동계좌 변경
    fun changeAccount(
        accessToken: String,
        deviceId: String,
        memberId: String,
        registerNhBankModel: NhBankRegisterVo) : Flow<BaseVo>

    // 고객 입금 확인 조회
    suspend fun nhHistory(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) : Response<NhBankHistoryVo>

    // 출금 신청
    fun nhBankWithDraw(
        accessToken: String,
        deviceId: String,
        memberId: String,
        withDrawModel: WithDrawVo
    ) : Flow<BaseVo>
}
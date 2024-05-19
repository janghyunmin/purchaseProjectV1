package run.piece.domain.refactoring.investment.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.investment.model.InvestMentVo
import run.piece.domain.refactoring.investment.model.InvestmentQuestionVo
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel

interface InvestMentRepository {
    fun postInvestMent(accessToken: String, deviceId: String, memberId: String, investBodyModel: InvestBodyModel): Flow<InvestMentVo>

    fun getInvestMent(): Flow<List<InvestmentQuestionVo>>
}
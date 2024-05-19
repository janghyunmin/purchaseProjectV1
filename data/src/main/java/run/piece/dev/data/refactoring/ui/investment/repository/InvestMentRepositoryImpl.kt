package run.piece.dev.data.refactoring.ui.investment.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.investment.mapper.mapperToInvestMentQuestionVo
import run.piece.dev.data.refactoring.ui.investment.mapper.mapperToInvestMentVo
import run.piece.dev.data.refactoring.ui.investment.repository.local.InvestMentLocalDataSource
import run.piece.dev.data.refactoring.ui.investment.repository.remote.InvestMentRemoteDataSource
import run.piece.domain.refactoring.investment.model.InvestMentVo
import run.piece.domain.refactoring.investment.model.InvestmentQuestionVo
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel
import run.piece.domain.refactoring.investment.repository.InvestMentRepository

class InvestMentRepositoryImpl(
    private val investMentLocalDataSource: InvestMentLocalDataSource,
    private val investMentRemoteDataSource: InvestMentRemoteDataSource): InvestMentRepository {

    override fun postInvestMent(accessToken: String, deviceId: String, memberId: String, investBodyModel: InvestBodyModel): Flow<InvestMentVo> = flow {
         emit(investMentRemoteDataSource.postInvestMent(accessToken = accessToken, deviceId = deviceId, memberId = memberId , investBodyModel = investBodyModel).data.mapperToInvestMentVo())
    }

    override fun getInvestMent(): Flow<List<InvestmentQuestionVo>> = flow {
        emit(investMentRemoteDataSource.getInvestMent().data.mapperToInvestMentQuestionVo())
    }
}
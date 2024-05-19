package run.piece.dev.data.refactoring.ui.investment.repository.remote

import run.piece.dev.data.refactoring.ui.investment.api.InvestMentApi
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentDto
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentQuestionDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel

class InvestMentRemoteDataSourceImpl(private val api: InvestMentApi): InvestMentRemoteDataSource {
    override suspend fun postInvestMent(accessToken: String, deviceId: String, memberId: String, investBodyModel: InvestBodyModel): WrappedResponse<InvestMentDto>
    = api.postInvestMent(accessToken = accessToken, deviceId = deviceId, memberId = memberId , investBodyModel = investBodyModel)

    override suspend fun getInvestMent(): WrappedResponse<List<InvestMentQuestionDto>> = api.getInvestMent()
}
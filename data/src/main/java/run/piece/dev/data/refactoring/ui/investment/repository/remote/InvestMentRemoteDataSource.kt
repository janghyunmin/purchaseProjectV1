package run.piece.dev.data.refactoring.ui.investment.repository.remote

import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentDto
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentQuestionDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel

interface InvestMentRemoteDataSource {
    suspend fun postInvestMent(accessToken: String, deviceId: String, memberId: String, investBodyModel: InvestBodyModel): WrappedResponse<InvestMentDto>

    suspend fun getInvestMent(): WrappedResponse<List<InvestMentQuestionDto>>
}
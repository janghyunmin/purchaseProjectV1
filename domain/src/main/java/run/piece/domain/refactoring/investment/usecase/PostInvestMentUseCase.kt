package run.piece.domain.refactoring.investment.usecase

import run.piece.domain.refactoring.investment.model.request.InvestBodyModel
import run.piece.domain.refactoring.investment.repository.InvestMentRepository

class PostInvestMentUseCase(private val repository: InvestMentRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId: String, investBodyModel: InvestBodyModel) = repository.postInvestMent(accessToken = accessToken , deviceId = deviceId , memberId = memberId , investBodyModel = investBodyModel)
}
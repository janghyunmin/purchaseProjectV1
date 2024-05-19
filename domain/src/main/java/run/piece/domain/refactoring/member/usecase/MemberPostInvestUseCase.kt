package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.investment.model.request.InvestRiskModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class MemberPostInvestUseCase(private val repository: MemberRepository) {
    operator fun invoke(
        accessToken: String,
        deviceId: String,
        memberId: String,
        investRiskModel: InvestRiskModel
    ) = repository.postInvestAgreement(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        investRiskModel = investRiskModel
    )
}
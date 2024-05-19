package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.repository.MemberRepository
import run.piece.domain.refactoring.portfolio.repository.PortfolioRepository

class MemberInfoGetUseCase(private val repository: MemberRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId: String) = repository.getMemberInfo(accessToken = accessToken, deviceId = deviceId, memberId = memberId)
}
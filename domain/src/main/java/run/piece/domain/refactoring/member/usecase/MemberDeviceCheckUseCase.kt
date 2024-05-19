package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.repository.MemberRepository

class MemberDeviceCheckUseCase(private val repository: MemberRepository) {
    operator fun invoke(memberId: String, deviceId: String, memberAppVersion: String) = repository.getMemberDeviceCheck(memberId = memberId, deviceId = deviceId, memberAppVersion = memberAppVersion)
}
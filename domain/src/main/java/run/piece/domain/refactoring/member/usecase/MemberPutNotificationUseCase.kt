package run.piece.domain.refactoring.member.usecase

import run.piece.domain.refactoring.member.model.request.NewMemberNotificationModel
import run.piece.domain.refactoring.member.repository.MemberRepository

class MemberPutNotificationUseCase(private val repository: MemberRepository) {
    operator fun invoke(accessToken: String, deviceId: String, memberId: String, model: NewMemberNotificationModel) =
        repository.memberPutNotification(accessToken = accessToken, deviceId = deviceId, memberId = memberId, model = model)
}
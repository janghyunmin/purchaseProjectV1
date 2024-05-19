package run.piece.domain.refactoring.deed.usecase

import run.piece.domain.refactoring.deed.model.MemberDocumentVo
import run.piece.domain.refactoring.deed.repository.DeedRepository
import run.piece.domain.refactoring.notice.repository.NoticeRepository

class SendEmailUseCase(private val repository: DeedRepository) {
    suspend operator fun invoke(accessToken: String,
                                deviceId: String,
                                memberId: String,
                                document: MemberDocumentVo) = repository.sendEmail(accessToken, deviceId, memberId, document)
}
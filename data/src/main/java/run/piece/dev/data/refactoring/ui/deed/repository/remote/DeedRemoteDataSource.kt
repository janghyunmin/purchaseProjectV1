package run.piece.dev.data.refactoring.ui.deed.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deed.model.MemberDocumentModel

interface DeedRemoteDataSource {
    suspend fun sendEmail(accessToken: String,
                          deviceId: String,
                          memberId: String,
                          document: MemberDocumentModel
    ): BaseDto
}
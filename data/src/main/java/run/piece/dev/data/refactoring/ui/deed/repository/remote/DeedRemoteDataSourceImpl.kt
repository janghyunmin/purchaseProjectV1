package run.piece.dev.data.refactoring.ui.deed.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.deed.api.DeedApi
import run.piece.dev.data.refactoring.ui.deed.model.MemberDocumentModel

class DeedRemoteDataSourceImpl(private val api: DeedApi) : DeedRemoteDataSource {
    override suspend fun sendEmail(
        accessToken: String,
        deviceId: String,
        memberId: String,
        document: MemberDocumentModel
    ): BaseDto = api.sendEmail(accessToken, deviceId, memberId, document)
}
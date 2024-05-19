package run.piece.domain.refactoring.deed.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deed.model.MemberDocumentVo

interface DeedRepository {
    suspend fun sendEmail(accessToken: String,
                          deviceId: String,
                          memberId: String,
                          document: MemberDocumentVo): Flow<BaseVo>
}
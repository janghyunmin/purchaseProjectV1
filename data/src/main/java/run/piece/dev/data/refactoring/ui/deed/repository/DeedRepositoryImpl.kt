package run.piece.dev.data.refactoring.ui.deed.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.base.mapperToBaseVo
import run.piece.dev.data.refactoring.ui.deed.mapper.mapperToMemberDocumentDTO
import run.piece.dev.data.refactoring.ui.deed.repository.local.DeedLocalDataSource
import run.piece.dev.data.refactoring.ui.deed.repository.remote.DeedRemoteDataSource
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.deed.model.MemberDocumentVo
import run.piece.domain.refactoring.deed.repository.DeedRepository

class DeedRepositoryImpl(private val deedRemoteDataSource: DeedRemoteDataSource,
                         private val deedLocalDataSource: DeedLocalDataSource): DeedRepository {

    override suspend fun sendEmail(
        accessToken: String,
        deviceId: String,
        memberId: String,
        document: MemberDocumentVo
    ): Flow<BaseVo> = flow {
        emit(deedRemoteDataSource.sendEmail(accessToken, deviceId, memberId, document.mapperToMemberDocumentDTO()).mapperToBaseVo())
    }
}
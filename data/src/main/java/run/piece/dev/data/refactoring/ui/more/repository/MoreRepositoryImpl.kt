package run.piece.dev.data.refactoring.ui.more.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.db.user.mapper.mapperToUserDto
import run.piece.dev.data.refactoring.db.user.mapper.mapperToUserEntity
import run.piece.dev.data.refactoring.db.user.mapper.mapperToUserVo
import run.piece.dev.data.refactoring.ui.more.repository.local.MoreLocalDataSource
import run.piece.dev.data.refactoring.ui.more.repository.remote.MoreRemoteDataSource
import run.piece.domain.refactoring.db.user.UserVo
import run.piece.domain.refactoring.more.repository.MoreRepository

class MoreRepositoryImpl(private val moreRemoteDataSource: MoreRemoteDataSource,
                         private val moreLocalDataSource: MoreLocalDataSource): MoreRepository {

    /* entity(서버에서 꺼내온 imutable & nullable 객체)
           dto(entity를 가공하기 위한 mutable & nullable 객체)
           vo(imutable & non-null 객체) */
    override fun getUserVo(): Flow<UserVo> = flow {
        val dto = moreLocalDataSource.getUserEntity().mapperToUserDto() //entity를 가공하기 위해서 dto로 변경한다.
        // 가공 예시
        //dto.birthDay = "1995.06.07"
        emit(dto.mapperToUserVo())
    }

    override fun setUserVo(userVo: UserVo) {
        val dto = userVo.mapperToUserDto() // vo를 가공하기 위해서 dto로 변환한다.
        //가공 예시
        //dto.birthDay = "1995.06.01"
        moreLocalDataSource.setUserEntity(dto.mapperToUserEntity())
    }
}
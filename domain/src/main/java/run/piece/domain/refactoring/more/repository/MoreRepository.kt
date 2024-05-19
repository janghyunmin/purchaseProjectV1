package run.piece.domain.refactoring.more.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.db.user.UserVo

interface MoreRepository {
    fun getUserVo():Flow<UserVo>
    fun setUserVo(userVo: UserVo)
}
package run.piece.dev.data.refactoring.ui.magazine.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.magazine.MemberMagazinePagingSource
import run.piece.dev.data.refactoring.ui.magazine.NotMemberMagazinePagingSource
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToBookMarkCountVo
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToBookMarkItemVo
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToMagazineBaseVo
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToMagazineDetailVo
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToMagazineItemVo
import run.piece.dev.data.refactoring.ui.magazine.mapper.mapperToMagazineTypeVo
import run.piece.dev.data.refactoring.ui.magazine.repository.local.MagazineLocalDataSource
import run.piece.dev.data.refactoring.ui.magazine.repository.remote.MagazineRemoteDataSource
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.repository.MagazineRepository
import run.piece.domain.refactoring.magazine.vo.BookMarkCountItemVo
import run.piece.domain.refactoring.magazine.vo.BookMarkCountVo
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineDetailVo
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineTypeVo

class MagazineRepositoryImpl(
    private val remoteDataSource: MagazineRemoteDataSource,
    private val localDataSource: MagazineLocalDataSource
) : MagazineRepository {
    companion object {
        const val PAGE_SIZE = 10
    }

    override fun getMagazineImg(): Flow<BaseVo> = flow {
        emit(remoteDataSource.getMagazineTopImg().mapperToMagazineBaseVo())
    }

    override fun getMagazineNotMember(magazineType: String, length: Int, page: Int): Flow<List<MagazineItemVo>> = flow {
        emit(remoteDataSource.getMagazineNotMember(magazineType = magazineType, length = length, page = page).data.magazines.default().mapperToMagazineItemVo())
    }

    override fun getMagazineMember(accessToken: String, deviceId: String, memberId: String, magazineType: String ,length: Int, page: Int): Flow<List<MagazineItemVo>> = flow {
        emit(remoteDataSource.getMagazineMember(
            accessToken = accessToken,
            deviceId = deviceId,
            memberId = memberId,
            magazineType = magazineType ,
            length = length,
            page = page).data.magazines.default().mapperToMagazineItemVo())
    }

    override fun getMagazineDetailNotMember(magazineId: String): Flow<MagazineDetailVo> = flow {
        emit(remoteDataSource.getMagazineDetailNotMember(magazineId = magazineId).data.mapperToMagazineDetailVo())
    }

    override fun getMagazineDetailMember(
        accessToken: String,
        deviceId: String,
        memberId: String,
        magazineId: String
    ): Flow<MagazineItemVo> = flow {
        emit(remoteDataSource.getMagazineDetailMember(
            accessToken = accessToken,
            deviceId = deviceId,
            memberId = memberId,
            magazineId = magazineId
        ).data.mapperToMagazineDetailVo())
    }


    // 회원 북마크 정보 조회
    override fun getBookMark(accessToken: String, deviceId: String, memberId: String): Flow<List<BookMarkItemVo>> = flow {
        emit(remoteDataSource.getBookMark(accessToken, deviceId, memberId).data.mapperToBookMarkItemVo())
    }


    // 회원 북마크 등록 요청
    override fun updateBookMark(accessToken: String, deviceId: String, memberId: String, memberBookmarkRegModel: MemberBookmarkRegModel): Flow<BookMarkCountItemVo> = flow {
        emit(remoteDataSource.updateBookMark(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberBookmarkRegModel = memberBookmarkRegModel).data.mapperToBookMarkCountVo())
    }

    // 회원 북마크 취소 요청
    override fun deleteBookMark(accessToken: String, deviceId: String, memberId: String, memberBookmarkRemoveModel: MemberBookmarkRemoveModel): Flow<BookMarkCountItemVo> = flow {
        emit(remoteDataSource.deleteBookMark(accessToken = accessToken, deviceId = deviceId, memberId = memberId, memberBookmarkRemoveModel = memberBookmarkRemoveModel).data.mapperToBookMarkCountVo())
    }

    override fun getMagazineType(): Flow<List<MagazineTypeVo>>  = flow {
        emit(remoteDataSource.getMagazineType().data.mapperToMagazineTypeVo())
    }
}
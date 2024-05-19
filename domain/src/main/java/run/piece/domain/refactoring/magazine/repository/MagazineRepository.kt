package run.piece.domain.refactoring.magazine.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.vo.BookMarkCountItemVo
import run.piece.domain.refactoring.magazine.vo.BookMarkCountVo
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineDetailVo
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineTypeVo

interface MagazineRepository {
    fun getMagazineImg(): Flow<BaseVo>

    fun getMagazineNotMember(magazineType: String, length: Int, page:Int): Flow<List<MagazineItemVo>>
    fun getMagazineMember(accessToken: String,deviceId: String,memberId: String,magazineType: String,length: Int, page:Int): Flow<List<MagazineItemVo>>

    fun getMagazineDetailNotMember(magazineId: String): Flow<MagazineDetailVo>

    fun getMagazineDetailMember(accessToken: String, deviceId: String, memberId: String, magazineId: String) : Flow<MagazineItemVo>

    // 회원 북마크 정보 조회
    fun getBookMark(
        accessToken: String,
        deviceId: String,
        memberId: String,
    ): Flow<List<BookMarkItemVo>>

    // 회원 북마크 등록 요청
    fun updateBookMark(accessToken: String, deviceId: String, memberId: String, memberBookmarkRegModel: MemberBookmarkRegModel): Flow<BookMarkCountItemVo>

    // 회원 북마크 삭제 요청
    fun deleteBookMark(accessToken: String, deviceId: String, memberId: String, memberBookmarkRemoveModel: MemberBookmarkRemoveModel): Flow<BookMarkCountItemVo>

    fun getMagazineType(): Flow<List<MagazineTypeVo>>
}
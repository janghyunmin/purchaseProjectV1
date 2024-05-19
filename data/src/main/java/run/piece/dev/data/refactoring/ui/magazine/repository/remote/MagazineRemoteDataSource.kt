package run.piece.dev.data.refactoring.ui.magazine.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.magazine.dto.*
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel

interface MagazineRemoteDataSource {
    suspend fun getMagazineTopImg() : BaseDto

    suspend fun getMagazineNotMember(
        magazineType: String,
        length: Int,
        page: Int): WrappedResponse<MagazineDto>

    suspend fun getMagazineMember(
        accessToken: String,
        deviceId: String,
        memberId: String,
        magazineType: String,
        length: Int,
        page: Int
    ): WrappedResponse<MagazineDto>

    suspend fun getMagazineDetailNotMember(
        magazineId: String
    ) : WrappedResponse<MagazineDetailDto>

    suspend fun getMagazineDetailMember(
        accessToken: String,
        deviceId: String,
        memberId: String,
        magazineId: String
    ) : WrappedResponse<MagazineItemDto>

    suspend fun getBookMark(
        accessToken: String,
        deviceId: String,
        memberId: String
    ): WrappedResponse<List<BookMarkDto>>

    suspend fun updateBookMark(
        accessToken: String,
        deviceId: String,
        memberId: String,
        memberBookmarkRegModel: MemberBookmarkRegModel
    ): BookMarkCountDto
    suspend fun deleteBookMark(
        accessToken: String,
        deviceId: String,
        memberId: String,
        memberBookmarkRemoveModel: MemberBookmarkRemoveModel
    ): BookMarkCountDto

    suspend fun getMagazineType(): WrappedResponse<List<MagazineTypeDto>>
}


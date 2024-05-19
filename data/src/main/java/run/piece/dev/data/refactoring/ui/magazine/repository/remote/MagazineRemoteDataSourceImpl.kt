package run.piece.dev.data.refactoring.ui.magazine.repository.remote

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.magazine.api.MagazineApi
import run.piece.dev.data.refactoring.ui.magazine.dto.*
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel

class MagazineRemoteDataSourceImpl(private val api: MagazineApi) : MagazineRemoteDataSource {

    // 매거진 상단 이미지 조회
    override suspend fun getMagazineTopImg(): BaseDto = api.getMagazineImg()

    override suspend fun getMagazineNotMember(magazineType: String, length: Int, page: Int
    ): WrappedResponse<MagazineDto> = api.getNotMemberMagazine(magazineType = magazineType, length = length, page = page)

    override suspend fun getMagazineMember(accessToken: String, deviceId: String, memberId: String, magazineType: String, length: Int, page: Int
    ): WrappedResponse<MagazineDto> = api.getMemberMagazine(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        magazineType = magazineType,
        length = length,
        page = page
    )

    // 비회원 라운지(매거진) 상세 조회
    override suspend fun getMagazineDetailNotMember(magazineId: String): WrappedResponse<MagazineDetailDto> =
        api.getNotMemberMagazineDetail(magazineId = magazineId)

    // 회원 라운지(매거진) 상세 조회
    override suspend fun getMagazineDetailMember(accessToken: String, deviceId: String, memberId: String, magazineId: String) : WrappedResponse<MagazineItemDto> =
        api.getMemberMagazineDetail(
            accessToken = accessToken,
            deviceId = deviceId,
            memberId = memberId,
            magazineId = magazineId
        )


    // 회원 북마크 정보 조회
    override suspend fun getBookMark(accessToken: String, deviceId: String, memberId: String): WrappedResponse<List<BookMarkDto>> = api.getBookMark(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId
    )

    override suspend fun updateBookMark(accessToken: String, deviceId: String, memberId: String, memberBookmarkRegModel: MemberBookmarkRegModel): BookMarkCountDto = api.updateBookMark(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        memberBookmarkRegModel = memberBookmarkRegModel
    )

    override suspend fun deleteBookMark(accessToken: String, deviceId: String, memberId: String, memberBookmarkRemoveModel: MemberBookmarkRemoveModel): BookMarkCountDto = api.deleteBookMark(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        memberBookmarkRemoveModel = memberBookmarkRemoveModel
    )

    override suspend fun getMagazineType(): WrappedResponse<List<MagazineTypeDto>> = api.getMagazineType()
}
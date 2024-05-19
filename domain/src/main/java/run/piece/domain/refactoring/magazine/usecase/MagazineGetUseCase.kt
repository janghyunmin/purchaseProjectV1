package run.piece.domain.refactoring.magazine.usecase

import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.repository.MagazineRepository

class MagazineGetUseCase(private val repository: MagazineRepository) {
    // 라운지 상단 이미지 조회
    fun getMagazineImg() = repository.getMagazineImg()

    // 비회원 라운지 리스트 조회
    fun getMagazineNotMember(magazineType: String, length: Int, page: Int) = repository.getMagazineNotMember(magazineType, length, page)

    // 회원 라운지 리스트 조회
    fun getMagazineMember(accessToken: String, deviceId: String, memberId: String, magazineType: String, length: Int, page: Int) =
        repository.getMagazineMember(
            accessToken = accessToken,
            deviceId = deviceId,
            memberId = memberId,
            magazineType = magazineType,
            length = length,
            page = page
        )

    // 비회원 라운지 상세 조회
    fun getMagazineDetailNotMember(magazineId: String) = repository.getMagazineDetailNotMember(magazineId = magazineId)

    // 회원 라운지 상세 조회
    fun getMagazineDetailMember(
        accessToken: String,
        deviceId: String,
        memberId: String,
        magazineId: String
    ) = repository.getMagazineDetailMember(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        magazineId = magazineId
    )

    // 북마크 조회
    fun getBookMark(
        accessToken: String,
        deviceId: String,
        memberId: String
    ) = repository.getBookMark(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId
    )

    // 회원 북마크 등록 요청
    fun updateBookmark(
        accessToken: String,
        deviceId: String,
        memberId: String,
        memberBookmarkRegModel: MemberBookmarkRegModel
    ) = repository.updateBookMark(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        memberBookmarkRegModel = memberBookmarkRegModel
    )

    // 회원 북마크 삭제 요청
    fun deleteBookMark(
        accessToken: String,
        deviceId: String,
        memberId: String,
        memberBookmarkRemoveModel: MemberBookmarkRemoveModel
    ) = repository.deleteBookMark(
        accessToken = accessToken,
        deviceId = deviceId,
        memberId = memberId,
        memberBookmarkRemoveModel = memberBookmarkRemoveModel
    )
}
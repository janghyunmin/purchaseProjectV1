package run.piece.dev.data.refactoring.ui.magazine.mapper

import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.magazine.dto.BookMarkCountDto
import run.piece.dev.data.refactoring.ui.magazine.dto.BookMarkDto
import run.piece.dev.data.refactoring.ui.magazine.dto.BookMarkCountItemDto
import run.piece.dev.data.refactoring.ui.magazine.dto.MagazineDetailDto
import run.piece.dev.data.refactoring.ui.magazine.dto.MagazineDto
import run.piece.dev.data.refactoring.ui.magazine.dto.MagazineItemDto
import run.piece.dev.data.refactoring.ui.magazine.dto.MagazineTypeDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo
import run.piece.domain.refactoring.magazine.vo.BookMarkCountItemVo
import run.piece.domain.refactoring.magazine.vo.BookMarkCountVo
import run.piece.domain.refactoring.magazine.vo.MagazineDetailVo
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineTypeVo
import run.piece.domain.refactoring.magazine.vo.MagazineVo


fun BaseDto.mapperToMagazineBaseVo(): BaseVo = BaseVo(this.status.default(), this.statusCode.default(), this.message.default(), this.subMessage.default(), this.data.default())
fun MagazineDto.mapperToMagazineVo(): MagazineVo {
    return MagazineVo(
        magazines = magazines?.mapperToMagazineItemVo()
    )
}

fun List<MagazineItemDto>.mapperToMagazineItemVo(): List<MagazineItemVo> {
    val list = arrayListOf<MagazineItemVo>()

    forEach {
        list.add(
            MagazineItemVo(
                it.magazineId.default(),
                it.magazineType.default(),
                it.title.default(),
                it.midTitle.default(),
                it.smallTitle.default(),
                it.author.default(),
                it.representThumbnailPath.default(),
                it.representImagePath.default(),
                it.contents.default(),
                it.isDelete.default(),
                it.createdAt.default(),
                it.shareUrl.default(),
                it.isFavorite.default()
            )
        )
    }
    return list
}

// 비회원 라운지 상세 조회
fun MagazineDetailDto.mapperToMagazineDetailVo() : MagazineDetailVo {
    return MagazineDetailVo(
        magazineId = this.magazineId.default(),
        magazineType = this.magazineType.default(),
        title = this.title.default(),
        midTitle = this.midTitle.default(),
        smallTitle = this.smallTitle.default(),
        author = this.author.default(),
        representThumbnailPath = this.representThumbnailPath.default(),
        representImagePath = this.representImagePath.default(),
        contents = this.contents.default(),
        isDelete = this.isDelete.default(),
        createdAt = this.createdAt.default(),
        shareUrl = this.shareUrl.default()
    )
}

fun MagazineItemDto.mapperToMagazineDetailVo() : MagazineItemVo {
    return MagazineItemVo(
        magazineId = this.magazineId.default(),
        magazineType = this.magazineType.default(),
        title = this.title.default(),
        midTitle = this.midTitle.default(),
        smallTitle = this.smallTitle.default(),
        author = this.author.default(),
        representThumbnailPath = this.representThumbnailPath.default(),
        representImagePath = this.representImagePath.default(),
        contents = this.contents.default(),
        isDelete = this.isDelete.default(),
        createdAt = this.createdAt.default(),
        shareUrl = this.shareUrl.default(),
        isFavorite = this.isFavorite.default()
    )
}

// 회원 북마크 정보 조회
fun List<BookMarkDto>.mapperToBookMarkItemVo(): List<BookMarkItemVo> {
    val list = arrayListOf<BookMarkItemVo>()
    forEach {
        list.add(
            BookMarkItemVo(
                it.memberId.default(),
                it.magazineId.default(),
                it.magazineType.default(),
                it.title.default(),
                it.midTitle.default(),
                it.smallTitle.default(),
                it.author.default(),
                it.representThumbnailPath.default(),
                it.representImagePath.default(),
                it.contents.default(),
                it.isDelete.default(),
                it.createdAt.default(),
                it.isFavorite.default()
            )
        )
    }
    return list
}


// 북마크 요청 / 취소 Mapper
fun BookMarkCountItemDto.mapperToBookMarkCountVo(): BookMarkCountItemVo = BookMarkCountItemVo(bookmarkCount = bookmarkCount.default())

fun List<MagazineTypeDto>.mapperToMagazineTypeVo(): List<MagazineTypeVo> {
    val list = arrayListOf<MagazineTypeVo>()
    forEach {
        list.add(
            MagazineTypeVo(
                magazineType = it.magazineType.default(),
                magazineTypeName = it.magazineTypeName.default()
            )
        )
    }
    return list
}

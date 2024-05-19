package run.piece.dev.data.refactoring.db.user.mapper

import run.piece.dev.data.refactoring.db.user.UserDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.db.user.UserEntity
import run.piece.domain.refactoring.db.user.UserVo

fun UserEntity?.mapperToUserDto(): UserDto =
    UserDto(name = this?.name.default(), this?.email.default(), this?.birthDay.default(), this?.cellPhoneNo.default(), this?.gender.default(), this?.pinNumber.default(), this?.joinDay.default(), this?.isFido.default())

fun UserDto?.mapperToUserVo(): UserVo =
    UserVo(name = this?.name.default(), this?.email.default(), this?.birthDay.default(), this?.cellPhoneNo.default(), this?.gender.default(), this?.pinNumber.default(), this?.joinDay.default(), this?.isFido.default())

fun UserVo?.mapperToUserDto(): UserDto =
    UserDto(name = this?.name.default(), this?.email.default(), this?.birthDay.default(), this?.cellPhoneNo.default(), this?.gender.default(), this?.pinNumber.default(), this?.joinDay.default(), this?.isFido.default())

fun UserDto?.mapperToUserEntity(): UserEntity =
    UserEntity(name = this?.name.default(), this?.email.default(), this?.birthDay.default(), this?.cellPhoneNo.default(), this?.gender.default(), this?.pinNumber.default(), this?.joinDay.default(), this?.isFido.default())
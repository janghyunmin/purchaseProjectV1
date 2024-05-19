package run.piece.dev.data.refactoring.ui.member.mapper


import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.member.model.dto.AuthPinDto
import run.piece.dev.data.refactoring.ui.member.model.dto.JoinDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberBookmarkDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberConsentDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDeleteDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDeviceDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberNotificationDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberNotificationItemDto
import run.piece.dev.data.refactoring.ui.member.model.dto.MemberPreferenceDto
import run.piece.dev.data.refactoring.ui.member.model.dto.PostSmsAuthDto
import run.piece.dev.data.refactoring.ui.member.model.dto.PostSmsVerificationDto
import run.piece.dev.data.refactoring.ui.member.model.dto.SsnDto
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.member.model.AuthPinVo
import run.piece.domain.refactoring.member.model.JoinVo
import run.piece.domain.refactoring.member.model.MemberBookmarkVo
import run.piece.domain.refactoring.member.model.MemberConsentVo
import run.piece.domain.refactoring.member.model.MemberDeleteVo
import run.piece.domain.refactoring.member.model.MemberDeviceVo
import run.piece.domain.refactoring.member.model.MemberNotificationItemVo
import run.piece.domain.refactoring.member.model.MemberNotificationVo
import run.piece.domain.refactoring.member.model.MemberPreferenceVo
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.PostSmsAuthVo
import run.piece.domain.refactoring.member.model.PostSmsVerificationVo
import run.piece.domain.refactoring.member.model.SsnVo


fun BaseDto.mapperToBaseVo() : BaseVo = BaseVo(this.status.default(),this.statusCode.default(), this.message.default(),this.subMessage.default(), this.data.default())

fun JoinDto.mapperToJoinVo(): JoinVo = JoinVo(
    memberId = memberId.default(),
    deviceId = deviceId.default(),
    accessToken = accessToken.default(),
    refreshToken = refreshToken.default(),
    expiredAt = expiredAt.default()
)

fun AuthPinDto.mapperToAuthPinVo(): AuthPinVo = AuthPinVo(
    memberId = this.memberId.default(),
    pinNumber = this.pinNumber.default(),
    passwordUpdatedAt = this.passwordUpdatedAt.default(),
    passwordIncorrectCount = this.passwordIncorrectCount.default(),
    isExistMember = this.isExistMember.default()
)

fun SsnDto.mapperToSsnVo(): SsnVo = SsnVo(
    publicKey = publicKey.default(),
    ssnYn = ssnYn.default()
)
fun MemberDto.mapperToMemberVo(): MemberVo = MemberVo(
    memberId = memberId.default(),
    name = name.default(),
    pinNumber = pinNumber.default(),
    cellPhoneIdNo = cellPhoneIdNo.default(),
    cellPhoneNo = cellPhoneNo.default(),
    birthDay = birthDay.default(),
    zipCode = zipCode.default(),
    baseAddress = baseAddress.default(),
    detailAddress = detailAddress.default(),
    idNo = idNo.default(),
    ci = ci.default(),
    di = di.default(),
    gender = gender.default(),
    email = email.default(),
    isFido = isFido.default(),
    ssn = ssn.default(),
    isIdNo = isIdNo.default(),
    createdAt = createdAt.default(),
    joinDay = joinDay.default(),
    totalProfitAmount = totalProfitAmount.default(),
    requiredConsentDate = requiredConsentDate.default(),
    notRequiredConsentDate = notRequiredConsentDate.default(),
    vran = vran.default(),
    invstDepsBal = invstDepsBal.default(),
    rtnAblAmt = rtnAblAmt.default(),
    publicKey = publicKey.default(),
    notification = notification.mapperToMemberNotificationVo(),
    device = device.mapperToBaseVo(),
    consents = consents.mapperToMemberConsentVo(),
    bookmarks = bookmarks.mapperToMemberBookmarkVo(),
    portfolioNotifications = portfolioNotifications.mapperToMemberNotificationVo(),
    preference = preference?.mapperToMemberPreferenceVo()
)

fun List<MemberBookmarkDto>?.mapperToMemberBookmarkVo(): List<MemberBookmarkVo> {
    val list = arrayListOf<MemberBookmarkVo>()
    this?.let {
        forEach {
            list.add(it.mapperToMemberBookmarkVo())
        }
    }
    return list
}

fun List<MemberConsentDto>?.mapperToMemberConsentVo(): List<MemberConsentVo> {
    val list = arrayListOf<MemberConsentVo>()
    this?.let {
        forEach {
            list.add(it.mapperToMemberConsentVo())
        }
    }
    return list
}

fun MemberNotificationDto?.mapperToMemberNotificationVo(): MemberNotificationVo =
    MemberNotificationVo(
        memberId = this?.memberId.default(),
        assetNotification = this?.assetNotification.default(),
        isAd = this?.isAd.default(),
        isNotice = this?.isNotice.default(),
        marketingApp = this?.marketingApp.default(),
        marketingSms = this?.marketingSms.default(),
        portfolioNotification = this?.portfolioNotification.default()
    )

fun MemberDeviceDto?.mapperToBaseVo(): MemberDeviceVo = MemberDeviceVo(
    memberId = this?.memberId.default(),
    deviceId = this?.deviceId.default(),
    deviceOs = this?.deviceOs.default(),
    deviceState = this?.deviceState.default(),
    fcmToken = this?.fcmToken.default(),
    fbToken = this?.fbToken.default()
)


fun MemberConsentDto?.mapperToMemberConsentVo(): MemberConsentVo = MemberConsentVo(
    memberId = this?.memberId.default(),
    consentCode = this?.consentCode.default(),
    isAgreement = this?.isAgreement.default()
)

fun MemberBookmarkDto?.mapperToMemberBookmarkVo(): MemberBookmarkVo = MemberBookmarkVo(
    memberId = this?.memberId.default(),
    magazineId = this?.magazineId.default(),
    magazineType = this?.magazineType.default(),
    title = this?.title.default(),
    midTitle = this?.midTitle.default(),
    smallTitle = this?.smallTitle.default(),
    author = this?.author.default(),
    representThumbnailPath = this?.representThumbnailPath.default(),
    representImagePath = this?.representImagePath.default(),
    contents = this?.contents.default(),
    isDelete = this?.isDelete.default(),
    createdAt = this?.createdAt.default(),
    isFavorite = this?.isFavorite.default()
)

fun List<MemberNotificationItemDto>?.mapperToMemberNotificationVo(): List<MemberNotificationItemVo> {
    val list = arrayListOf<MemberNotificationItemVo>()
    this?.let {
        forEach {
            list.add(it.mapperToMemberNotificationItemVo())
        }
    }
    return list
}
fun MemberNotificationItemDto?.mapperToMemberNotificationItemVo(): MemberNotificationItemVo = MemberNotificationItemVo(
    memberId = this?.memberId.default(), portfolioId = this?.portfolioId.default(), notificationAt = this?.notificationAt.default()
)

fun MemberPreferenceDto?.mapperToMemberPreferenceVo(): MemberPreferenceVo = MemberPreferenceVo(
    resultId = this?.resultId.default(),
    minScore = this?.minScore.default(),
    maxScore = this?.maxScore.default(),
    result = this?.result.default(),
    description = this?.description.default(),
    resultImagePath = this?.resultImagePath.default(),
    interestProductDescription = this?.interestProductDescription.default(),
    memberId = this?.memberId.default(),
    name = this?.name.default(),
    score = this?.score.default(),
    count = this?.count.default(),
    isVulnerableInvestors = this?.isVulnerableInvestors.default(),
    createdAt = this?.createdAt.default(),
)

fun PostSmsAuthDto?.mapperToPostAuthVo(): PostSmsAuthVo {
    return PostSmsAuthVo(
        txSeqNo = this?.txSeqNo.default(),
        rsltCd = this?.rsltCd.default(),
        cpCd = this?.cpCd.default(),
        mdlTkn = this?.mdlTkn.default(),
        rsltMsg = this?.rsltMsg.default(),
        telComCd = this?.telComCd.default(),
        resendCnt = this?.resendCnt.default())
}

//postSmsVerification

fun PostSmsVerificationDto?.mapperToPostSmsVerificationVo(): PostSmsVerificationVo {
    return PostSmsVerificationVo(
        rsltCd = this?.rsltCd.default(),
        rsltMsg = this?.rsltMsg.default(),
        cpCd = this?.cpCd.default(),
        txSeqNo = this?.txSeqNo.default(),
        di = this?.di.default(),
        ci = this?.ci.default(),
        ci2 = this?.ci2.default(),
        ciUpdate = this?.ciUpdate.default(),
        rsltName = this?.rsltName.default(),
        telNo = this?.telNo.default(),
        telComCd = this?.telComCd.default(),
        rsltBirthday = this?.rsltBirthday.default(),
        rsltSexCd = this?.rsltSexCd.default(),
        rsltNtvFrnrCd = this?.rsltNtvFrnrCd.default(),
        publicKey = this?.publicKey.default())
}

fun MemberDeleteDto?.mapperToMemberDeleteVo(code: Int): MemberDeleteVo {
    return MemberDeleteVo(status = this?.status.default(), statusCode = this?.statusCode.default(), message = this?.message.default(), subMessage = this?.subMessage.default(), data = this?.data.default(), code = code.default())
}
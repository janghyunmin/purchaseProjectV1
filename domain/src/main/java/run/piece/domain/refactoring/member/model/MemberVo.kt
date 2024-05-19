package run.piece.domain.refactoring.member.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

class JoinVo(
    val memberId: String,
    val deviceId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiredAt: String
)

class AuthPinVo(
    val memberId: String,
    val pinNumber: String,
    val passwordUpdatedAt: String,
    val passwordIncorrectCount: String,
    val isExistMember: String
)

class SsnVo(
    val publicKey: String,
    val ssnYn: String
)

@Parcelize
class MemberVo(
    val memberId: String,
    val name: String,
    val pinNumber: String,
    val cellPhoneNo: String,
    val cellPhoneIdNo: String,
    val birthDay: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val idNo: Int,
    val ci: String,
    val di: String,
    val gender: String,
    val email: String,
    val isFido: String,
    val ssn: String,
    val isIdNo: String,
    val createdAt: String,
    val joinDay: String,
    val totalProfitAmount: String,
    val requiredConsentDate: String,
    val notRequiredConsentDate: String,
    val vran: String,
    val invstDepsBal: String,
    val rtnAblAmt: String,
    val publicKey: String,
    val notification: MemberNotificationVo,
    val device: MemberDeviceVo,
    val consents: List<MemberConsentVo>,
    val bookmarks: List<MemberBookmarkVo>,
    val portfolioNotifications: List<MemberNotificationItemVo>,
    val preference: MemberPreferenceVo?,
) : Parcelable

@Parcelize
class MemberNotificationVo(
    val memberId: String,
    val assetNotification: String,
    val isAd: String,
    val isNotice: String,
    val marketingApp: String,
    val marketingSms: String,
    val portfolioNotification: String
) : Parcelable

@Parcelize
class MemberDeviceVo(
    val memberId: String,
    val deviceId: String,
    val deviceOs: String,
    val deviceState: String,
    val fcmToken: String,
    val fbToken: String
) : Parcelable

@Parcelize
class MemberConsentVo(
    val memberId: String,
    val consentCode: String,
    val isAgreement: String
) : Parcelable

@Parcelize
class MemberBookmarkVo(
    val memberId: String,
    val magazineId: String,
    val magazineType: String,
    val title: String,
    val midTitle: String,
    val smallTitle: String,
    val author: String,
    val representThumbnailPath: String,
    val representImagePath: String,
    val contents: String,
    val isDelete: String,
    val createdAt: String,
    val isFavorite: String
) : Parcelable

@Parcelize
class MemberNotificationItemVo(val memberId: String, val portfolioId: String, val notificationAt: String) : Parcelable

@Parcelize
class MemberPreferenceVo(
    val resultId: String,
    val minScore: Int,
    val maxScore: Int,
    val result: String,
    val description: String,
    val resultImagePath: String,
    val interestProductDescription: String,
    val memberId: String,
    val name: String,
    val score: Int,
    val count: Int,
    val isVulnerableInvestors: String,
    val createdAt: String,
) : Parcelable
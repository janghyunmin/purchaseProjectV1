package run.piece.dev.data.refactoring.ui.member.model.dto

import com.google.gson.annotations.SerializedName

class MemberDto(@SerializedName("memberId") var memberId: String?,
                @SerializedName("name") var name: String?,
                @SerializedName("pinNumber") var pinNumber: String?,
                @SerializedName("cellPhoneNo") var cellPhoneNo: String?,
                @SerializedName("cellPhoneIdNo") var cellPhoneIdNo: String?,
                @SerializedName("birthDay") var birthDay: String?,
                @SerializedName("zipCode") var zipCode: String?,
                @SerializedName("baseAddress") var baseAddress: String?,
                @SerializedName("detailAddress") var detailAddress: String?,
                @SerializedName("idNo") var idNo: Int?,
                @SerializedName("ci") var ci: String?,
                @SerializedName("di") var di: String?,
                @SerializedName("gender") var gender: String?,
                @SerializedName("email") var email: String?,
                @SerializedName("isFido") var isFido: String?,
                @SerializedName("ssn") var ssn: String?,
                @SerializedName("isIdNo") var isIdNo: String?,
                @SerializedName("createdAt") var createdAt: String?,
                @SerializedName("joinDay") var joinDay: String?,
                @SerializedName("totalProfitAmount") var totalProfitAmount: String?,
                @SerializedName("requiredConsentDate") var requiredConsentDate: String?,
                @SerializedName("notRequiredConsentDate") var notRequiredConsentDate: String?,
                @SerializedName("vran") var vran: String?,
                @SerializedName("invstDepsBal") var invstDepsBal: String?,
                @SerializedName("rtnAblAmt") var rtnAblAmt: String?,
                @SerializedName("publicKey") var publicKey: String?,
                @SerializedName("notification") var notification: MemberNotificationDto?,
                @SerializedName("device") var device: MemberDeviceDto?,
                @SerializedName("consents") var consents: List<MemberConsentDto>?,
                @SerializedName("bookmarks") var bookmarks: List<MemberBookmarkDto>?,
                @SerializedName("portfolioNotifications") var portfolioNotifications: List<MemberNotificationItemDto>?,
                @SerializedName("preference") var preference: MemberPreferenceDto?,
)

// 회원가입 DTO
class JoinDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("deviceId") var deviceId: String?,
    @SerializedName("accessToken") var accessToken: String?,
    @SerializedName("refreshToken") var refreshToken: String?,
    @SerializedName("expiredAt") var expiredAt: String?
)

// 회원 핀번호 검증 DTO
class AuthPinDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("pinNumber") var pinNumber: String?,
    @SerializedName("passwordUpdatedAt") var passwordUpdatedAt: String?,
    @SerializedName("passwordIncorrectCount") var passwordIncorrectCount: String?,
    @SerializedName("isExistMember") var isExistMember: String?
    )

// 실명인증 여부 조회 DTO
class SsnDto(
    @SerializedName("publicKey") var publicKey: String?,
    @SerializedName("ssnYn") var ssnYn: String?
)

class MemberNotificationDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("assetNotification") var assetNotification: String?,
    @SerializedName("isAd") var isAd: String?,
    @SerializedName("isNotice") var isNotice: String?,
    @SerializedName("marketingApp") var marketingApp: String?,
    @SerializedName("marketingSms") var marketingSms: String?,
    @SerializedName("portfolioNotification") var portfolioNotification: String?
)

class MemberDeviceDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("deviceId") var deviceId: String?,
    @SerializedName("deviceOs") var deviceOs: String?,
    @SerializedName("deviceState") var deviceState: String?,
    @SerializedName("fcmToken") var fcmToken: String?,
    @SerializedName("fbToken") var fbToken: String?
)

class MemberConsentDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("consentCode") var consentCode: String?,
    @SerializedName("isAgreement") var isAgreement: String?
)

class MemberBookmarkDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("magazineId") var magazineId: String?,
    @SerializedName("magazineType") var magazineType: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("midTitle") var midTitle: String?,
    @SerializedName("smallTitle") var smallTitle: String?,
    @SerializedName("author") var author: String?,
    @SerializedName("representThumbnailPath") var representThumbnailPath: String?,
    @SerializedName("representImagePath") var representImagePath: String?,
    @SerializedName("contents") var contents: String?,
    @SerializedName("isDelete") var isDelete: String?,
    @SerializedName("createdAt") var createdAt: String?,
    @SerializedName("isFavorite") var isFavorite: String?
)


class MemberNotificationItemDto(
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("portfolioId") var portfolioId: String?,
    @SerializedName("notificationAt") var notificationAt: String?)

class MemberPreferenceDto(
    @SerializedName("resultId") var resultId: String?,
    @SerializedName("minScore") var minScore: Int?,
    @SerializedName("maxScore") var maxScore: Int?,
    @SerializedName("result") var result: String?,
    @SerializedName("description") var description: String?,
    @SerializedName("resultImagePath") var resultImagePath: String?,
    @SerializedName("interestProductDescription") var interestProductDescription: String?,
    @SerializedName("memberId") var memberId: String?,
    @SerializedName("name") var name: String?,
    @SerializedName("score") var score: Int?,
    @SerializedName("count") var count: Int?,
    @SerializedName("isVulnerableInvestors") var isVulnerableInvestors: String?,
    @SerializedName("createdAt") var createdAt: String?,
)
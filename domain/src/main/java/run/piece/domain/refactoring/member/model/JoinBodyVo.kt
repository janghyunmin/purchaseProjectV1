package run.piece.domain.refactoring.member.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class JoinBodyVo(
    val name: String,
    val pinNumber: String,
    val cellPhoneNo: String,
    val birthDay: String,
    val ci: String,
    val di: String,
    val gender: String,
    val isFido: String,
    val publicKey: String,
    val ssn: String,
    val deviceInfo: DeviceInfo,
    val notificationInfo: NotificationInfo,
    val consents: List<Consents>
) : Parcelable

@Parcelize
class MemberPinModel(
    val memberId: String,
    val pinNumber: String
) : Parcelable

@Parcelize
class DeviceInfo(
    val deviceId: String,
    val deviceOs: String,
    val deviceState: String,
    val fcmToken: String,
    val fbToken: String
) : Parcelable

@Parcelize
class NotificationInfo(
    val assetNotification: String,
    val portfolioNotification: String,
    val marketingSms: String,
    val marketingApp: String
) : Parcelable

@Parcelize
class Consents(
    val consentCode: String,
    var isAgreement: String
) : Parcelable
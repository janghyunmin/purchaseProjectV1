package run.piece.domain.refactoring.member.model.request

data class MemberModifyModel(var memberId: String,
                             var name: String,
                             var pinNumber: String,
                             var cellPhoneNo: String,
                             var cellPhoneIdNo: String,
                             var birthDay: String,
                             var zipCode: String,
                             var baseAddress: String,
                             var detailAddress: String,
                             var ci: String,
                             var di: String,
                             var gender: String,
                             var email: String,
                             var isFido: String,
                             var notification: NotificationModel,
                             var consents: List<UpdateConsentItemModel>)

data class NotificationModel(var memberId: String,
                             var assetNotification: String,
                             var portfolioNotification: String,
                             var marketingSms: String,
                             var marketingApp: String,
                             var isAd: String,
                             var isNotice: String)

data class UpdateConsentItemModel(var memberId: String,
                                  var consentCode: String,
                                  var isAgreement: String)
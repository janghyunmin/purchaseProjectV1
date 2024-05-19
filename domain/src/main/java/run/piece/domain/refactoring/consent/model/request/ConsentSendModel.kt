package run.piece.domain.refactoring.consent.model.request

data class ConsentSendModel(val memberId: String, val consentCode: String, val isAgreement: String)
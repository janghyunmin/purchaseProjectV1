package run.piece.domain.refactoring.member.model.request

data class PostSmsVerificationModel(private val txSeqNo: String, private val telNo: String, private val otpNo: String, private val deviceId: String)
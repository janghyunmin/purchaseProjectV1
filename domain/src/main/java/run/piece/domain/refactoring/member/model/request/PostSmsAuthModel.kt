package run.piece.domain.refactoring.member.model.request

data class PostSmsAuthModel(val txSeqNo: String = "",
                            val name: String,
                            val birthday: String,
                            val sexCd: String,
                            val ntvFrnrCd: String,
                            val telComCd: String,
                            val telNo: String,
                            val agree1: String,
                            val agree2: String,
                            val agree3: String,
                            val agree4: String,
                            val otpNo: String)
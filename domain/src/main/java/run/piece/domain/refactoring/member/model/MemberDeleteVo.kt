package run.piece.domain.refactoring.member.model
data class MemberDeleteVo(val status: String,
                          val statusCode: Int,
                          val message: String,
                          val subMessage: String,
                          val data: Any,
                          val code: Int)
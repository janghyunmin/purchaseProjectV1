package run.piece.domain.refactoring
class BaseVo(val status: String?,
             val statusCode: Int?,
             val message: String?,
             val subMessage: String?,
             val data : Any?)

class PurchaseVo(
    val status: String?,
    val message: String?,
    val subMessage: String?,
    val data : Any?
)
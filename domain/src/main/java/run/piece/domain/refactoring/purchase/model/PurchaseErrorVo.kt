package run.piece.domain.refactoring.purchase.model

data class PurchaseErrorVo(
    val status: String,
    val statusCode: Int,
    val message: String,
    val data: PurchaseErrorItemVo
)

data class PurchaseErrorItemVo(
    val responseCode: Int,
    val message: String,
    val subMessage: String,
    val ptWithDrawDate: String?
)
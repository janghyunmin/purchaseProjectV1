package run.piece.dev.data.refactoring.ui.purchase.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseDto
import run.piece.dev.data.refactoring.ui.purchase.dto.PurchaseInfoDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.purchase.model.PurchaseCancelModel
import run.piece.domain.refactoring.purchase.model.PurchaseDefaultVo
import run.piece.domain.refactoring.purchase.model.PurchaseModel
import run.piece.domain.refactoring.purchase.model.PurchaseVo

interface PurchaseApi {
    // 청약 신청
    @POST("offer")
    suspend fun postPurchaseOffer(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body purchaseModel: PurchaseModel?
    ) : WrappedResponse<PurchaseDto?>

    @POST("offer")
    suspend fun newPurchaseOffer(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body purchaseModel: PurchaseModel?
    ) : Response<PurchaseDefaultVo?>

    @HTTP(method = "DELETE", path = "offer", hasBody = true)
    suspend fun cancelPurchase(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body purchaseCancelModel: PurchaseCancelModel?
    ) : BaseDto


    //청약 정보 조회
    @GET("offer")
    suspend fun getPurchaseInfo(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Query("portfolioId") portfolioId: String,
    ) : WrappedResponse<PurchaseInfoDto>
}
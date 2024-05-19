package run.piece.dev.data.refactoring.ui.investment.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentDto
import run.piece.dev.data.refactoring.ui.investment.dto.InvestMentQuestionDto
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.investment.model.request.InvestBodyModel

interface InvestMentApi {
    // 투자 성향 분석 요청 API
    @POST("investment-analysis")
    suspend fun postInvestMent(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Body investBodyModel: InvestBodyModel
    ): WrappedResponse<InvestMentDto>

    // 투자 성향 분석 질문/답변 조회
    @GET("investment-analysis")
    suspend fun getInvestMent(): WrappedResponse<List<InvestMentQuestionDto>>

}
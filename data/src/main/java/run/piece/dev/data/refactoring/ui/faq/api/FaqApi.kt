package run.piece.dev.data.refactoring.ui.faq.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.faq.dto.FaqListDto
import run.piece.dev.data.utils.WrappedResponse

interface FaqApi {
    @GET("board") suspend fun getFaqList(@Query("page") page: Int, @Query("boardType") boardType: String, @Query("boardCategory") boardCategory: String, @Header("apiVersion") apiVersion: String): WrappedResponse<FaqListDto>
}
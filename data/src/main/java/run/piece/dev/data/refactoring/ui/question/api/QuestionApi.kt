package run.piece.dev.data.refactoring.ui.question.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.question.dto.QuestionListDto
import run.piece.dev.data.utils.WrappedResponse

interface QuestionApi {
    @GET("board") suspend fun getQuestionList(@Query("page") page: Int, @Query("boardType") boardType: String, @Query("boardCategory") boardCategory: String, @Header("apiVersion") apiVersion: String): WrappedResponse<QuestionListDto>
}
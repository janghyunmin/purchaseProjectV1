package run.piece.dev.data.refactoring.ui.notice.api

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeItemDto
import run.piece.dev.data.refactoring.ui.notice.dto.NoticeListDto
import run.piece.dev.data.utils.WrappedResponse

interface NoticeApi {
    @GET("board") suspend fun getNoticeList(@Query("page") page: Int, @Query("boardType") boardType: String): WrappedResponse<NoticeListDto>
    @GET("board/{boardId}") suspend fun getNoticeDetail(@Path("boardId") boardId: String, @Header("apiVersion") apiVersion: String): WrappedResponse<NoticeItemDto>
}
package run.piece.dev.data.refactoring.ui.magazine.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import run.piece.dev.data.refactoring.base.BaseDto
import run.piece.dev.data.refactoring.ui.magazine.dto.*
import run.piece.dev.data.utils.WrappedResponse
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel

interface MagazineApi {

    // 라운지 상단 이미지 조회 API
    @GET("board/magazine/main")
    suspend fun getMagazineImg() : BaseDto

    // 비회원 라운지 리스트 조회
    @GET("board/magazine")
    suspend fun getNotMemberMagazine(
        @Query("magazineType") magazineType: String,
        @Query("length") length: Int,
        @Query("page") page: Int
    ): WrappedResponse<MagazineDto>

    // 비회원 매거진(리운지) 상세 조회
    @GET("board/magazine/{magazineId}")
    suspend fun getNotMemberMagazineDetail(
        @Path("magazineId") magazineId: String?
    ): WrappedResponse<MagazineDetailDto>

    // 회원 매거진(라운지) 상세 조회
    @GET("member/magazine/{magazineId}")
    suspend fun getMemberMagazineDetail(
        @Header("accessToken") accessToken: String?,
        @Header("deviceId") deviceId: String?,
        @Header("memberId") memberId: String?,
        @Path("magazineId") magazineId: String?
    ) : WrappedResponse<MagazineItemDto>


    // 회원 라운지 리스트 조회
    @GET("member/magazine")
    suspend fun getMemberMagazine(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Query("magazineType") magazineType: String,
        @Query("length") length: Int,
        @Query("page") page: Int
    ): WrappedResponse<MagazineDto>

    // 회원 북마크 조회
    @GET("member/bookmark")
    suspend fun getBookMark(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String
    ): WrappedResponse<List<BookMarkDto>>


    // 회원 북마크 등록 요청
    @POST("member/bookmark")
    suspend fun updateBookMark(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Body memberBookmarkRegModel: MemberBookmarkRegModel
    ): BookMarkCountDto


    // 회원 북마크 삭제 요청
    @HTTP(method = "DELETE" , path = "member/bookmark", hasBody = true)
    suspend fun deleteBookMark(
        @Header("accessToken") accessToken: String,
        @Header("deviceId") deviceId: String,
        @Header("memberId") memberId: String,
        @Body memberBookmarkRemoveModel: MemberBookmarkRemoveModel
    ): BookMarkCountDto

    @GET("board/magazine/type")
    suspend fun getMagazineType(): WrappedResponse<List<MagazineTypeDto>>
}
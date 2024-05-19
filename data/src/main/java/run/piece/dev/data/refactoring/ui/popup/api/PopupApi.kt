package run.piece.dev.data.refactoring.ui.popup.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import run.piece.dev.data.refactoring.ui.popup.dto.PopupDto
import run.piece.dev.data.utils.WrappedResponse

interface PopupApi {
    @GET("popup")
    suspend fun getPopup(
        @Query("popupType") popupType: String
    ) : WrappedResponse<PopupDto>
}
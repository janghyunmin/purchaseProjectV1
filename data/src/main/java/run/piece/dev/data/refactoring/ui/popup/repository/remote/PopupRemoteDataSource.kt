package run.piece.dev.data.refactoring.ui.popup.repository.remote

import retrofit2.Response
import run.piece.dev.data.refactoring.ui.popup.dto.PopupDto
import run.piece.dev.data.utils.WrappedResponse

interface PopupRemoteDataSource {
    // 메인 팝업 조회 요청
    suspend fun getPopup(
        popupType: String
    ) : WrappedResponse<PopupDto>
}
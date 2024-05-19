package run.piece.dev.data.refactoring.ui.popup.repository.remote

import retrofit2.Response
import run.piece.dev.data.refactoring.ui.popup.api.PopupApi
import run.piece.dev.data.refactoring.ui.popup.dto.PopupDto
import run.piece.dev.data.utils.WrappedResponse

class PopupRemoteDataSourceImpl(private val api: PopupApi) : PopupRemoteDataSource {
    override suspend fun getPopup(popupType: String): WrappedResponse<PopupDto> = api.getPopup(popupType = popupType)
}
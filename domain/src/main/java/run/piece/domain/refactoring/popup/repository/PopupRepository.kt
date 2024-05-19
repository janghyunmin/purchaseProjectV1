package run.piece.domain.refactoring.popup.repository

import kotlinx.coroutines.flow.Flow
import run.piece.domain.refactoring.popup.model.PopupVo


interface PopupRepository {
    fun getPopup(popupType: String) : Flow<PopupVo>
}
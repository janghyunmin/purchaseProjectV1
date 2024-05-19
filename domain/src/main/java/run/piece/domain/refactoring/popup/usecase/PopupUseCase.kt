package run.piece.domain.refactoring.popup.usecase

import run.piece.domain.refactoring.popup.repository.PopupRepository

class PopupUseCase(private val repository: PopupRepository) {
    fun openPopUp(popupType: String) = repository.getPopup(popupType = popupType)
}
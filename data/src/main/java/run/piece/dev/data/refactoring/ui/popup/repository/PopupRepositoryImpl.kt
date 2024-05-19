package run.piece.dev.data.refactoring.ui.popup.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import run.piece.dev.data.refactoring.ui.popup.mapper.mapperToPopupVo
import run.piece.dev.data.refactoring.ui.popup.repository.local.PopupLocalDataSource
import run.piece.dev.data.refactoring.ui.popup.repository.remote.PopupRemoteDataSource
import run.piece.domain.refactoring.popup.model.PopupVo
import run.piece.domain.refactoring.popup.repository.PopupRepository

class PopupRepositoryImpl(private val popupLocalDataSource: PopupLocalDataSource, private val popupRemoteDataSource: PopupRemoteDataSource) : PopupRepository {
    override fun getPopup(popupType: String): Flow<PopupVo>  = flow {
        emit(popupRemoteDataSource.getPopup(popupType = popupType).data.mapperToPopupVo())
    }
}
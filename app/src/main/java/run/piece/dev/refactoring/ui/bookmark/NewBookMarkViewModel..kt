package run.piece.dev.refactoring.ui.bookmark

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.utils.default
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.usecase.MagazineGetUseCase
import run.piece.domain.refactoring.magazine.vo.BookMarkCountItemVo
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo
import javax.inject.Inject

@HiltViewModel
class NewBookMarkViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider,
    private val magazineGetUseCase: MagazineGetUseCase
) : ViewModel() {

    val isLogin: String = PrefsHelper.read("isLogin", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private val _bookMarkList: MutableStateFlow<BookMarkState> = MutableStateFlow(BookMarkState.Init)
    val bookMarkList: StateFlow<BookMarkState> get() = _bookMarkList.asStateFlow()

    private val _updateBookmarkList: MutableStateFlow<MemberBookMarkState> = MutableStateFlow(MemberBookMarkState.Init)
    val updateBookmarkList: StateFlow<MemberBookMarkState> get() = _updateBookmarkList.asStateFlow()

    // 회원 북마크 정보 조회
    fun getBookMark() {
        viewModelScope.launch {
            magazineGetUseCase.getBookMark(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _bookMarkList.value = BookMarkState.IsLoading(true)
            }.catch { exception ->
                _bookMarkList.value = BookMarkState.IsLoading(false)
                _bookMarkList.value = BookMarkState.Failure(exception.message.default())
            }.collect {
                _bookMarkList.value = BookMarkState.IsLoading(false)
                _bookMarkList.value = BookMarkState.Success(it)
            }
        }
    }

    // 회원 북마크 취소 요청
    fun deleteBookMark(memberBookmarkRemoveModel: MemberBookmarkRemoveModel) {
        viewModelScope.launch {
            magazineGetUseCase.deleteBookMark(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                memberBookmarkRemoveModel = memberBookmarkRemoveModel
            ).onStart {
                _updateBookmarkList.value = MemberBookMarkState.IsLoading(true)
            }.catch { exception ->
                _updateBookmarkList.value = MemberBookMarkState.IsLoading(false)
                _updateBookmarkList.value = MemberBookMarkState.Failure(exception.message.default())
            }.collect {
                _updateBookmarkList.value = MemberBookMarkState.IsLoading(false)
                _updateBookmarkList.value = MemberBookMarkState.Success(it)
            }
        }
    }
    
    // 북마크 정보 조회 State
    sealed class BookMarkState {
        object Init : BookMarkState()
        data class IsLoading(val isLoading: Boolean) : BookMarkState()
        data class Success(val bookMarkList: List<BookMarkItemVo>) : BookMarkState()
        data class Failure(val message: String) : BookMarkState()
    }


    // 북마크 등록,취소 State
    sealed class MemberBookMarkState {
        object Init : MemberBookMarkState()
        data class IsLoading(val isLoading: Boolean) : MemberBookMarkState()
        data class Success(val data: BookMarkCountItemVo) : MemberBookMarkState()
        data class Failure(val message: String) : MemberBookMarkState()
    }
    
}
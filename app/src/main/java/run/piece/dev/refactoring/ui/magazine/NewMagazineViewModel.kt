package run.piece.dev.refactoring.ui.magazine

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.BaseVo
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRegModel
import run.piece.domain.refactoring.magazine.model.MemberBookmarkRemoveModel
import run.piece.domain.refactoring.magazine.usecase.MagazineGetUseCase
import run.piece.domain.refactoring.magazine.usecase.MagazineTypeGetUseCase
import run.piece.domain.refactoring.magazine.vo.BookMarkCountItemVo
import run.piece.domain.refactoring.magazine.vo.BookMarkItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineDetailVo
import run.piece.domain.refactoring.magazine.vo.MagazineItemVo
import run.piece.domain.refactoring.magazine.vo.MagazineTypeVo
import javax.inject.Inject

@HiltViewModel
class NewMagazineViewModel @Inject constructor(private val magazineGetUseCase: MagazineGetUseCase, private val magazineTypeGetUseCase: MagazineTypeGetUseCase) : ViewModel() {

    var requestPage = 1
    var tabType = ""
    var isLoading = false
    var lastPage = false
    var tabItemList = ArrayList<MagazineTypeVo>()

    val isLogin: String = PrefsHelper.read("isLogin", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val magazineImgUrl: String = PrefsHelper.read("magazineImgUrl", "")

    private val _magazineTopImg: MutableStateFlow<MagazineTopState> =
        MutableStateFlow(MagazineTopState.Init)
    val magazineTopImg: StateFlow<MagazineTopState> get() = _magazineTopImg.asStateFlow()

    private val _magazineNotMemberList: MutableStateFlow<MagazineState> = MutableStateFlow(MagazineState.Init)
    val magazineNotMemberList: StateFlow<MagazineState> get() = _magazineNotMemberList.asStateFlow()

    private val _magazineNotMemberRefreshList: MutableStateFlow<MagazineState> = MutableStateFlow(MagazineState.Init)
    val magazineNotMemberRefreshList: StateFlow<MagazineState> get() = _magazineNotMemberRefreshList.asStateFlow()

    private val _magazineMemberList: MutableStateFlow<MagazineState> = MutableStateFlow(MagazineState.Init)

    val magazineMemberList: StateFlow<MagazineState> get() = _magazineMemberList.asStateFlow()

    private val _magazineMemberRefreshList: MutableStateFlow<MagazineState> = MutableStateFlow(MagazineState.Init)
    val magazineMemberRefreshList: StateFlow<MagazineState> get() = _magazineMemberRefreshList.asStateFlow()

    private val _magazineDetailNotMember: MutableStateFlow<MagazineDetailNotMemberState> = MutableStateFlow(MagazineDetailNotMemberState.Init)
    val magazineDetailNotMember: StateFlow<MagazineDetailNotMemberState> get() = _magazineDetailNotMember.asStateFlow()

    private val _magazineDetailMember: MutableStateFlow<MagazineDetailMemberState> = MutableStateFlow(MagazineDetailMemberState.Init)
    val magazineDetailMember: StateFlow<MagazineDetailMemberState> get() = _magazineDetailMember.asStateFlow()

    private val _bookMarkList: MutableStateFlow<BookMarkState> = MutableStateFlow(BookMarkState.Init)
    val bookMarkList: StateFlow<BookMarkState> get() = _bookMarkList.asStateFlow()

    private val _updateBookmarkList: MutableStateFlow<MemberBookMarkState> = MutableStateFlow(MemberBookMarkState.Init)
    val updateBookmarkList: StateFlow<MemberBookMarkState> get() = _updateBookmarkList.asStateFlow()

    private val _magazineTypeList: MutableStateFlow<MagazineTypeState> = MutableStateFlow(MagazineTypeState.Init)
    val magazineTypeList: StateFlow<MagazineTypeState> get() = _magazineTypeList.asStateFlow()

    // 라운지 상단 이미지 조회
    fun getMagazineTopImg() {
        viewModelScope.launch {
            magazineGetUseCase.getMagazineImg()
                .onStart {
                    _magazineTopImg.value = MagazineTopState.IsLoading(true)
                }
                .catch { exception ->
                    _magazineTopImg.value = MagazineTopState.IsLoading(false)
                    _magazineTopImg.value = MagazineTopState.Failure(exception.message.default())
                }
                .collect {
                    if ("${it.data}".isNotBlank()) {
                        PrefsHelper.write("magazineImgUrl", it.data.toString()) // 내부 저장소 저장
                    }
                    _magazineTopImg.value = MagazineTopState.IsLoading(false)
                    _magazineTopImg.value = MagazineTopState.Success(it)
                }
        }
    }

    // 비회원 라운지 조회
    fun getNotMemberMagazine(magazineType: String, length: Int = 20, page: Int = 1, isRefresh: Boolean = false) {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                magazineGetUseCase.getMagazineNotMember(magazineType, length, page)
                    .onStart {
                        _magazineNotMemberList.value = MagazineState.IsLoading(true)
                    }
                    .catch { exception ->
                        _magazineNotMemberList.value = MagazineState.IsLoading(false)
                        _magazineNotMemberList.value = MagazineState.Failure(exception.message.default())
                    }
                    .collect {
                        _magazineNotMemberList.value = MagazineState.IsLoading(false)

                        if (!isRefresh) {
                            _magazineNotMemberList.value = MagazineState.Success(it)
                        } else {
                            _magazineNotMemberRefreshList.value = MagazineState.Success(it)
                        }

                        lastPage = if (it.isNotEmpty()) {
                            requestPage ++
                            false
                        } else true
                    }
            }
        }
    }

    // 회원 라운지 조회
    fun getMemberMagazine(magazineType: String, length: Int = 20, page: Int = 1, isRefresh: Boolean = false) {
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                magazineGetUseCase.getMagazineMember(
                    accessToken = "Bearer $accessToken",
                    deviceId = deviceId,
                    memberId = memberId,
                    magazineType = magazineType,
                    length = length,
                    page = page
                )
                    .onStart {
                        _magazineMemberList.value = MagazineState.IsLoading(true)
                    }
                    .catch { exception ->
                        _magazineMemberList.value = MagazineState.IsLoading(false)
                        _magazineMemberList.value = MagazineState.Failure(exception.message.default())
                    }
                    .collect {
                        _magazineMemberList.value = MagazineState.IsLoading(false)

                        if (!isRefresh) {
                            _magazineMemberList.value = MagazineState.Success(it)
                        } else {
                            _magazineMemberRefreshList.value = MagazineState.Success(it)
                        }

                        lastPage = if (it.isNotEmpty()) {
                            requestPage ++
                            false
                        } else true
                    }
            }
        }
    }

    // 비회원 라운지 상세 조회
    fun getMagazineDetailNotMember(magazineId: String) {
        viewModelScope.launch {
            magazineGetUseCase.getMagazineDetailNotMember(
                magazineId = magazineId
            )
                .onStart {
                    _magazineDetailNotMember.value = MagazineDetailNotMemberState.IsLoading(true)
                }
                .catch { exception ->
                    _magazineDetailNotMember.value = MagazineDetailNotMemberState.IsLoading(false)
                    _magazineDetailNotMember.value =
                        MagazineDetailNotMemberState.Failure(exception.message.default())
                }
                .collect {
                    _magazineDetailNotMember.value = MagazineDetailNotMemberState.IsLoading(false)
                    _magazineDetailNotMember.value = MagazineDetailNotMemberState.Success(it)
                }
        }
    }

    // 회원 라운지 상세 조회
    fun getMagazineDetailMember(magazineId: String) {
        viewModelScope.launch {
            magazineGetUseCase.getMagazineDetailMember(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                magazineId = magazineId
            ).onStart {
                _magazineDetailMember.value = MagazineDetailMemberState.IsLoading(true)
            }.catch { exception ->
                _magazineDetailMember.value = MagazineDetailMemberState.IsLoading(false)
                _magazineDetailMember.value = MagazineDetailMemberState.Failure(exception.message.default())
            }.collect {
                _magazineDetailMember.value = MagazineDetailMemberState.IsLoading(false)
                _magazineDetailMember.value = MagazineDetailMemberState.Success(it)
            }
        }
    }


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

    // 회원 북마크 등록 요청
    fun updateBookMark(memberBookmarkRegModel: MemberBookmarkRegModel) {
        viewModelScope.launch {
            magazineGetUseCase.updateBookmark(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                memberBookmarkRegModel = memberBookmarkRegModel
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

    //매거진 타입 리스트 호출
    fun getMagazineTypeList() {
        viewModelScope.launch {
            magazineTypeGetUseCase()
                .onStart {
                    _magazineTypeList.value = MagazineTypeState.IsLoading(true)
                }.catch { exception ->
                    _magazineTypeList.value = MagazineTypeState.IsLoading(false)
                    _magazineTypeList.value = MagazineTypeState.Failure(exception.message.default())
                }.collect {
                    _magazineTypeList.value = MagazineTypeState.IsLoading(false)
                    _magazineTypeList.value = MagazineTypeState.Success(it)
                }
        }
    }

    // 라운지 상단 이미지 조회 State
    sealed class MagazineTopState {
        object Init : MagazineTopState()
        data class IsLoading(val isLoading: Boolean) : MagazineTopState()
        data class Success(val isSuccess: BaseVo) : MagazineTopState()
        data class Failure(val message: String) : MagazineTopState()
    }


    // 라운지 조회 State
    sealed class MagazineState {
        object Init : MagazineState()
        data class IsLoading(val isLoading: Boolean) : MagazineState()
        data class Success(val magazineList: List<MagazineItemVo>) : MagazineState()
        data class Failure(val message: String) : MagazineState()
    }

    // 비회원 라운지 상세 조회 State
    sealed class MagazineDetailNotMemberState {
        object Init : MagazineDetailNotMemberState()
        data class IsLoading(val isLoading: Boolean) : MagazineDetailNotMemberState()
        data class Success(val data: MagazineDetailVo) : MagazineDetailNotMemberState()
        data class Failure(val message: String) : MagazineDetailNotMemberState()
    }

    // 회원 라운지 상세 조회 State
    sealed class MagazineDetailMemberState {
        object Init: MagazineDetailMemberState()
        data class IsLoading(val isLoading: Boolean) : MagazineDetailMemberState()
        data class Success(val data: MagazineItemVo) : MagazineDetailMemberState()
        data class Failure(val message: String) : MagazineDetailMemberState()
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

    // 매거진 타입 리스트 State
    sealed class MagazineTypeState {
        object Init : MagazineTypeState()
        data class IsLoading(val isLoading: Boolean) : MagazineTypeState()
        data class Success(val data: List<MagazineTypeVo>) : MagazineTypeState()
        data class Failure(val message: String) : MagazineTypeState()
    }
}


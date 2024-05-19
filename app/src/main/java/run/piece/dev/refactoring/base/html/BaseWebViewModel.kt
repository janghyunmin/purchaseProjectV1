package run.piece.dev.refactoring.base.html

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.utils.default
import run.piece.domain.refactoring.consent.model.ConsentVo
import run.piece.domain.refactoring.consent.usecase.ConsentListGetUseCase
import run.piece.domain.refactoring.event.model.EventDetailVo
import run.piece.domain.refactoring.event.usecase.EventDetailGetUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.model.request.MemberModifyModel
import run.piece.domain.refactoring.member.model.request.UpdateConsentItemModel
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.member.usecase.PutMemberUseCase
import run.piece.domain.refactoring.notice.model.NoticeItemVo
import run.piece.domain.refactoring.notice.usecase.NoticeDetailGetUseCase
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


@ViewModelScoped
enum class WebViewState(val id: Int, val title: String) {
    COMMON(0, "공통 상세"),
    LOUNGE(1, "라운지 상세"),
    STORY(2, "스토리 상세"),
    CONSENT(3, "약관 상세"),
    DISCLOSURE(4, "투자공시 상세"),
    NOTICE(5, "공지사항 상세"),
    EVENT(6, "이벤트 상세")
}


@HiltViewModel
class BaseWebViewModel @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
    private val savedStateHandle: SavedStateHandle,
    private val noticeDetailGetUseCase: NoticeDetailGetUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val consentListGetUseCase: ConsentListGetUseCase,
    private val putMemberUseCase: PutMemberUseCase,
    private val eventDetailGetUseCase: EventDetailGetUseCase

) : ViewModel() {
    val isLogin: String = PrefsHelper.read("isLogin", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val sendConsentList = ArrayList<UpdateConsentItemModel>()

    @SuppressLint("SimpleDateFormat")
    var mFormat = SimpleDateFormat("yyyy년 MM월 dd일")
    var mNow: Long = 0
    var mDate: Date? = null

    private val _noticeDetail: MutableStateFlow<NoticeDetailState> = MutableStateFlow(NoticeDetailState.Init)
    val noticeDetail: StateFlow<NoticeDetailState> get() = _noticeDetail.asStateFlow()

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()

    private val _consentList: MutableStateFlow<ConsentState> = MutableStateFlow(ConsentState.Init)
    val consentList: StateFlow<ConsentState> = _consentList.asStateFlow()

    private val _putMember: MutableStateFlow<PutMemberState> = MutableStateFlow(PutMemberState.Init)
    val putMember: StateFlow<PutMemberState> = _putMember.asStateFlow()

    private val _eventDetail: MutableStateFlow<EventDetailState> = MutableStateFlow(EventDetailState.Init)
    val eventDetail: StateFlow<EventDetailState> get() = _eventDetail.asStateFlow()

    init {
        viewModelScope.launch {
            startType()
        }
    }

    fun startType(): Int {
        when (savedStateHandle.get<String>("viewName")) {
            "공통 상세" -> {
                return WebViewState.COMMON.id
            }

            "라운지 상세" -> {
                return WebViewState.LOUNGE.id
            }

            "스토리 상세" -> {
                return WebViewState.STORY.id
            }

            "약관 상세" -> {
                return WebViewState.CONSENT.id
            }

            "투자공시 상세" -> {
                return WebViewState.DISCLOSURE.id
            }

            "공지사항 상세" -> {
                return WebViewState.NOTICE.id
            }

            "이벤트 상세" -> {
                return WebViewState.EVENT.id
            }
        }

        return WebViewState.COMMON.id
    }

    fun updateTime(): String {
        mNow = System.currentTimeMillis()
        mDate = Date(mNow)
        return mFormat.format(mDate)
    }

    fun getNoticeDetail(boardId: String, apiVersion: String) {
        viewModelScope.launch {
            noticeDetailGetUseCase(boardId, apiVersion)
                .onStart {
                    _noticeDetail.value = NoticeDetailState.IsLoading(true)
                }
                .catch { exception ->
                    _noticeDetail.value = NoticeDetailState.IsLoading(false)
                    _noticeDetail.value = NoticeDetailState.Failure(exception.message.default())
                }
                .collect {
                    _noticeDetail.value = NoticeDetailState.IsLoading(false)
                    _noticeDetail.value = NoticeDetailState.Success(it)
                }
        }
    }

    fun getMemberData() {
        viewModelScope.launch {
            memberInfoGetUseCase(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _memberInfo.value = MemberInfoState.Loading(true)
            }.catch { exception ->
                _memberInfo.value = MemberInfoState.Loading(false)
                _memberInfo.value = MemberInfoState.Failure(exception.message.toString())
            }.collect {
                _memberInfo.value = MemberInfoState.Loading(false)
                _memberInfo.value = MemberInfoState.Success(it)
            }
        }
    }

    fun getConsentList() {
        viewModelScope.launch {
            consentListGetUseCase("", "v0.0.2")
                .onStart {
                    _consentList.value = ConsentState.Loading(true)
                }
                .catch { exception ->
                    _consentList.value = ConsentState.Loading(false)
                    _consentList.value = ConsentState.Failure(exception.message.toString())
                }
                .collect {
                    _consentList.value = ConsentState.Loading(false)
                    _consentList.value = ConsentState.Success(it)
                }
        }
    }

    fun putMember(model: MemberModifyModel) {
        viewModelScope.launch {
            val map = HashMap<String, String>()
            map["accessToken"] = "Bearer $accessToken"
            map["deviceId"] = deviceId
            map["memberId"] = memberId

            putMemberUseCase(
                headers = map,
                model = model
            ).onStart {
                _putMember.value = PutMemberState.Loading(true)
            }.catch { exception ->
                _putMember.value = PutMemberState.Loading(false)
                _putMember.value = PutMemberState.Failure(exception.message.toString())
            }.collect {
                _putMember.value = PutMemberState.Loading(false)
                _putMember.value = PutMemberState.Success(it)
            }
        }
    }

    fun getEventDetail() {
        savedStateHandle.get<String>("eventId")?.let {
            viewModelScope.launch {
                eventDetailGetUseCase(it)
                    .onStart {
                        _eventDetail.value = EventDetailState.IsLoading(true)
                    }.catch { exception ->
                        _eventDetail.value = EventDetailState.IsLoading(false)
                        _eventDetail.value = EventDetailState.Failure(exception.message.toString())
                    }.collect {
                        _eventDetail.value = EventDetailState.IsLoading(false)
                        _eventDetail.value = EventDetailState.Success(it)
                    }
            }
        }
    }

    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }

    sealed class ConsentState {
        object Init : ConsentState()
        data class Loading(val isLoading: Boolean) : ConsentState()
        data class Success(val consentList: List<ConsentVo>) : ConsentState()
        data class Failure(val message: String) : ConsentState()
    }

    sealed class PutMemberState {
        object Init : PutMemberState()
        data class Loading(val isLoading: Boolean) : PutMemberState()
        data class Success(val memberVo: MemberVo) : PutMemberState()
        data class Failure(val message: String) : PutMemberState()
    }

    sealed class NoticeDetailState {
        object Init : NoticeDetailState()
        data class IsLoading(val isLoading: Boolean) : NoticeDetailState()
        data class Success(val noticeDetailVo: NoticeItemVo) : NoticeDetailState()
        data class Failure(val message: String) : NoticeDetailState()
    }

    sealed class EventDetailState {
        object Init : EventDetailState()
        data class IsLoading(val isLoading: Boolean) : EventDetailState()
        data class Success(val eventDetailVo: EventDetailVo) : EventDetailState()
        data class Failure(val message: String) : EventDetailState()
    }
}
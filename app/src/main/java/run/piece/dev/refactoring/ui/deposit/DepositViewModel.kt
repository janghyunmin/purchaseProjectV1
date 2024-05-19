package run.piece.dev.refactoring.ui.deposit

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.refactoring.module.ResourcesProvider
import run.piece.dev.data.utils.default
import run.piece.dev.refactoring.ui.address.AddressViewModel
import run.piece.dev.refactoring.ui.main.MainViewModel
import run.piece.dev.refactoring.ui.wallet.BankIconState
import run.piece.domain.refactoring.deposit.model.AccountVo
import run.piece.domain.refactoring.deposit.model.DepositBalanceVo
import run.piece.domain.refactoring.deposit.model.HistoryItemVo
import run.piece.domain.refactoring.deposit.model.PurchaseVo
import run.piece.domain.refactoring.deposit.model.PurchaseVoV2
import run.piece.domain.refactoring.deposit.usecase.DepositBalanceUseCase
import run.piece.domain.refactoring.deposit.usecase.DepositHistoryUseCase
import run.piece.domain.refactoring.deposit.usecase.DepositPurchaseUseCase
import run.piece.domain.refactoring.deposit.usecase.GetMemberAccountUseCase
import run.piece.domain.refactoring.member.model.MemberVo
import run.piece.domain.refactoring.member.usecase.MemberDeviceCheckUseCase
import run.piece.domain.refactoring.member.usecase.MemberInfoGetUseCase
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailVo
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val resourcesProvider: ResourcesProvider,
    private val memberDeviceCheckUseCase: MemberDeviceCheckUseCase,
    private val memberInfoGetUseCase: MemberInfoGetUseCase,
    private val getMemberAccountUseCase: GetMemberAccountUseCase,
    private val depositBalanceUseCase: DepositBalanceUseCase,
    private val depositHistoryUseCase: DepositHistoryUseCase,
    private val depositPurchaseUseCase: DepositPurchaseUseCase
) : ViewModel() {

    private val appVersion: String = PrefsHelper.read("appVersion", "")
    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")
    val isLogin: String = PrefsHelper.read("isLogin","")

    private val _deviceChk: MutableStateFlow<MemberDeviceState> = MutableStateFlow(MemberDeviceState.Init)
    val deviceChk: StateFlow<MemberDeviceState> = _deviceChk.asStateFlow()

    private val _memberInfo: MutableStateFlow<MemberInfoState> = MutableStateFlow(MemberInfoState.Init)
    val memberInfo: StateFlow<MemberInfoState> = _memberInfo.asStateFlow()
    
    private val _getAccountInfo: MutableStateFlow<GetMemberAccountState> = MutableStateFlow(GetMemberAccountState.Init)
    val getAccountInfo: StateFlow<GetMemberAccountState> get() = _getAccountInfo.asStateFlow()

    private val _depositBalance: MutableStateFlow<DepositBalanceState> = MutableStateFlow(DepositBalanceState.Init)
    val depositBalance: StateFlow<DepositBalanceState> get() = _depositBalance.asStateFlow()

    private val _historyList: MutableStateFlow<DepositHistoryState> = MutableStateFlow(DepositHistoryState.Init)
    val historyList: StateFlow<DepositHistoryState> get() = _historyList.asStateFlow()

    private val _purchaseListV1: MutableStateFlow<DepositPurchaseV1State> = MutableStateFlow(DepositPurchaseV1State.Init)
    val purchaseListV1: StateFlow<DepositPurchaseV1State> get() = _purchaseListV1.asStateFlow()

    private val _purchaseListV2: MutableStateFlow<DepositPurchaseV2State> = MutableStateFlow(DepositPurchaseV2State.Init)
    val purchaseListV2: StateFlow<DepositPurchaseV2State> get() = _purchaseListV2.asStateFlow()

    fun memberDeviceChk() {
        viewModelScope.launch {
            memberDeviceCheckUseCase(
                memberId = memberId,
                deviceId = deviceId,
                memberAppVersion = appVersion
            ).onStart {
                _deviceChk.value = MemberDeviceState.Loading(true)
            }.catch { exception ->
                _deviceChk.value = MemberDeviceState.Loading(false)
                _deviceChk.value = MemberDeviceState.Failure(exception.message.toString())
            }.collect {
                _deviceChk.value = MemberDeviceState.Loading(false)
                _deviceChk.value = MemberDeviceState.Success(it.default())
            }
        }
    }

    // 회원 정보 조회
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

    // 회원 계좌 정보 조회
    fun getAccountInfo() {
        viewModelScope.launch {
            getMemberAccountUseCase.getMemberAccount(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _getAccountInfo.value = GetMemberAccountState.IsLoading(true)
            }.catch { exception ->
                _getAccountInfo.value = GetMemberAccountState.IsLoading(false)
                _getAccountInfo.value = GetMemberAccountState.Failure(exception.message.toString())
            }.collect {
                _getAccountInfo.value = GetMemberAccountState.IsLoading(false)
                _getAccountInfo.value = GetMemberAccountState.Success(it)
            }
        }
    }

    // 예치금 잔액 조회
    fun getDepositBalance() {
        viewModelScope.launch {
            depositBalanceUseCase.getDepositBalance(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId
            ).onStart {
                _depositBalance.value = DepositBalanceState.IsLoading(true)
            }.catch { exception ->
                _depositBalance.value = DepositBalanceState.IsLoading(false)
                _depositBalance.value = DepositBalanceState.Failure(exception.message.toString())
            }.collect {
                _depositBalance.value = DepositBalanceState.IsLoading(false)
                _depositBalance.value = DepositBalanceState.Success(it)
            }
        }
    }


    // 거래내역 조회
    fun getHistory(apiVersion: String, searchDvn: String, page: Int) {
        viewModelScope.launch {
            // searchDvn - ALL : 전체 / DEPOSIT : 입금 / WITHDRAW : 출금
            depositHistoryUseCase.getDepositHistory(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                apiVersion = apiVersion,
                searchDvn = searchDvn,
                page = page
            ).onStart {
                _historyList.value = DepositHistoryState.IsLoading(true)
            }.catch { exception ->
                _historyList.value = DepositHistoryState.IsLoading(false)
                _historyList.value = DepositHistoryState.Failure(exception.message.toString())
            }.collect {
                _historyList.value = DepositHistoryState.IsLoading(false)
                _historyList.value = DepositHistoryState.Success(it)
            }
        }
    }


    // 내지갑 - 소유조각 상세 API
    fun getDepositPurchaseV1(apiVersion: String) {
        viewModelScope.launch {
            depositPurchaseUseCase.getDepositPurchaseV1(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                apiVersion = apiVersion
            ).onStart {
                _purchaseListV1.value = DepositPurchaseV1State.IsLoading(true)
            }.catch { exception ->
                _purchaseListV1.value = DepositPurchaseV1State.IsLoading(false)
                _purchaseListV1.value = DepositPurchaseV1State.Failure(exception.message.toString())
            }.collect {
                _purchaseListV1.value = DepositPurchaseV1State.IsLoading(false)
                _purchaseListV1.value = DepositPurchaseV1State.Success(it)
            }
        }
    }

    // 내지갑 - 소유조각 목록 API
    fun getDepositPurchaseV2(apiVersion: String) {
        viewModelScope.launch {
            depositPurchaseUseCase.getDepositPurchaseV2(
                accessToken = "Bearer $accessToken",
                deviceId = deviceId,
                memberId = memberId,
                apiVersion = apiVersion
            ).onStart {
                _purchaseListV2.value = DepositPurchaseV2State.IsLoading(true)
            }.catch { exception ->
                _purchaseListV2.value = DepositPurchaseV2State.IsLoading(false)
                _purchaseListV2.value = DepositPurchaseV2State.Failure(exception.message.toString())
            }.collect {
                _purchaseListV2.value = DepositPurchaseV2State.IsLoading(false)
                _purchaseListV2.value = DepositPurchaseV2State.Success(it)
            }
        }
    }

    fun accountScheme(accountNo: String) : String {
        var schemeNumber = accountNo.substring(accountNo.length - 4 , accountNo.length)
        var decryption = "********"
        schemeNumber = decryption + schemeNumber
        return schemeNumber
    }
    fun getBankIconInit(context: Context, bankCode: String, targetView: AppCompatImageView) {
        when(BankIconState.getBankCode(code = bankCode)) {
            BankIconState.BANK001.code -> { }
            BankIconState.BANK002.code -> { Glide.with(context).load(R.drawable.bank02).into(targetView) }
            BankIconState.BANK003.code -> { Glide.with(context).load(R.drawable.bank03).into(targetView) }
            BankIconState.BANK004.code -> { Glide.with(context).load(R.drawable.bank04).into(targetView) }
            BankIconState.BANK007.code -> { Glide.with(context).load(R.drawable.bank07).into(targetView) }
            BankIconState.BANK008.code -> { }
            BankIconState.BANK011.code -> { Glide.with(context).load(R.drawable.bank11).into(targetView) }
            BankIconState.BANK012.code -> { }
            BankIconState.BANK020.code -> { Glide.with(context).load(R.drawable.bank20).into(targetView) }
            BankIconState.BANK021.code -> {  }
            BankIconState.BANK023.code -> { Glide.with(context).load(R.drawable.bank23).into(targetView) }
            BankIconState.BANK026.code -> { Glide.with(context).load(R.drawable.bank26).into(targetView) }
            BankIconState.BANK027.code -> { Glide.with(context).load(R.drawable.bank27).into(targetView) }
            BankIconState.BANK031.code -> { Glide.with(context).load(R.drawable.bank31).into(targetView) }
            BankIconState.BANK032.code -> { Glide.with(context).load(R.drawable.bank32).into(targetView) }
            BankIconState.BANK034.code -> { Glide.with(context).load(R.drawable.bank34).into(targetView) }
            BankIconState.BANK035.code -> { Glide.with(context).load(R.drawable.bank35).into(targetView) }
            BankIconState.BANK037.code -> { Glide.with(context).load(R.drawable.bank37).into(targetView) }
            BankIconState.BANK039.code -> { Glide.with(context).load(R.drawable.bank39).into(targetView) }
            BankIconState.BANK045.code -> { Glide.with(context).load(R.drawable.bank45).into(targetView) }
            BankIconState.BANK047.code -> { Glide.with(context).load(R.drawable.bank47).into(targetView) }
            BankIconState.BANK064.code -> { Glide.with(context).load(R.drawable.bank64).into(targetView) }
            BankIconState.BANK071.code -> { Glide.with(context).load(R.drawable.bank71).into(targetView) }
            BankIconState.BANK081.code -> { Glide.with(context).load(R.drawable.bank05).into(targetView) }
            BankIconState.BANK089.code -> { Glide.with(context).load(R.drawable.bank89).into(targetView) }
            BankIconState.BANK090.code -> { Glide.with(context).load(R.drawable.bank90).into(targetView) }
            BankIconState.BANK092.code -> { Glide.with(context).load(R.drawable.bank92).into(targetView) }
        }
    }

    sealed class MemberDeviceState {
        object Init : MemberDeviceState()
        data class Loading(val isLoading: Boolean) : MemberDeviceState()
        data class Success(val isSuccess: Any) : MemberDeviceState()
        data class Failure(val message: String) : MemberDeviceState()
    }


    // 계좌 정보 조회 State
    sealed class GetMemberAccountState {
        object Init: GetMemberAccountState()
        data class IsLoading(val isLoading: Boolean) : GetMemberAccountState()
        data class Success(val data : AccountVo) : GetMemberAccountState()
        data class Failure(val message: String) : GetMemberAccountState()
    }

    // 예치금 잔액 조회 State
    sealed class DepositBalanceState {
        object Init : DepositBalanceState()
        data class IsLoading(val isLoading: Boolean) : DepositBalanceState()
        data class Success(val data: DepositBalanceVo) : DepositBalanceState()
        data class Failure(val message: String) : DepositBalanceState()
    }

    // 거래내역 조회 State
    sealed class DepositHistoryState {
        object Init: DepositHistoryState()
        data class IsLoading(val isLoading: Boolean) : DepositHistoryState()
        data class Success(val data: List<HistoryItemVo>) : DepositHistoryState()
        data class Failure(val message: String) : DepositHistoryState()
    }

    // 구매 목록 조회 요청 V1 State
    sealed class DepositPurchaseV1State {
        object Init: DepositPurchaseV1State()
        data class IsLoading(val isLoading: Boolean) : DepositPurchaseV1State()
        data class Success(val data: List<PurchaseVo>) : DepositPurchaseV1State()
        data class Failure(val message: String) : DepositPurchaseV1State()
    }

    // 구매 목록 조회 요청 V2 State
    sealed class DepositPurchaseV2State {
        object Init: DepositPurchaseV2State()
        data class IsLoading(val isLoading: Boolean) : DepositPurchaseV2State()
        data class Success(val data: List<PurchaseVoV2>) : DepositPurchaseV2State()
        data class Failure(val message: String) : DepositPurchaseV2State()
    }

    // 회원 정보 조회
    sealed class MemberInfoState {
        object Init : MemberInfoState()
        data class Loading(val isLoading: Boolean) : MemberInfoState()
        data class Success(val memberVo: MemberVo) : MemberInfoState()
        data class Failure(val message: String) : MemberInfoState()
    }
}
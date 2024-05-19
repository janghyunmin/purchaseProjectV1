package run.piece.dev.refactoring.datastore

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import run.piece.domain.refactoring.datastore.DataStoreRepository
import javax.inject.Inject

// DataStore 공통 ViewModel
@HiltViewModel
class DataNexusViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    /**** DataStore Shared 방식 ****/
    // 1. companion object 에 Key 값을 셋팅한다.
    // 2. 사용할 키값을 넣어 put , get 사용
    // DataStore KeySetting List
    companion object {
        const val EMAIL = "email"
        const val CON_1501 = "CON1501"
        const val LOGIN_KEY = "IsLogin"
        const val PASSWORD_KEY = "inputPinNumber"
        const val DEVICEID_KEY = "deviceId"
        const val USERNAME_KEY = "name"
        const val ACCESSTOKEN_KEY = "accessToken"
        const val EXPIREAT_KEY = "expiredAt"
        const val MEMBERID_KEY = "memberId"
        const val REFRESHTOKEN_KEY = "refreshToken"
        const val PASSWORD_CHANGE_MODAL = "passCodeModal"
        const val IS_FIDO_KEY = "isFido"
        const val PURCHASE_AGREE_TIME = "agree"
        const val INVEST_SCREEN_STEP = "launchStep"
        const val INVEST_BTN_STATUS = "investBtn"
        const val INVEST_SCORE = "investScore"
        const val INVEST_RESULT = "investResult"
        const val INVEST_FINAL_SCORE = "investFinalScore"
        const val INVEST_VULNERABLE = "vulnerable"
        const val INVEST_FINAL_VULNERABLE = "vulnerableFinal"
    }

    // 로그인 유/무 저장
    fun putIsLoginKey(value: String) {
        viewModelScope.launch {
            repository.putString(LOGIN_KEY, value)
        }
    }

    fun getIsLogin(): String = runBlocking {
        repository.getString(LOGIN_KEY)
    }

    // 간편 비밀번호 저장
    fun putPassWord(value: String) {
        viewModelScope.launch {
            repository.putString(PASSWORD_KEY, value)
        }
    }

    fun getPassWord(): String = runBlocking {
        repository.getString(PASSWORD_KEY)
    }

    // 휴대폰 DeviceId 저장
    fun putDeviceId(value: String) {
        viewModelScope.launch {
            repository.putString(DEVICEID_KEY, value)
        }
    }

    fun getDeviceId(): String = runBlocking {
        repository.getString(DEVICEID_KEY)
    }

    // 회원 이름 저장
    fun putName(value: String) {
        viewModelScope.launch {
            repository.putString(USERNAME_KEY, value)
        }
    }

    fun getName(): String = runBlocking {
        repository.getString(USERNAME_KEY)
    }

    // AccessToken 저장
    fun putAccessToken(value: String) {
        viewModelScope.launch {
            repository.putString(ACCESSTOKEN_KEY, value)
        }
    }

    fun getAccessToken(): String = runBlocking {
        repository.getString(ACCESSTOKEN_KEY)
    }

    // 토큰 만료 시간 저장
    fun putExpiredAt(value: String) {
        viewModelScope.launch {
            repository.putString(EXPIREAT_KEY, value)
        }
    }

    fun getExpiredAt(): String = runBlocking {
        repository.getString(EXPIREAT_KEY)
    }

    // 회원 ID 저장
    fun putMemberId(value: String) {
        viewModelScope.launch {
            repository.putString(MEMBERID_KEY, value)
        }
    }

    fun getMemberId(): String = runBlocking {
        repository.getString(MEMBERID_KEY)
    }

    // refreshToken 저장
    fun putRefreshToken(value: String) {
        viewModelScope.launch {
            repository.putString(REFRESHTOKEN_KEY, value)
        }
    }

    fun getRefreshToken(): String = runBlocking {
        repository.getString(REFRESHTOKEN_KEY)
    }

    // 간편비밀번호 재설정 모달
    fun putPassWordModal(value: String) {
        viewModelScope.launch {
            repository.putString(PASSWORD_CHANGE_MODAL, value)
        }
    }

    fun getPassWordModal(): String = runBlocking {
        repository.getString(PASSWORD_CHANGE_MODAL)
    }

    fun putIsFido(value: String) {
        viewModelScope.launch {
            repository.putString(IS_FIDO_KEY, value)
        }
    }

    fun getIsFido(): String = runBlocking {
        repository.getString(IS_FIDO_KEY)
    }

    fun putPurchaseAgreeTime(value: String) {
        viewModelScope.launch {
            repository.putString(PURCHASE_AGREE_TIME, value)
        }
    }

    fun getPurchaseAgreeTime(): String = runBlocking {
        repository.getString(PURCHASE_AGREE_TIME)
    }


    fun putInvestBtnStatus(value: String) {
        viewModelScope.launch {
            repository.putString(INVEST_BTN_STATUS, value)
        }
    }

    fun getInvestBtnStatus(): String = runBlocking {
        repository.getString(INVEST_BTN_STATUS)
    }

    fun putInvestScreenStep(value: Int) {
        viewModelScope.launch {
            repository.putInt(INVEST_SCREEN_STEP,value)
        }
    }
    fun getInvestScreenStep(): Int = runBlocking {
        repository.getInt(INVEST_SCREEN_STEP)
    }

    fun putInvestScore(value: Int){
        viewModelScope.launch {
            repository.putInt(INVEST_SCORE, value)
        }
    }
    fun getInvestScore() : Int = runBlocking {
        repository.getInt(INVEST_SCORE)
    }

    val investFirstStatus: LiveData<Boolean> = repository.getInvestFirstStatus().asLiveData()
    fun setInvestFirstStatus(investFirstStatus:Boolean) = viewModelScope.launch {
        repository.setInvestFirstStatus(key = investFirstStatus)
    }


    // 내정보 조회를 통한 결과값 저장
    // 투자성향 결과 저장
    fun putInvestResult(value: String) {
        viewModelScope.launch {
            repository.putString(INVEST_RESULT,value)
        }
    }

    fun getInvestResult() : String = runBlocking {
        repository.getString(INVEST_RESULT)
    }

    fun putInvestFinalScore(value: Int) {
        viewModelScope.launch {
            repository.putInt(INVEST_FINAL_SCORE, value)
        }
    }

    fun getInvestFinalScore() : Int = runBlocking {
        repository.getInt(INVEST_FINAL_SCORE)
    }


    fun putVulnerable(value: String) {
        viewModelScope.launch {
            repository.putString(INVEST_VULNERABLE, value)
        }
    }

    fun getVulnerable(): String = runBlocking {
        repository.getString(INVEST_VULNERABLE)
    }

    fun putFinalVulnerable(value: String) {
        viewModelScope.launch {
            repository.putString(INVEST_FINAL_VULNERABLE, value)
        }
    }

    fun getFinalVulnerable(): String = runBlocking {
        repository.getString(INVEST_FINAL_VULNERABLE)
    }

    /**** DataStore Flow 방식 ****/
    // 앱 첫 실행시 판별 ( 테스트용 )
    val isFirstLaunch: LiveData<Boolean> = repository.getFirstLaunch().asLiveData()
    fun setFirstLaunch(isFirstLaunch: Boolean) = viewModelScope.launch {
        repository.setFirstLaunch(key = isFirstLaunch)
    }

    // 기존 IsLogin Migration
    fun setFbToken(
        fbToken: String
    ) = flow {
        repository.setFbToken(fbToken = fbToken)
        emit(EventState.SetInit)
    }

    fun getFbToken() = flow {
        val result = repository.getFbToken()
        val fbToken = result.getOrNull().orEmpty()
        emit(EventState.GetSuccess(fbToken))
    }

    fun putCON1501(value: String) {
        viewModelScope.launch {
            repository.putString(CON_1501, value)
        }
    }

    fun putEmail(value: String) {
        viewModelScope.launch {
            repository.putString(EMAIL, value)
        }
    }
}

// 뷰와 상호작용 하는 이벤트
sealed class EventState {
    object SetInit : EventState()
    class GetSuccess(
        val fbToken: String
    ) : EventState()
}
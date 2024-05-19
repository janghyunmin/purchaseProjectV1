package run.piece.dev.data.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.AccountDTO
import run.piece.dev.data.repository.AccountRepository

// 회원 계좌 정보 요청 ViewModel
class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: AccountRepository = AccountRepository(application)
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    val accountResponse: MutableLiveData<AccountDTO> = MutableLiveData()
    @SuppressLint("CheckResult")
    val accessToken: String = PrefsHelper.read("accessToken","")
    val deviceId: String = PrefsHelper.read("deviceId","")
    val memberId:String = PrefsHelper.read("memberId","")

    fun getAccount(accessToken: String, deviceId: String, memberId: String) {
        // 뷰모델이 사라지면 코루틴도 같이 삭제 - jhm 2022/09/20
        viewModelScope.launch(Dispatchers.IO) {
            val response = repo.getMemberAccount(accessToken = "Bearer $accessToken", deviceId = deviceId, memberId = memberId)
            try {
                if(response.status.equals("OK")) {
                    accountResponse.postValue(response)
                } else {
                    accountResponse.postValue(response)
                }
            } catch(e: Exception){
                e.printStackTrace()
            }
        }
    }
}
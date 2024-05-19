package run.piece.dev.data.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.DepositDTO
import run.piece.dev.data.repository.DepositBalanceRepository

/**
 *packageName    : com.bsstandard.piece.data.viewmodel
 * fileName       : DepositBalanceViewModel
 * author         : piecejhm
 * date           : 2022/09/20
 * description    : 회원 예치금 잔액 조회 요청 ViewModel
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/09/20        piecejhm       최초 생성
 */



class DepositBalanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: DepositBalanceRepository = DepositBalanceRepository(application)
    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    val depoResponse: MutableLiveData<DepositDTO> = MutableLiveData()
    @SuppressLint("CheckResult")
    val accessToken: String = PrefsHelper.read("accessToken","")
    val deviceId: String = PrefsHelper.read("deviceId","")
    val memberId:String = PrefsHelper.read("memberId","")

    fun getDepositBalance(accessToken: String, deviceId: String, memberId: String) {
        // 뷰모델이 사라지면 코루틴도 같이 삭제 - jhm 2022/09/20
        viewModelScope.launch(Dispatchers.IO) {
            val response = repo.getDepositBalance(accessToken = "Bearer $accessToken", deviceId = deviceId, memberId = memberId)
            try {
                if(response.status.equals("OK")) {
                    depoResponse.postValue(response)

                } else {
                    depoResponse.postValue(response)
                }
            } catch(e: Exception){
                e.printStackTrace()

            }
        }
    }

}
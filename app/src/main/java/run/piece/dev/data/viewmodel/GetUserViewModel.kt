package run.piece.dev.data.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.MemberDTO
import run.piece.dev.data.repository.GetUserRepository

/**
 *packageName    : com.bsstandard.piece.data.viewmodel
 * fileName       : GetUserViewModel
 * author         : piecejhm
 * date           : 2022/09/06
 * description    : 회원 정보 조회 ViewModel
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/09/06        piecejhm       최초 생성
 */

class GetUserViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val EVENT_MY_INFO_ACTIVITY = "0001"
    }

    private val repo: GetUserRepository = GetUserRepository(application)

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val memberArrayList: ArrayList<String> = arrayListOf()

    private val _detailResponse: MutableLiveData<MemberDTO> = MutableLiveData()
    val detailResponse: LiveData<MemberDTO> get() = _detailResponse


    private val _liveAddress = MutableLiveData<String>()
    val liveAddress: LiveData<String> get() = _liveAddress

    private val _liveDetailAddress = MutableLiveData<String>()
    val liveDetailAddress: LiveData<String> get() = _liveDetailAddress

    private val _liveEmail = MutableLiveData<String>()
    val liveEmail: LiveData<String> get() = _liveEmail

    // Notification LiveData - jhm 2022/09/21
    private var _liveNotification = MutableLiveData<MemberDTO.Data.Notification>()
    val liveNotification: LiveData<MemberDTO.Data.Notification> get() = _liveNotification


    // joinDay - jhm 2022/11/28
    private val _liveDay = MutableLiveData<String>()
    val liveDay: LiveData<String> get() = _liveDay


    // 분배금 정산을 받기 위한 상태값 조회 데이터 - jhm 2022/10/26
    private val _isIdNo = MutableLiveData<String>()
    val isIdNo: LiveData<String> get() = _isIdNo

    // 분배금 정산 금액 - jhm 2022/10/26
    private val _totalProfitAmount = MutableLiveData<String>()
    val totalProfitAmount: LiveData<String> get() = _totalProfitAmount

    // NH 나의 예치금 잔액 변수
    private val _invstDepsBal = MutableLiveData<String>()
    val invstDepsBal: LiveData<String> get() = _invstDepsBal

    // NH 나의 출금 가능 금액 변수
    private val _rtnAblAmt = MutableLiveData<String>()
    val rtnAblAmt: LiveData<String> get() = _rtnAblAmt

    // 정보성 알림 수신 - jhm 2023/01/26
    private var _isNotice = MutableLiveData<String?>()
    val isNotice : LiveData<String?> get() = _isNotice

    // 광고성 알림 수신 - jhm 2023/01/26
    private var _isAd = MutableLiveData<String?>()
    val isAd : LiveData<String?> get() = _isAd

    // 가상계좌 번호
    private var _vran = MutableLiveData<String?>()
    val vran : LiveData<String?> get() = _vran


    @SuppressLint("CheckResult")
    fun getUserData() {
        repo.getUser()
            ?.subscribeOn(Schedulers.io())
            ?.retry(1)
            ?.observeOn(AndroidSchedulers.mainThread())


        repo.getUser()?.subscribe(
            { MemberDTO ->
                PrefsHelper.write("memberId", MemberDTO?.data?.memberId)
                PrefsHelper.write("name", MemberDTO?.data?.name)
                PrefsHelper.write("pinNumber", MemberDTO?.data?.pinNumber.toString())
                PrefsHelper.write("cellPhoneNo", MemberDTO?.data?.cellPhoneNo)
                PrefsHelper.write("cellPhoneIdNo", MemberDTO?.data?.cellPhoneIdNo)
                PrefsHelper.write("birthDay", MemberDTO?.data?.birthDay)
                PrefsHelper.write("zipCode", MemberDTO?.data?.zipCode.toString())
                PrefsHelper.write("baseAddress", MemberDTO?.data?.baseAddress.toString())
                PrefsHelper.write("detailAddress", MemberDTO?.data?.detailAddress.toString())
                PrefsHelper.write("idNo", MemberDTO?.data?.idNo.toString())
                PrefsHelper.write("ci", MemberDTO?.data?.ci)
                PrefsHelper.write("di", MemberDTO?.data?.di)
                PrefsHelper.write("gender", MemberDTO?.data?.gender)
                PrefsHelper.write("email", MemberDTO?.data?.email.toString())
                PrefsHelper.write("gender", MemberDTO?.data?.gender)

//                PrefsHelper.write("isFido", MemberDTO?.data?.isFido)

                PrefsHelper.write("createdAt", MemberDTO?.data?.createdAt)
                PrefsHelper.write("joinDay", MemberDTO?.data?.joinDay)
                PrefsHelper.write("vran", MemberDTO?.data?.vran)

                // 필수약관 동의 시간 저장 - jhm 2023/01/04
                PrefsHelper.write("requiredConsentDate",MemberDTO?.data?.requiredConsentDate)
                // 선택 약관 동의 시간 저장 - jhm 2023/01/04
                PrefsHelper.write("notRequiredConsentDate",MemberDTO?.data?.notRequiredConsentDate)



                _vran.value = MemberDTO?.data?.vran // 가상계좌 번호
                _invstDepsBal.value = MemberDTO?.data?.invstDepsBal // NH 나의 예치금 잔액
                _rtnAblAmt.value = MemberDTO?.data?.rtnAblAmt // NH 나의 출금 가능 금액

                PrefsHelper.write("assetNotification",MemberDTO?.data?.notification?.assetNotification)
                PrefsHelper.write("portfolioNotification",MemberDTO?.data?.notification?.portfolioNotification)
                PrefsHelper.write("marketingSms",MemberDTO?.data?.notification?.marketingSms)
                PrefsHelper.write("marketingApp",MemberDTO?.data?.notification?.marketingApp)

                PrefsHelper.write("isAd",MemberDTO?.data?.notification?.isAd)
                PrefsHelper.write("isNotice",MemberDTO?.data?.notification?.isNotice)

                _isNotice.value = MemberDTO?.data?.notification?.isNotice
                _isAd.value = MemberDTO?.data?.notification?.isAd


                // room db 저장 - jhm 2022/09/18
//                var newUser = User(
//                    MemberDTO?.data?.name.toString(),
//                    PrefsHelper.read("inputPinNumber", ""),
//                    MemberDTO?.data?.cellPhoneNo.toString(),
//                    MemberDTO?.data?.birthDay.toString(),
//                    MemberDTO?.data?.gender.toString(),
//                    MemberDTO?.data?.isFido.toString()
//                )
//
//                // 싱글톤 - jhm 2022/09/18
//                val db = AppDatabase.getInstance(context)
//                CoroutineScope(Dispatchers.IO).launch {
//                    db!!.userDao().deleteUserByName(MemberDTO?.data?.name.toString())
//                    db.userDao().insert(newUser)
//                }


                _detailResponse.value = MemberDTO
                _detailResponse.postValue(MemberDTO)

                _liveAddress.value = MemberDTO?.data?.baseAddress.toString()
                _liveAddress.postValue(MemberDTO?.data?.baseAddress.toString())

                _liveDetailAddress.value = MemberDTO?.data?.detailAddress.toString()
                _liveDetailAddress.postValue(MemberDTO?.data?.detailAddress.toString())

                _liveEmail.value = MemberDTO?.data?.email.toString()
                _liveEmail.postValue(MemberDTO?.data?.email.toString())


                _liveDay.value = MemberDTO?.data?.joinDay.toString()
                _liveDay.postValue(MemberDTO?.data?.joinDay.toString())

                _isIdNo.value = MemberDTO?.data?.isIdNo
                _isIdNo.postValue(MemberDTO?.data?.isIdNo)


                _totalProfitAmount.value = MemberDTO?.data?.totalProfitAmount
                _totalProfitAmount.postValue(MemberDTO?.data?.totalProfitAmount)


            }, { throwable ->
                throwable.printStackTrace()
                throwable.message
            }
        )
    }

}
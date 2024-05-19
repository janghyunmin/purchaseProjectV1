package run.piece.dev.view.bank

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.base.BaseActivity
import run.piece.dev.data.viewmodel.GetUserViewModel
import run.piece.dev.databinding.ActivityAccountSelectBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.NetworkConnection

// 계좌 등록하기 (은행 선택) Activity
@AndroidEntryPoint
class BankSelectActivity :
    BaseActivity<ActivityAccountSelectBinding>(R.layout.activity_account_select) {
    private lateinit var mvm: GetUserViewModel // 내 정보 조회
    private val mContext: Context = this@BankSelectActivity
    private var vranNo: String = ""
    private var bankCode: String = ""
    private var bankName: String = ""

    companion object {
        const val TAG: String = "BankRegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        val networkConnection = NetworkConnection(this)
        networkConnection.observe(this) { isConnected ->
            if (!isConnected) {
                val intent = Intent(this, NetworkActivity::class.java)
                startActivity(intent)
            }
        }

        binding.apply {
            binding.lifecycleOwner = this@BankSelectActivity
            mvm = ViewModelProvider(this@BankSelectActivity)[GetUserViewModel::class.java]

            setStatusBarIconColor(true) // 상태바 아이콘 true : 검정색
            setStatusBarBgColor("#ffffff") // 상태바 배경색상 설정
            setNaviBarIconColor(true) // 네비게이션 true : 검정색
            setNaviBarBgColor("#ffffff") // 네비게이션 배경색
        }

        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // 최초에 회원 정보 조회 ViewModel 에서 데이터를 받아옴
        mvm.getUserData()

        // 가상계좌 번호
        mvm.vran.observe(this@BankSelectActivity,Observer {
            try {
                if(it.isNullOrEmpty()) {
                    vranNo = ""
                    binding.topTitle.text = getString(R.string.account_text)
                    binding.title.text = "연동계좌 등록"
                }
                else {
                    vranNo = it.toString()
                    binding.topTitle.text = getString(R.string.account_change_text)
                    binding.title.text = "은행 선택"
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        })

        // 은행 선택 후 출금계좌 등록으로 이동 - jhm 2022/10/05
        selectBank()

        binding.backImgIv.setOnClickListener {
            BackPressedUtil().activityFinish(this@BankSelectActivity,this@BankSelectActivity)
        }

        BackPressedUtil().activityCreate(this@BankSelectActivity,this@BankSelectActivity)
        BackPressedUtil().systemBackPressed(this@BankSelectActivity,this@BankSelectActivity)
    }

    private fun selectBank() {
        binding.bankLayout1.setOnClickListener {
            bankCode = "004"
            bankName = "KB 국민은행"
            bankRegister()
        }

        binding.bankLayout2.setOnClickListener {
            bankCode = "011"
            bankName = "NH농협은행"
            bankRegister()
        }

        binding.bankLayout3.setOnClickListener {
            bankCode = "026"
            bankName = "신한은행"
            bankRegister()
        }

        binding.bankLayout4.setOnClickListener {
            bankCode = "039"
            bankName = "경남은행"
            bankRegister()
        }

        binding.bankLayout5.setOnClickListener {
            bankCode = "034"
            bankName = "광주은행"
            bankRegister()
        }

        binding.bankLayout6.setOnClickListener {
            bankCode = "031"
            bankName = "대구은행"
            bankRegister()
        }

        binding.bankLayout7.setOnClickListener {
            bankCode = "032"
            bankName = "부산은행"
            bankRegister()
        }

        binding.bankLayout8.setOnClickListener {
            bankCode = "007"
            bankName = "수협은행"
            bankRegister()
        }

        binding.bankLayout9.setOnClickListener {
            bankCode = "020"
            bankName = "우리은행"
            bankRegister()
        }

        binding.bankLayout10.setOnClickListener {
            bankCode = "037"
            bankName = "전북은행"
            bankRegister()
        }

        binding.bankLayout11.setOnClickListener {
            bankCode = "035"
            bankName = "제주은행"
            bankRegister()
        }

        binding.bankLayout12.setOnClickListener {
            bankCode = "090"
            bankName = "카카오뱅크"
            bankRegister()
        }

        binding.bankLayout13.setOnClickListener {
            bankCode = "089"
            bankName = "케이뱅크"
            bankRegister()
        }

//        binding.bankLayout14.setOnClickListener {
//            bankCode = "092"
//            bankName = "토스뱅크"
//            bankRegister()
//        }

        binding.bankLayout15.setOnClickListener {
            bankCode = "027"
            bankName = "한국씨티은행"
            bankRegister()
        }

        binding.bankLayout16.setOnClickListener {
            bankCode = "003"
            bankName = "IBK기업은행"
            bankRegister()
        }

        binding.bankLayout17.setOnClickListener {
            bankCode = "002"
            bankName = "KDB산업은행"
            bankRegister()
        }

        binding.bankLayout18.setOnClickListener {
            bankCode = "081"
            bankName = "하나은행"
            bankRegister()
        }

        binding.bankLayout19.setOnClickListener {
            bankCode = "023"
            bankName = "SC제일은행"
            bankRegister()
        }

        binding.bankLayout20.setOnClickListener {
            bankCode = "071"
            bankName = "우체국"
            bankRegister()
        }

//        binding.bankLayout21.setOnClickListener {
//            bankCode = ""
//            bankName = "외환은행"
//            bankRegister()
//        }

        binding.bankLayout22.setOnClickListener {
            bankCode = "047"
            bankName = "신협"
            bankRegister()
        }

        binding.bankLayout23.setOnClickListener {
            bankCode = "064"
            bankName = "산림조합중앙회"
            bankRegister()
        }

        binding.bankLayout24.setOnClickListener {
            bankCode = "045"
            bankName = "새마을금고"
            bankRegister()
        }
    }

    // 계좌 등록/변경하기 Activity 로 이동 - jhm 2022/10/05
    private fun bankRegister() {
        val intent = Intent(mContext, BankRegisterActivity::class.java)
        intent.putExtra("vranNo",vranNo)
        intent.putExtra("bankCode", bankCode)
        intent.putExtra("bankName", bankName)
        startActivity(intent)
        finish()
    }

    /** Util start **/
    /**
     * 상태바 아이콘 색상 지정
     * @param isBlack true : 검정색 / false : 흰색
     */
    private fun setStatusBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능

            window.insetsController?.let {
                it.setSystemBarsAppearance(
                    if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // minSdk 6.0부터 사용 가능
            window.decorView.systemUiVisibility = if (isBlack) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                // 기존 uiVisibility 유지
                window.decorView.systemUiVisibility
            }

        } // end if

    }

    /**
     * 상태바 배경 색상 지정
     * @param colorHexValue #ffffff 색상 값
     */
    private fun setStatusBarBgColor(colorHexValue: String) {

        // 상태바 배경색은 5.0부터 가능하나, 아이콘 색상은 6.0부터 변경 가능
        // -> 아이콘/배경색 모두 바뀌어야 의미가 있으므로 6.0으로 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor(colorHexValue)

        } // end if
    }

    /**
     * 내비바 아이콘 색상 지정
     * @param isBlack true : 검정색 / false : 흰색
     */
    private fun setNaviBarIconColor(isBlack: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // android os 12에서 사용 가능

            window.insetsController?.let {
                it.setSystemBarsAppearance(
                    if (isBlack) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                )
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 아이콘 색상이 8.0부터 가능하므로 커스텀은 동시에 진행해야 하므로 조건 동일 처리.
            window.decorView.systemUiVisibility =
                if (isBlack) {
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

                } else {
                    // 기존 uiVisibility 유지
                    // -> 0으로 설정할 경우, 상태바 아이콘 색상 설정 등이 지워지기 때문
                    window.decorView.systemUiVisibility

                } // end if

        } // end if
    }

    /**
     * 내비바 배경 색상 설정
     * @param colorHexValue #ffffff 색상 값
     */
    private fun setNaviBarBgColor(colorHexValue: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 내비바 배경색은 8.0부터 지원한다.
            window.navigationBarColor = Color.parseColor(colorHexValue)

        } // end if

    }


}
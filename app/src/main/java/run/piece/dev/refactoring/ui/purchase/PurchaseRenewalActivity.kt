package run.piece.dev.refactoring.ui.purchase

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.viewmodel.AccountViewModel
import run.piece.dev.data.viewmodel.DepositBalanceViewModel
import run.piece.dev.data.viewmodel.GetUserViewModel
import run.piece.dev.databinding.ActivityPurchaseRenewalBinding
import run.piece.dev.refactoring.datastore.DataNexusViewModel
import run.piece.dev.refactoring.ui.deposit.NhChargeBtDlg
import run.piece.dev.refactoring.ui.deposit.NhHistoryViewModel
import run.piece.dev.refactoring.ui.passcode.NewPassCodeActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.NewVibratorUtil
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toDecimalComma
import run.piece.dev.refactoring.utils.toInt
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.portfolio.model.PortfolioStockItemVo
import java.lang.Math.abs


@AndroidEntryPoint
class PurchaseRenewalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPurchaseRenewalBinding
    private val dvm by viewModels<DepositBalanceViewModel>()  // 출금 가능 금액 ViewModel - jhm 2022/09/29
    private val nhHistoryViewModel: NhHistoryViewModel by viewModels() // NH 고객 입금 확인 조회 ViewModel
    private val dataNexusViewModel: DataNexusViewModel by viewModels()

    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    private lateinit var mvm: GetUserViewModel // 내 정보 조회
    private lateinit var mavm: AccountViewModel // 회원 계좌 정보 조회 ViewModel
    private var nhChargeBtDlg: NhChargeBtDlg? = null // 입금 안내 BottomSheet
    private var purchaseBtDlg: PurchaseBtDlg? = null // 신청 내역 BottomSheet

    private var keyPadBtn: Array<AppCompatButton?> = arrayOfNulls<AppCompatButton>(17)
    private var keyPadMoney: String = "0" // 사용자가 키패드를 통하여 입력한 값
    private val stringBuilder = StringBuilder()


    // Nh 입금 안내 페이지로 넘겨줄 데이터
    private var bankCode: String = ""
    private var bankName: String = "" // 은행 이름 - jhm 2022/10/07
    private var accountNo: String = "" // 은행 계좌번호 - jhm 2022/10/07
    private var vranNo: String = ""


    companion object {
        private var mBtnBoolean: Boolean = false // 조건 충족시 버튼 클릭 제어
        private var mDepositBalance: Long = 0 // 나의 예치금 잔액
        private var portfolioId: String? = "" // 포트폴리오 아이디
        private var ptThumbImg: String? = "" // 포트폴리오 썸네일
        private var ptNumTitle: String? = "" // 포트폴리오 번호
        private var ptTitle: String? = "" // 포트폴리오 제목
        private var ptWithDrawDate: String? = "" // 출금일자
        private var faceValue: String = "" // 1조각당 금액
        private var recruitmentAmount: String? = "" // 총 판매 금액
        private var totalPiece: Int? = 0 // 총 판매 조각 수
        private var quantityLeft: Int? = 0 // 남은 수량
        private var maxPurchaseAmount: String = "" // 최대 구매 가능 금액
        private var attachFileCode: String = "" // 투자 설명서 링크
    }

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseRenewalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
        }

        binding.apply {
            lifecycleOwner = this@PurchaseRenewalActivity
            activity = this@PurchaseRenewalActivity

            mvm = ViewModelProvider(this@PurchaseRenewalActivity)[GetUserViewModel::class.java]
            mavm = ViewModelProvider(this@PurchaseRenewalActivity)[AccountViewModel::class.java]
            nhChargeBtDlg = NhChargeBtDlg(this@PurchaseRenewalActivity, "Purchase")
            purchaseBtDlg = PurchaseBtDlg(this@PurchaseRenewalActivity)

            depositVm = dvm
            dataStoreViewModel = dataNexusViewModel

            intent?.let {

                // PortfolioDetail 데이터
                val detailDefaultVo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableExtra("detailDefaultVo", PortfolioDetailDefaultVo::class.java)
                } else {
                    it.getParcelableExtra("detailDefaultVo")
                }

                val stockVo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableExtra("stockVo", PortfolioStockItemVo::class.java)
                } else {
                    it.getParcelableExtra("stockVo")
                }

                val filesVo: List<AttachFileItemVo>? = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableArrayListExtra("attachFileItemVo", AttachFileItemVo::class.java)
                } else {
                    it.getParcelableArrayListExtra("attachFileItemVo")
                }

                // portfolioStock 데이터
                stockVo?.let { vo ->
                    faceValue = vo.faceValue
                    recruitmentAmount = vo.recruitmentAmount
                    totalPiece = vo.totalPiece.toInt()
                }

                detailDefaultVo?.let { vo ->
                    portfolioId = vo.portfolioId
                    ptThumbImg = vo.representThumbnailImagePath
                    ptTitle = vo.title
                    ptNumTitle = vo.subTitle
                    ptWithDrawDate = vo.prizeAt
                    quantityLeft = vo.quantityLeft.toInt()
                    maxPurchaseAmount = vo.maxPurchaseAmount
                }

                attachFileCode = it.getStringExtra("attachFileCode").toString()


                confirmBtn.onThrottleClick {
                    if (binding.confirmBtn.isSelected) {

                        val bundle = Bundle().apply {
                            putParcelable("stockVo" , stockVo)
                            putParcelable("detailDefaultVo" , detailDefaultVo)
                            putParcelableArrayList("attachFileItemVo", ArrayList(filesVo))
                            putString("attachFileCode",attachFileCode)
                            putInt("userVolume", keyPadMoney.toInt()) // 청약 수량
                            putInt("userPrice", keyPadMoney.toInt().times(faceValue.toInt())) // 신청한 금액
                        }

                        var agreeTime = this@PurchaseRenewalActivity.dataNexusViewModel.getPurchaseAgreeTime()

                        purchaseBtDlg?.arguments = bundle
                        purchaseBtDlg?.show(supportFragmentManager, "신청내역")
                        purchaseBtDlg?.setCallback(object : PurchaseBtDlg.OnSendFromBottomSheetDialog {
                            override fun sendValue(portfolioId: String, portfolioTitle: String, userVolume: Int, endDate: String) {
                                val intent = Intent(this@PurchaseRenewalActivity, NewPassCodeActivity::class.java)
                                intent.run {
                                    putExtra("Step","4")
                                    putExtra("portfolioId",portfolioId)
                                    putExtra("portfolioTitle",portfolioTitle)
                                    putExtra("userVolume",userVolume)
                                    putExtra("endDate",endDate)
                                    putExtra("agreeTime",agreeTime)
                                }
                                startActivity(intent)
                                finish()
                            }
                        })
                    }
                }
            }

            // 최초 화면 문구 표기 셋팅
            CoroutineScope(Dispatchers.Main).launch {
                statusTxt1.text = "모집총량 "
                statusTv.text = totalPiece.toString().toDecimalComma() // 총 조각 수
                statusTxt2.text = "주"
            }


            CoroutineScope(Dispatchers.Main).launch {
                keyPadBtn[0] = code1
                keyPadBtn[1] = code2
                keyPadBtn[2] = code3
                keyPadBtn[3] = code4
                keyPadBtn[4] = code5
                keyPadBtn[5] = code6
                keyPadBtn[6] = code7
                keyPadBtn[7] = code8
                keyPadBtn[8] = code9
                keyPadBtn[9] = allClear
                keyPadBtn[10] = code0
                keyPadBtn[11] = clear
                keyPadBtn[12] = plus1
                keyPadBtn[13] = plus5
                keyPadBtn[14] = plus10
                keyPadBtn[15] = plus50
                keyPadBtn[16] = plusMax

                for (index in 0..16) {
                    keyPadBtn[index]?.setOnClickListener {
                        NewVibratorUtil().run {
                            init(this@PurchaseRenewalActivity)
                            oneShot(100, 100)
                        }
                        val btn: AppCompatButton = it as AppCompatButton
                        val btnText: String = btn.text.toString()
                        try {

                            /** 내가 가진 금액 나누기 1조각당 금액 **/
                            var availableQuantity = mDepositBalance.floorDiv(faceValue.toInt())

                            if (btnText == getString(R.string.purchase_btn_text_1)) {
                                if (keyPadMoney.toInt().times(faceValue.toInt()) > mDepositBalance) return@setOnClickListener
                                else keyPadMoney = keyPadMoney.toInt().plus(1).toString()
                            } else if (btnText == getString(R.string.purchase_btn_text_5)) {
                                if (keyPadMoney.toInt().times(faceValue.toInt()) > mDepositBalance) return@setOnClickListener
                                else keyPadMoney = keyPadMoney.toInt().plus(5).toString()
                            } else if (btnText == getString(R.string.purchase_btn_text_10)) {
                                if (keyPadMoney.toInt().times(faceValue.toInt()) > mDepositBalance) return@setOnClickListener
                                else keyPadMoney = keyPadMoney.toInt().plus(10).toString()
                            } else if (btnText == getString(R.string.purchase_btn_text_50)) {
                                if (keyPadMoney.toInt().times(faceValue.toInt()) > mDepositBalance) return@setOnClickListener
                                else keyPadMoney = keyPadMoney.toInt().plus(50).toString()
                            } else if (btnText == getString(R.string.purchase_btn_text_max)) {
                                if (mBtnBoolean) {
                                    return@setOnClickListener
                                } else {
                                    try {

                                        // 나의 예치금 잔액이 0원일 경우
                                        if (mDepositBalance.toInt() == 0) {
                                            return@setOnClickListener
                                        } else {
                                            // 1원이라도 가지고 있을 경우 1조각 살수 있는 금액 만큼 표기
                                            keyPadMoney = availableQuantity.toString()
                                        }

                                    } catch (ex: Exception) {
                                        ex.printStackTrace()
                                    }


                                }
                            } else if (btnText == "초기화") {
                                purchaseDefault()
                            } else if (btnText == "지우기") {
                                keyPadMoney = keyPadMoney.substring(0, keyPadMoney.length - 1)
                            } else {
                                if (keyPadMoney.length < 8) {
                                    if (keyPadMoney.toInt() > availableQuantity) {
                                        return@setOnClickListener
                                    } else {
                                        if (keyPadMoney == "0") {
                                            keyPadMoney =
                                                stringBuilder.append(btn.text.toString()).toString()
                                                    .toInt()
                                                    .toString()
                                        } else {
                                            keyPadMoney = keyPadMoney.plus(btn.text.toString())
                                        }

                                    }
                                }
                            }

                            purchaseKeyPadTextChange()


                        } catch (ex: Exception) {
                            ex.printStackTrace()
                            purchaseDefault()
                        }

                    }
                }
            }


            backIv.onThrottleClick {
                BackPressedUtil().activityFinish(this@PurchaseRenewalActivity,this@PurchaseRenewalActivity)
            }

            depositChargeBtn.onThrottleClick {
                val data = Bundle()
                data.putString("bankCode", bankCode)
                data.putString("bankName", bankName)
                data.putString("accountNo", accountNo)
                data.putString("vranNo", vranNo)

                nhChargeBtDlg?.show(
                    supportFragmentManager,
                    getString(R.string.nh_charge_bt_dlg_tag)
                )
                nhChargeBtDlg?.arguments = data
                nhChargeBtDlg?.setCallback(object :
                    NhChargeBtDlg.OnSendFromBottomSheetDialog {
                    override fun sendValue() {
                        nhChargeBtDlg?.dismiss()
                        mvm.getUserData()
                    }
                })
            }
        }


        CoroutineScope(Dispatchers.IO).launch {
            mvm.getUserData() // 최초에 회원 정보 조회 ViewModel 에서 데이터를 받아옴
            mavm.getAccount(accessToken, deviceId, memberId) // 등록된 계좌 조회
            dvm.getDepositBalance(accessToken, deviceId, memberId) // 잔액 조회
        }

        CoroutineScope(Dispatchers.Main).launch {
            // 예치금 잔액
            mvm.invstDepsBal.observe(this@PurchaseRenewalActivity, Observer {
                if (it.toInt() == 0) {
                    mDepositBalance = 0
                    binding.depositBalanceTv.text = "예치금 $mDepositBalance 원"
                } else {
                    mDepositBalance = it.toLong()
                    binding.depositBalanceTv.text = "예치금 ${mDepositBalance.decimalComma()}원"
                }
            })
            // 가상계좌 번호
            mvm.vran.observe(this@PurchaseRenewalActivity, Observer {
                vranNo = if (it.isNullOrEmpty()) "" else it.toString()
                LogUtil.d("vranNo : $vranNo")
            })
        }

        CoroutineScope(Dispatchers.Main).launch {
            mavm.accountResponse.observe(this@PurchaseRenewalActivity, Observer {
                try {
                    if (it.data != null) {
                        bankCode = it.data.bankCode
                        bankName = it.data.bankName
                        accountNo = it.data.accountNo
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            })
        }

        BackPressedUtil().activityCreate(this@PurchaseRenewalActivity,this@PurchaseRenewalActivity)
        BackPressedUtil().systemBackPressed(this@PurchaseRenewalActivity,this@PurchaseRenewalActivity)
    }

    @SuppressLint("SetTextI18n")
    fun purchaseDefault() {
        mBtnBoolean = false

        binding.statusTxt1.visibility = View.VISIBLE
        binding.statusTxt1.text = "모집총량 "
        binding.statusTv.text = totalPiece.toString().toDecimalComma() // 총 조각 수
        binding.statusTxt2.text = "주"

        binding.inputTv.text = ""
        keyPadMoney = "0"
        stringBuilder.setLength(0)
        binding.confirmBtn.isSelected = false
        binding.depositChargeBtn.visibility = View.GONE // 예치금 충전하기 버튼 GONE
        binding.depositBalanceTv.visibility = View.VISIBLE // 예치금 잔액 VISIBLE
        binding.depositBalanceTv.text = "예치금 ${mDepositBalance.decimalComma()}원"

        textChangeColor(
            binding.depositBalanceTv,
            binding.depositBalanceTv.text.toString(),
            "#B8BCC8"
        ) // 예치금 잔액 Color
    }

    @SuppressLint("SetTextI18n")
    fun purchaseKeyPadTextChange() {
        if (keyPadMoney == "0") {
            binding.inputTv.text = ""
            keyPadMoney = "0"
            stringBuilder.setLength(0)
            binding.confirmBtn.isSelected = false
            binding.depositChargeBtn.visibility = View.GONE // 예치금 충전하기 버튼 GONE
            binding.depositBalanceTv.visibility = View.VISIBLE // 예치금 잔액 VISIBLE
            binding.depositBalanceTv.text = "예치금 ${mDepositBalance.decimalComma()}원"

            textChangeColor(binding.depositBalanceTv, binding.depositBalanceTv.text.toString(), "#B8BCC8") // 예치금 잔액 Color
        } else {
            var inputTimes = keyPadMoney.toInt().times(faceValue.toInt()) // 입력값 * 1조각당 금액 = 구매 희망 금액
            var insuffictient = mDepositBalance.minus(keyPadMoney.toInt().times(faceValue.toInt())) // 나의 예치금 잔액 - (입력값 * 1조각당 금액) = 부족한 예치금 금액
            var inputDivPurchase = keyPadMoney.toInt().times(faceValue.toInt()).floorDiv(faceValue.toInt()) // 입력값 * (1조각당 금액 / 1조각당 금액) = 내가 구매하는 조각의 수
            var inputFloorPurchase = mDepositBalance.floorDiv(faceValue.toInt()) // 나의 예치금 잔액 / 1조각당 금액 = 구매 할 수 있는 조각 수
            var quarterPurchase = totalPiece?.times(0.25).default()
            var maxPurchase = maxPurchaseAmount.toInt().floorDiv(faceValue.toInt()) // ( 최대 구매 가능 금액 / 1 조각당 금액 )


            binding.inputTv.text = keyPadMoney.toLong().decimalComma() + "주"
            binding.statusTxt1.visibility = View.VISIBLE
            binding.statusTxt1.text = "모집총량 "
            binding.statusTxt2.text = "주"

            // 청약 일때
            // 예치금 잔액이 0 원 일때
            if (mDepositBalance.toInt() == 0) {
                mBtnBoolean = true

                binding.confirmBtn.isSelected = false
                // 입력시 예치금 잔액 GONE
                binding.depositBalanceTv.visibility = View.GONE

                // 예치금 충전 버튼 VISIBLE
                binding.depositChargeBtn.visibility = View.VISIBLE

                textChangeColor(binding.statusTv, binding.statusTv.text.toString(), "#FF7878")

                binding.statusTxt1.text = "예치금이 "
                binding.statusTv.text = keyPadMoney.toInt().times(faceValue.toInt()).toLong().decimalComma() + "원"
                binding.statusTxt2.text = "더 필요해요"
            }
            // 예치금 잔액이 부족할때
            else if (inputTimes > mDepositBalance) {
                mBtnBoolean = true
                binding.confirmBtn.isSelected = false
                binding.depositChargeBtn.visibility = View.VISIBLE
                binding.depositBalanceTv.visibility = View.GONE
                binding.statusTxt1.visibility = View.VISIBLE

                binding.statusTxt1.text = "예치금이 "
                binding.statusTv.text = abs(insuffictient).decimalComma() + "원"
                binding.statusTxt2.text = " 더 필요해요"
                textChangeColor(
                    binding.statusTv,
                    binding.statusTv.text.toString(),
                    "#FF7878"
                )
            }

            // 투자한도 25%
            // 신청 조각 >= 총 조각의 25%
//            else if (keyPadMoney.toInt() > quarterPurchase) {
//                binding.confirmBtn.isSelected = false
//                /*** 상단 메시지 ***/
//                binding.depositChargeBtn.visibility = View.GONE
//                binding.depositBalanceTv.visibility = View.VISIBLE
//                binding.depositBalanceTv.text = "투자한도 초과"
//                textChangeColor(
//                    binding.depositBalanceTv,
//                    binding.depositBalanceTv.text.toString(),
//                    "#F95D5D"
//                )
//
//                /*** 하단 메시지 ***/
//                binding.statusTxt1.visibility = View.GONE
//                binding.statusTv.text = quarterPurchase.toInt().decimalComma()
//                binding.statusTxt2.text = "주까지 신청할 수 있어요"
//                textChangeColor(
//                    binding.statusTv,
//                    binding.statusTv.text.toString(),
//                    "#10CFC9"
//                )
//            }

            // 개인 한도 ( 최대 구매 수량 )
//            else if (keyPadMoney.toInt() > maxPurchase) {
//                binding.confirmBtn.isSelected = false
//                /*** 상단 메시지 ***/
//                binding.depositChargeBtn.visibility = View.GONE
//                binding.depositBalanceTv.visibility = View.VISIBLE
//                binding.depositBalanceTv.text = "투자한도 초과"
//                textChangeColor(
//                    binding.depositBalanceTv,
//                    binding.depositBalanceTv.text.toString(),
//                    "#F95D5D"
//                )
//
//                /*** 하단 메시지 ***/
//                binding.statusTxt1.visibility = View.GONE
//                binding.statusTv.text = maxPurchase.decimalComma()
//                binding.statusTxt2.text = "주까지 신청할 수 있어요"
//                textChangeColor(
//                    binding.statusTv,
//                    binding.statusTv.text.toString(),
//                    "#10CFC9"
//                )
//            }
            // 정상 케이스
            else {
                mBtnBoolean = false
                binding.confirmBtn.isSelected = true
                binding.depositChargeBtn.visibility = View.GONE
                binding.depositBalanceTv.visibility = View.VISIBLE
//                binding.depositBalanceTv.text = keyPadMoney.toInt().times(faceValue.toInt()).toLong().decimalComma() + "원"


                var piecePrice: Long = keyPadMoney.toLong().times(faceValue.toLong())
                Log.i("입력한 조각당 금액 : ", "$piecePrice")

                binding.depositBalanceTv.text = piecePrice.decimalComma() + "원"
                textChangeColor(binding.depositBalanceTv, binding.depositBalanceTv.text.toString(), "#131313")

                // maxPurchase = 10,000,000 / 10,000 = 1,000 최대 구매 가능 수량
                var maxPurchase = maxPurchaseAmount.toInt().floorDiv(faceValue.toInt())
                Log.d("최대 구매 가능 수량 : ", "$maxPurchase")

                // deposit = 100,000,000 / 10,000 = 10,000 조각을 살 수 있는 돈
                var availableQuantity = mDepositBalance.floorDiv(faceValue.toInt())
                Log.i("내가 가진 돈에서 최대 구매 가능 수량 : ", "$availableQuantity")


                // 나의 예치금 잔액이 최대 구매 가능 수량 보다 적을때
//                if(deposit < maxPurchase) {
//                    if(keyPadMoney.toInt() == deposit) {
//                        binding.statusTxt1.text = ""
//                        binding.statusTxt2.text = "주를 신청할까요?"
//                        binding.statusTv.text = keyPadMoney.toInt().decimalComma()
//                    } else {
//                        if(keyPadMoney.toInt() <= deposit) {
//                            binding.statusTxt1.text = ""
//                            binding.statusTxt2.text = "주 더 신청할 수 있어요"
//                            binding.statusTv.text = deposit.minus(keyPadMoney.toInt()).toString().toDecimalComma()
//                        } else {
//                            mBtnBoolean = true
//                            binding.confirmBtn.isSelected = false
//                            binding.depositChargeBtn.visibility = View.VISIBLE
//                            binding.depositBalanceTv.visibility = View.GONE
//                            binding.statusTxt1.visibility = View.VISIBLE
//
//                            binding.statusTxt1.text = "예치금이 "
//                            binding.statusTv.text = abs(insuffictient).decimalComma() + "원"
//                            binding.statusTxt2.text = " 더 필요해요"
//                            textChangeColor(
//                                binding.statusTv,
//                                binding.statusTv.text.toString(),
//                                "#FF7878"
//                            )
//                        }
//                    }
//                } else {
//                    if (keyPadMoney.toInt() == maxPurchase) {
//                        binding.statusTxt1.text = ""
//                        binding.statusTxt2.text = "주를 신청할까요?"
//                        binding.statusTv.text = keyPadMoney.toInt().decimalComma()
//                    } else {
//                        binding.statusTxt1.text = ""
//                        binding.statusTxt2.text = "주 더 신청할 수 있어요"
//                        binding.statusTv.text = maxPurchase.minus(keyPadMoney.toInt()).toString().toDecimalComma()
//                    }
//                }

                if (keyPadMoney.toInt().toLong() == availableQuantity) {
                    binding.statusTxt1.text = ""
                    binding.statusTxt2.text = "주를 신청할까요?"
                    binding.statusTv.text = keyPadMoney.toInt().decimalComma()
                } else {
                    if (keyPadMoney.toInt() <= availableQuantity) {
                        binding.statusTxt1.text = ""
                        binding.statusTxt2.text = "주 더 신청할 수 있어요"
                        binding.statusTv.text = availableQuantity.minus(keyPadMoney.toInt()).toString().toDecimalComma()
                    } else {
                        mBtnBoolean = true
                        binding.confirmBtn.isSelected = false
                        binding.depositChargeBtn.visibility = View.VISIBLE
                        binding.depositBalanceTv.visibility = View.GONE
                        binding.statusTxt1.visibility = View.VISIBLE

                        binding.statusTxt1.text = "예치금이 "
                        binding.statusTv.text = abs(insuffictient).decimalComma() + "원"
                        binding.statusTxt2.text = " 더 필요해요"
                        textChangeColor(
                            binding.statusTv,
                            binding.statusTv.text.toString(),
                            "#FF7878"
                        )
                    }
                }

            }
        }
    }

    fun textChangeColor(textView: AppCompatTextView, text: String, colorString: String) {
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor(colorString)),
            0,
            text.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        );
        textView.text = spannableString
    }

}
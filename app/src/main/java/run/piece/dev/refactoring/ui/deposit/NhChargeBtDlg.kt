package run.piece.dev.refactoring.ui.deposit

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.SlideupNhChargeBinding
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.widget.extension.SnackBarCommon
import run.piece.dev.widget.utils.IndentLeadingMarginSpan

// 입금 안내 BottomSheet Dialog
class NhChargeBtDlg(context: Context,viewType: String) : BottomSheetDialogFragment() {
    lateinit var binding: SlideupNhChargeBinding
    private val mContext: Context = context
    private val viewType: String = viewType
    private var listener: OnSendFromBottomSheetDialog? = null

    // Fold UI 대응
    private val display = context.resources?.displayMetrics
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SlideupNhChargeBinding.inflate(inflater,container,false)
        binding.lifecycleOwner = this@NhChargeBtDlg
        binding.apply {
            // 연동 계좌 데이터 Set
            var mBankCode = arguments?.getString("bankCode").toString()
            var mBankName = arguments?.getString("bankName").toString()
            var mBankAccountNo = arguments?.getString("accountNo").toString()
            var mVranNo = arguments?.getString("vranNo").toString()
            var encryption = "**********"
            var decryption = ""
            decryption = mBankAccountNo.substring(mBankAccountNo.length - 4, mBankAccountNo.length)
            when(mBankCode) {
                "001" -> {}
                "002" -> { Glide.with(root.context).load(R.drawable.bank02).into(myAccountIv)}
                "003" -> { Glide.with(root.context).load(R.drawable.bank03).into(myAccountIv)}
                "004" -> { Glide.with(root.context).load(R.drawable.bank04).into(myAccountIv)}
                "081" -> { Glide.with(root.context).load(R.drawable.bank05).into(myAccountIv)}
                "007" -> { Glide.with(root.context).load(R.drawable.bank07).into(myAccountIv)}
                "008" -> {}
                "011" -> { Glide.with(root.context).load(R.drawable.bank11).into(myAccountIv)}
                "020" -> { Glide.with(root.context).load(R.drawable.bank20).into(myAccountIv) }
                "023" -> { Glide.with(root.context).load(R.drawable.bank23).into(myAccountIv) }
                "026" -> { Glide.with(root.context).load(R.drawable.bank26).into(myAccountIv) }
                "027" -> { Glide.with(root.context).load(R.drawable.bank27).into(myAccountIv) }
                "031" -> { Glide.with(root.context).load(R.drawable.bank31).into(myAccountIv) }
                "032" -> { Glide.with(root.context).load(R.drawable.bank32).into(myAccountIv)}
                "034" -> { Glide.with(root.context).load(R.drawable.bank34).into(myAccountIv) }
                "035" -> { Glide.with(root.context).load(R.drawable.bank35).into(myAccountIv) }
                "037" -> { Glide.with(root.context).load(R.drawable.bank37).into(myAccountIv) }
                "039" -> { Glide.with(root.context).load(R.drawable.bank39).into(myAccountIv) }
                "045" -> { Glide.with(root.context).load(R.drawable.bank45).into(myAccountIv) }
                "047" -> { Glide.with(root.context).load(R.drawable.bank47).into(myAccountIv) }
                "064" -> { Glide.with(root.context).load(R.drawable.bank64).into(myAccountIv) }
                "071" -> { Glide.with(root.context).load(R.drawable.bank71).into(myAccountIv) }
                "089" -> { Glide.with(root.context).load(R.drawable.bank89).into(myAccountIv) }
                "090" -> { Glide.with(root.context).load(R.drawable.bank90).into(myAccountIv) }
                "092" -> { Glide.with(root.context).load(R.drawable.bank92).into(myAccountIv) }
            }
            myAccountNameTv.text = mBankName
            myAccountNumTv.text = encryption + decryption


            // 전용 가상 계좌 데이터 Set
            nameTv.text = PrefsHelper.read("name","").toString() + "님 전용 가상계좌"
            myVranNumTv.text = mVranNo

            if(display != null) {
                // 폴드 펼침
                if(display.widthPixels > 1600) {
                    copyTv.visibility = View.VISIBLE
                }
                // 미니 , 폴드 닫힘
                else if(display.widthPixels < 980) {
                    foldCopyTv.visibility = View.VISIBLE
                    foldCopyTv.onThrottleClick {
                        copyText(myVranNumTv.text.toString())
                    }
                }
                // 일반
                else {
                    copyTv.visibility = View.VISIBLE
                }
            }
            // 복사
            copyTv.onThrottleClick {
                copyText(myVranNumTv.text.toString())
            }


            noticeTitle1.text = SpannableStringBuilder(getString(R.string.nh_charge_bt_dlg_text_1)).apply {
                setSpan(IndentLeadingMarginSpan(), 0, length, 0)
            }

            noticeTitle2.text = SpannableStringBuilder(getString(R.string.nh_charge_bt_dlg_text_2)).apply {
                setSpan(IndentLeadingMarginSpan(), 0, length, 0)
            }

            noticeTitle3.text = SpannableStringBuilder(getString(R.string.nh_charge_bt_dlg_text_3)).apply {
                setSpan(IndentLeadingMarginSpan(), 0, length, 0)
            }

            noticeTitle4.text = SpannableStringBuilder(getString(R.string.nh_charge_bt_dlg_text_4)).apply {
                setSpan(IndentLeadingMarginSpan(), 0, length, 0)
            }

            noticeTitle5.text = SpannableStringBuilder(getString(R.string.nh_charge_bt_dlg_text_5)).apply {
                setSpan(IndentLeadingMarginSpan(), 0, length, 0)
            }

            when(viewType) {
                "Wallet" -> {
                    nhBtnTv.text = "입금했어요, 예치금을 확인할게요"
                }
                "Purchase" -> {
                    nhBtnTv.text = "확인"
                }
            }
            // 입금했어요, 예치금을 확인할게요 버튼 Click
            nhBtnTv.onThrottleClick {
                listener?.sendValue()
            }
        }

        // 스크롤 닫기 제어
        isCancelable = true

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;
    }


    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from<View>(bottomSheet)
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight().toInt()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getBottomSheetDialogDefaultHeight(): Double {
        if(display != null) {
            // 폴드 펼침
            if(display.widthPixels > 1600) {
                return getWindowHeight() * 68.9 / 100
            }
            // 미니 , 폴드 닫힘
            else if(display.widthPixels < 980) {
                return getWindowHeight() * 77.3 / 100
            }
            // 일반
            else {
                return getWindowHeight() * 77.3 / 100
            }
        }
        return getWindowHeight() * 77.3 / 100
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


    interface OnSendFromBottomSheetDialog {
        fun sendValue()
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }


    private fun copyText(text: String) {
        val clipboard: ClipboardManager = context?.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("vranCopy", text))
        // Aos Version 13 이상부터는 자동으로 클립보드 메시지가 뜨게 되어있으므로 아래 버전만 호출
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            val snackBarCommon = SnackBarCommon(
                binding.root.rootView,
                getString(R.string.bank_num_copy_txt),getString(R.string.custom_snackbar_type_bank_num_txt)
            )
            snackBarCommon.show(95)
        }
    }

}

package run.piece.dev.widget.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.bumptech.glide.Glide
import run.piece.dev.R
import run.piece.dev.databinding.*
import javax.inject.Singleton

/**
 *packageName    : com.bsstandard.piece.widget.utils
 * fileName       : DialogManager
 * author         : piecejhm
 * date           : 2022/10/18
 * description    : 공통 Dialog
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/10/18        piecejhm       최초 생성
 */

object DialogManager {
    /**
     * 버튼 Type 1개
     * Activity 종료 포함
     * **/
    fun openDialog(
        context: Context,
        title: String,
        subTitle: String,
        btnTitle: String,
        activity: Activity
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogBasicBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle
            dialogBinding.okBtn.text = btnTitle

            dialogBinding.okBtn.setOnClickListener {
                dialog.dismiss()
                activity.finish()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }

    /**
     * 버튼 Type 1개
     * Activity 종료 미포함
     * 버튼 클릭 리스너 포함
     * **/
    fun openDlgOneBtn(
        context: Context,
        title: String,
        subTitle: String,
        btnTitle: String,
        alg_listener: ModalOneBtnClickListener,
        activity: Activity
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogBasicBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            dialogBinding.title.text = title

            // "90일" 색상 변경  - jhm 2023/02/07
            val ssb = SpannableStringBuilder(subTitle)
            ssb.setSpan(
                ForegroundColorSpan(Color.parseColor("#FF7878")),
                8,
                11,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            dialogBinding.subTitle.text = ssb
            dialogBinding.okBtn.text = btnTitle

            dialogBinding.okBtn.setOnClickListener {
                dialog.dismiss()
                alg_listener.goActivity()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }


    // 실명인증 실패시 Dialog - jhm 2023/01/19
    fun openSsnChkDlg(
        context: Context,
        title: String,
        subTitle: String,
        btnTitle: String,
        activity: Activity,
        type: String,
        auth_listener: AuthChkClicked,
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogBasicBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)

        if (!dialog.isShowing) {
            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle
            dialogBinding.okBtn.text = btnTitle

            dialogBinding.okBtn.setOnClickListener {

                when(type) {
                    "성공" -> {
                        auth_listener.algDismiss()
                        dialog.dismiss()
                    }
                    "실패" -> {
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }


    /**
     * 버튼 Type 1개
     * Activity 종료 미포함
     * 유의사항 및 텍스트 Left 정렬
     * **/
    fun openLeftDalog(
        context: Context,
        title: String,
        subTitle: String,
        btnTitle: String
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogTextLeftBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            dialogBinding.title.text = title
//        dialogBinding.subTitle.text = subTitle
            dialogBinding.subTitle.text = SpannableStringBuilder(subTitle).apply {
                setSpan(IndentLeadingMarginSpan(), 0, length, 0)
            }
            dialogBinding.okBtn.text = btnTitle

            dialogBinding.okBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }


    /**
     * 버튼 Type 1개
     * Activity 종료 미포함
     * **/
    fun openNotGoDalog(
        context: Context,
        title: String,
        subTitle: String,
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogBasicBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle
            dialogBinding.okBtn.text = "확인"

            dialogBinding.okBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }

    /***
     * 알림 Dialog
     * Activity 종료 미포함
     * Activity 이동 포함
     * **/
    fun openAlarmDlg(context: Context, activity: Activity, alg_listener: AlarmDlgListener) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogAlarmBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)

        Glide.with(context).load(R.drawable.alarm_lopping).into(dialogBinding.alarmBellIv)

        if (!dialog.isShowing) {
            dialogBinding.closeBtn.setOnClickListener {
                alg_listener.offCloseClicked()
                dialog.dismiss()
            }
            dialogBinding.okBtn.setOnClickListener {
                alg_listener.openOptionsClicked()
                dialog.dismiss()
            }

            dialog.show()
            dialog.setCancelable(false) // 배경 터치 불가 설정 - jhm 2023/01/13
        } else {
            dialog.dismiss()
        }
    }


    /**
     * 버튼 Type 1개
     * 버튼 클릭시 구글 플레이 업데이트 이동
     * **/
    fun openGoUpdate(
        context: Context,
        title: String,
        subTitle: String,
        activity: Activity
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogBasicBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle
            dialogBinding.okBtn.text = "지금 업데이트"

            dialogBinding.okBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(context.resources.getString(R.string.playstore_url))
                try {
                    intent.data = Uri.parse("market://details?id=run.piece.dev")
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()

                    intent.data = Uri.parse("https://play.google.com/store/apps/details?id=run.piece.dev&hl=ko")
                    if(intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                }
                dialog.dismiss()
                activity.finish()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }



    // 버튼 타입 2개 - jhm 2022/10/18
    @Singleton
    @SuppressLint("ResourceAsColor")
    fun openTwoBtnDialog(
        context: Context,
        title: String,
        subTitle: String,
        listener_c: CustomDialogListener,
        type: String
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogMiddleBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)

        if (!dialog.isShowing) {
            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle

            when (type) {
                "비밀번호 재설정" -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "재설정"
                }

                "구매 확정" -> {
                    dialogBinding.cancleBtn.text = "취소"
                    dialogBinding.okBtn.text = "확인"
                }

                "쿠폰 사용" -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "사용하기"
                }
                "등록된 계좌" -> {
                    dialogBinding.cancleBtn.text = "뒤로"
                    dialogBinding.okBtn.text = "계좌 등록"
                }
                "로그아웃" -> {
                    dialogBinding.cancleBtn.text = "취소"
                    dialogBinding.okBtn.text = "로그아웃"
                }
                "주소 등록" -> {
                    dialogBinding.cancleBtn.text = "뒤로"
                    dialogBinding.okBtn.text = "주소 등록"
                }
                "우편" -> {
                    dialogBinding.cancleBtn.text = "뒤로"
                    dialogBinding.okBtn.text = "우편으로 받기"
                }
                "이메일 등록" -> {
                    dialogBinding.cancleBtn.text = "뒤로"
                    dialogBinding.okBtn.text = "이메일 등록"
                }
                "이메일" -> {
                    dialogBinding.cancleBtn.text = "뒤로"
                    dialogBinding.okBtn.text = "이메일 신청"
                }

                "약관 미동의" -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "동의하러 가기"
                }

                // 포트폴리오 상세에서 포트폴리오 알림을 눌렀을때 발생 - jhm 2023/01/13
                "알림 미설정" -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "알림 받기"
                }

                // 간편비밀번호 기회원 보안강화 안내 Dialog - jhm 2023/03/11
                "보안 강화 안내" -> {
                    dialogBinding.cancleBtn.text = "다음에 할게요"
                    dialogBinding.okBtn.text = "지금 변경하기"
                }
                // 간편 비밀번호 5회 횟수 초과시 Dialog - jhm 2023/03/12
                "오류 횟수 초과" -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "재설정"
                }
                context.getString(R.string.nh_charge_register_tag) -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = context.getString(R.string.nh_charge_register_tag)
                }
                context.getString(R.string.nh_charge_register_success_tag) -> {
                    dialogBinding.cancleBtn.text = "확인"
                    dialogBinding.okBtn.text = context.getString(R.string.nh_charge_register_btn_txt)
                }
            }

            // 취소 및 닫기 - jhm 2022/10/18
            dialogBinding.cancleBtn.setOnClickListener {
                listener_c.onCancelButtonClicked()
                dialog.dismiss()
                dialog.hide()
                dialog.cancel()
            }

            // 확인 - jhm 2022/10/18
            dialogBinding.okBtn.setOnClickListener {
                listener_c.onOkButtonClicked()
                dialog.dismiss()
                dialog.hide()
                dialog.cancel()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.cancel()
            dialog.hide()
            dialog.dismiss()
        }
    }


    fun goKakaoDialog(
        context: Context,
        title: String,
        subTitle: String,
        listener_c: CustomDialogListener,
        type: String,
        activity: Activity
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogMiddleBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle

            when (type) {
                "탈퇴 불가" -> {
                    dialogBinding.cancleBtn.text = "탈퇴 취소"
                    dialogBinding.okBtn.text = "카카오톡 문의하기"
                }

            }

            // 취소 및 닫기 - jhm 2022/10/18
            dialogBinding.cancleBtn.setOnClickListener {
                listener_c.onCancelButtonClicked()
                dialog.dismiss()
            }

            // 확인 - jhm 2022/10/18
            dialogBinding.okBtn.setOnClickListener {
                listener_c.onOkButtonClicked()
                dialog.dismiss()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }


    // 버튼 타입 2개 c_ff7878 - jhm 2022/10/25
    fun openTwoBtnNagativeDialog(
        context: Context,
        title: String,
        subTitle: String,
        listener_c: CustomDialogListener,
        type: String
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        val dialogBinding = DialogCancleBinding.inflate(dialog.layoutInflater)
        dialog.setContentView(dialogBinding.root)
        if (!dialog.isShowing) {
            when (type) {
                "신청 취소" -> {
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "신청 취소"
                }
                "회원 탈퇴" -> {
                    dialogBinding.subTitle.setTextColor(context.getColor(R.color.c_FF7878))
                    dialogBinding.cancleBtn.text = "취소"
                    dialogBinding.okBtn.text = "탈퇴"
                }
                "철회 확인" -> {
                    dialogBinding.subTitle.setTextColor(context.getColor(R.color.c_8c919f))
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "철회하기"
                }
                "정보성 알림 수신 해제" -> {
                    dialogBinding.subTitle.setTextColor(context.getColor(R.color.c_8c919f))
                    dialogBinding.cancleBtn.text = "닫기"
                    dialogBinding.okBtn.text = "해제"
                }
            }

            dialogBinding.title.text = title
            dialogBinding.subTitle.text = subTitle

            // 취소 및 닫기 - jhm 2022/10/18
            dialogBinding.cancleBtn.setOnClickListener {
                listener_c.onCancelButtonClicked()
                dialog.dismiss()
            }

            // 확인 - jhm 2022/10/18
            dialogBinding.okBtn.setOnClickListener {
                listener_c.onOkButtonClicked()
                dialog.dismiss()
            }

            dialog.show()
            dialog.setCancelable(false)
        } else {
            dialog.dismiss()
        }
    }

    // 버튼 타입 2개 생체인증 - jhm 2022/10/18
    @SuppressLint("ResourceAsColor")
    fun openAuthDialog(
        context: Context,
        title: String,
        subTitle: String,
        listener_c: CustomDialogListener,
        type: String,
        boolean: Boolean
    ) {
        val dialog = Dialog(context, R.style.Dialog)
        when (type) {
            "생체인증 등록" -> {
                val dialogBinding = DialogMiddleBinding.inflate(dialog.layoutInflater)
                dialog.setContentView(dialogBinding.root)
                if (!dialog.isShowing) {

                    dialogBinding.title.text = title
                    dialogBinding.subTitle.text = subTitle

                    dialogBinding.cancleBtn.text = "뒤로"
                    dialogBinding.okBtn.text = "등록"

                    // 확인 - jhm 2022/10/18
                    dialogBinding.okBtn.setOnClickListener {
                        listener_c.onOkButtonClicked()
                        dialog.dismiss()
                    }

                    // 취소 및 닫기 - jhm 2022/10/18
                    dialogBinding.cancleBtn.setOnClickListener {
                        listener_c.onCancelButtonClicked()
                        dialog.dismiss()
                    }
                } else {
                    dialog.dismiss()
                }
            }
            "생체인증 해제" -> {
                val dialogBinding = CertificationOffDialogBinding.inflate(dialog.layoutInflater)
                dialog.setContentView(dialogBinding.root)

                if(!dialog.isShowing) {
                    dialogBinding.title.text = title
                    dialogBinding.subTitle.text = subTitle


                    dialogBinding.cancleBtn.text = "확인"
                    dialogBinding.okBtn.text = "취소"

                    // 확인 - jhm 2022/10/18
                    dialogBinding.okBtn.setOnClickListener {
                        listener_c.onOkButtonClicked()
                        dialog.dismiss()
                    }

                    // 취소 및 닫기 - jhm 2022/10/18
                    dialogBinding.cancleBtn.setOnClickListener {
                        listener_c.onCancelButtonClicked()
                        dialog.dismiss()
                    }

                } else {
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
        dialog.setCancelable(false)
    }
}

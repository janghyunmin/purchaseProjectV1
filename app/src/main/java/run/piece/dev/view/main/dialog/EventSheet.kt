package run.piece.dev.view.main.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.SlideupEventBinding
import run.piece.dev.refactoring.base.html.BaseWebViewActivity
import run.piece.dev.refactoring.base.html.WebViewRouter
import run.piece.dev.refactoring.ui.info.NewMyInfoActivity
import run.piece.dev.refactoring.ui.magazine.NewMagazineDetailWebViewActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.notification.NewNotificationSettingActivity
import run.piece.dev.refactoring.ui.portfolio.detail.PortfolioDetailNewActivity
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import java.text.SimpleDateFormat
import java.util.*


/**
 *packageName    : com.bsstandard.piece.view.main.dialog
 * fileName       : EventSheet
 * author         : piecejhm
 * date           : 2022/07/07
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/07/07        piecejhm       최초 생성
 */

class EventSheet(
    activity: Activity,
    private val popupImagePath: String,
    private val popupType: String,
    private val popupLinkType: String,
    private val popupLinkUrl: String
) : BottomSheetDialogFragment() {
    lateinit var mainActivity: MainActivity
    lateinit var binding: SlideupEventBinding;

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        binding = SlideupEventBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        try {
            // 팝업 이미지 - jhm 2022/11/04
            Glide.with(requireContext())
                .load(popupImagePath)
                .into(binding.img)

            roundTop(binding.img, 32f)

        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        binding.today.onThrottleClick { toDayDismiss() }
        binding.close.onThrottleClick { onDismiss() }
        binding.img.onThrottleClick { openEvent() }

        isCancelable = false

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 2. Context를 액티비티로 형변환해서 할당
        mainActivity = context as MainActivity
    }


    @SuppressLint("SimpleDateFormat")
    fun toDayDismiss() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd").format(Date())
        LogUtil.v("dateFormat : $dateFormat")
        PrefsHelper.write("PopupDismiss", dateFormat)
        dismiss()
    }

    fun onDismiss() {
        dismiss()
    }


    /**
     * popupType
     * POP0201 : 포트폴리오 상세
     * POP0202 : 라운지 상세
     * POP0203 : 공지사항 상세
     * POP0204 : 이벤트 상세
     * POP0205 : 알림 설정 상세
     * POP0206 : 내 정보 상세
     * POP0207 : 웹뷰
     * POP0208 : 팝업 버튼 타입 1 - 오늘은 보지 않기
     * POP0209 : 팝업 버튼 타입 2 - 오늘은 보지 않기
     * POP0210 : 팝업 버튼 타입 3 - 앱 다운로드
     * POP0211 : 팝업 버튼 타입 4 - 앱 다운로드
     * **/

    fun openEvent() {
        when (popupLinkType) {
            "POP0201" -> {
                val intent = Intent(activity, PortfolioDetailNewActivity::class.java)
                intent.putExtra("portfolioId", popupLinkUrl)
                startActivity(intent)
            }
            "POP0202" -> {
                val intent = Intent(activity, NewMagazineDetailWebViewActivity::class.java)
                intent.putExtra("magazineId", popupLinkUrl)
                startActivity(intent)
            }
            "POP0203" -> {
                val intent = Intent(activity, BaseWebViewActivity::class.java)
                intent.putExtra("viewName",WebViewRouter.NOTICE.viewName)
                intent.putExtra("boardId", popupLinkUrl)
                startActivity(intent)
            }
            "POP0204" -> {
                val intent = Intent(activity, BaseWebViewActivity::class.java)
                intent.putExtra("viewName",WebViewRouter.EVENT.viewName)
                intent.putExtra("eventId", popupLinkUrl)
                startActivity(intent)
            }
            "POP0205" -> {
                val intent = Intent(activity, NewNotificationSettingActivity::class.java)
                startActivity(intent)
            }
            "POP0206" -> {
                val intent = Intent(activity, NewMyInfoActivity::class.java)
                startActivity(intent)
            }
            "POP0207" -> {
                val intent = Intent(activity, BaseWebViewActivity::class.java)
                intent.putExtra("viewName",WebViewRouter.COMMON.viewName)
                startActivity(intent)
            }
        }
    }

    fun roundTop(iv: ImageView, curveRadius: Float): ImageView {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            iv.outlineProvider = object : ViewOutlineProvider() {

                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0,
                        0,
                        view!!.width,
                        (view.height + curveRadius).toInt(),
                        curveRadius
                    )
                }
            }

            iv.clipToOutline = true
        }
        return iv
    }


}

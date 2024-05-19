package run.piece.dev.widget.extension

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import run.piece.dev.R
import run.piece.dev.databinding.CustomSnackbarBinding
import run.piece.dev.databinding.SnackbarBtnCustomBinding
import run.piece.dev.databinding.SnackbarCommonBinding
import kotlin.math.roundToInt

/**
 *packageName    : run.piece.dev.widget.extension
 * fileName       : SnackBarCommon
 * author         : piecejhm
 * date           : 2023/01/06
 * description    : SnackBar 공통
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023/01/06        piecejhm       최초 생성
 */


class SnackBarCommon(view: View, private val message: String, private val type: String) {

    companion object {
        private var displayType: String = ""
        fun make(view: View, message: String, type: String) = SnackBarCommon(view, message, type)
    }

    private val context = view.context
    private val snackbar = Snackbar.make(view, "", 2000)
    private val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

    private val inflater = LayoutInflater.from(context)

    private val snackbarBinding: SnackbarCommonBinding =
        DataBindingUtil.inflate(inflater, R.layout.snackbar_common, null, false)

    // 링크 복사 - jhm 2023/01/09
    private val snackbarLinkBinding: CustomSnackbarBinding =
        DataBindingUtil.inflate(inflater, R.layout.custom_snackbar, null, false)

    // 북마크 , 예치금 새로고침
    private val snackbarBtnCustomBinding: SnackbarBtnCustomBinding =
        DataBindingUtil.inflate(inflater, R.layout.snackbar_btn_custom, null, false)

    init {
        initView()
        initData()
    }

    private fun initView() {

        with(snackbarLayout) {
            removeAllViews()
            setPadding(0, 0, 0, 0)
            bringToFront()
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            getDisplayType(displayMetrics = resources.displayMetrics)


            when (type) {
                "광고" -> {
                    addView(snackbarBinding.root, 0)
                }

                "링크복사" -> {
                    addView(snackbarLinkBinding.root, 0)
                }

                "북마크" -> {
                    addView(snackbarBtnCustomBinding.root, 0)
                }

                "정보성" -> {
                    addView(snackbarBinding.root, 0)
                }

                "블록체인ID" -> {
                    addView(snackbarBinding.root, 0)
                }

                context.getString(R.string.custom_snackbar_type_bank_num_txt) -> {
                    addView(snackbarBinding.root, 0)
                }

                context.getString(R.string.custom_snackbar_type_refresh) -> {
                    addView(snackbarBinding.root, 0)
                    setPadding(48,0,48,0)
                }

                context.getString(R.string.nh_deposit_refresh_txt) -> {
                    addView(snackbarBtnCustomBinding.root, 0)
                }

                context.getString(R.string.investment_error_title) -> {
                    addView(snackbarBinding.root, 0)
                }

                "포트폴리오 알림" -> {
                    addView(snackbarBinding.root, 0)
                }

                "광고성 정보 활용 및 수신 알림" -> {
                    addView(snackbarBinding.root, 0)
                    setPadding(48, 0, 48, 0)
                }

                "정보성 알림 수신 동의" -> {
                    addView(snackbarBinding.root, 0)
                    setPadding(48, 0, 48, 0)
                }

                "광고성 알림 수신에 동의" -> {
                    addView(snackbarBinding.root, 0)
                    setPadding(48, 0, 48, 0)
                }
            }

            if(type == "북마크" || type == context.getString(R.string.nh_deposit_refresh_txt)) {
                when(displayType) {
                    "FOLD_DISPLAY_EXPAND" -> {
                        val layoutParams = snackbarBtnCustomBinding.title.layoutParams
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        snackbarBtnCustomBinding.title.layoutParams = layoutParams
                    }
                    else -> {
                        val layoutParams = snackbarBtnCustomBinding.title.layoutParams
                        layoutParams.width = 0
                        snackbarBtnCustomBinding.title.layoutParams = layoutParams
                        setPadding(48, 0, 48, 0)
                    }
                }
            }


        }
    }

    private fun initData() {
        when (type) {
            "광고" -> {
                snackbarBinding.title.text = message
            }

            "링크복사" -> {
                snackbarLinkBinding.title.text = message
            }

            "북마크" -> {
                snackbarBtnCustomBinding.title.text = message
                snackbarBtnCustomBinding.btn.text = "북마크 보기"
                snackbarBtnCustomBinding.customSnackbar.setOnClickListener {
                    itemClickListener?.onClick(snackbarBtnCustomBinding.btn)
                }
            }

            "정보성" -> {
                snackbarBinding.title.text = message
            }

            "블록체인ID" -> {
                snackbarBinding.title.text = message
            }

            context.getString(R.string.custom_snackbar_type_bank_num_txt) -> {
                snackbarBinding.title.text = message
            }

            context.getString(R.string.custom_snackbar_type_refresh) -> {
                snackbarBinding.title.text = message
            }

            context.getString(R.string.nh_deposit_refresh_txt) -> {
                snackbarBtnCustomBinding.title.text = message
                snackbarBtnCustomBinding.btn.text = context.getString(R.string.nh_deposit_refresh_txt)
                snackbarBtnCustomBinding.btn.setOnClickListener {
                    itemClickListener?.onClick(snackbarBtnCustomBinding.btn)
                }
            }

            context.getString(R.string.investment_error_title) -> {
                snackbarBinding.title.text = message
            }

            "포트폴리오 알림" -> {
                snackbarBinding.title.text = message
            }

            "광고성 정보 활용 및 수신 알림" -> {
                snackbarBinding.title.text = message
            }

            "정보성 알림 수신 동의" -> {
                snackbarBinding.title.text = message
            }

            "광고성 알림 수신에 동의" -> {
                snackbarBinding.title.text = message
            }
        }
    }

    fun show(margin: Int, widthMargin: Int? = null) {
        snackbar.show()
        val dm = context.resources.displayMetrics

        widthMargin?.let {
            val marginLeftRight = (it * dm.density).roundToInt()
            snackbar.view.setMargins(marginLeftRight, 0, marginLeftRight, (margin * dm.density).roundToInt())
        }?: run {
            snackbar.view.setMargins(0, 0, 0, (margin * dm.density).roundToInt())
        }


    }

    // (4) setItemClickListener로 설정한 함수 실행
    private var itemClickListener: OnItemClickListener? = null

    // (2) 리스너 인터페이스
    interface OnItemClickListener {
        fun onClick(v: View)
    }

    // (3) 외부에서 클릭 시 이벤트 설정
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    fun View.setMargins(
        left: Int = this.marginLeft,
        top: Int = this.marginTop,
        right: Int = this.marginRight,
        bottom: Int = this.marginBottom,
    ) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            setMargins(left, top, right, bottom)
        }
    }

    private fun getDisplayType(displayMetrics: DisplayMetrics): String {
        return when {
            displayMetrics.widthPixels > 1600 -> {
                displayType = "FOLD_DISPLAY_EXPAND"
                displayType
            }

            displayMetrics.widthPixels < 980 -> {
                displayType = "FOLD_DISPLAY_COLLAPSE"
                displayType
            }

            else -> {
                displayType = "BASIC_DISPLAY"
                displayType
            }
        }
    }

}
package run.piece.dev.refactoring.ui.purchase

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import run.piece.dev.R
import run.piece.dev.databinding.SlideupPurchaseNoticeBinding
import run.piece.dev.refactoring.base.BasePdfActivity
import run.piece.dev.refactoring.ui.faq.FaqTabActivity
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toDateFormat
import run.piece.domain.refactoring.portfolio.model.AttachFileItemVo
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo
import run.piece.domain.refactoring.portfolio.model.PortfolioStockItemVo


class PurchaseBtDlg(context: Context) : BottomSheetDialogFragment() {
    lateinit var binding: SlideupPurchaseNoticeBinding
    private var listener: OnSendFromBottomSheetDialog? = null
    private val display = context.resources?.displayMetrics // Fold UI 대응
    lateinit var activity: PurchaseRenewalActivity

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SlideupPurchaseNoticeBinding.inflate(inflater, container , false)
        binding.lifecycleOwner = this@PurchaseBtDlg
        binding.apply {

            arguments?.let { bundle ->
                // PortfolioDetail 데이터
                val detailDefaultVo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable("detailDefaultVo", PortfolioDetailDefaultVo::class.java)
                } else {
                    bundle.getParcelable("detailDefaultVo")
                }

                val stockVo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable("stockVo", PortfolioStockItemVo::class.java)
                } else {
                    bundle.getParcelable("stockVo")
                }


                val filesVo: List<AttachFileItemVo>? = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList("attachFileItemVo", AttachFileItemVo::class.java)
                } else {
                    bundle.getParcelableArrayList("attachFileItemVo")
                }

                detailDefaultVo?.let {
                    var requestOptions = RequestOptions()
                    requestOptions = requestOptions
                        .transform(CenterCrop(), RoundedCorners(15))
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)

                    Glide.with(requireContext())
                        .load(detailDefaultVo.representThumbnailImagePath)
                        .apply(requestOptions)
                        .into(portfolioIv)

                    portfolioNumberTv.text = detailDefaultVo.subTitle
                    portfolioTitleTv.text = detailDefaultVo.title
                    withdrawDateTv.text = detailDefaultVo.prizeAt.toDateFormat().default()
                }

                stockVo?.let { stock ->
                    offeringPriceTv.text = "${stock.faceValue.toLong().decimalComma()}(1주당)"
                }

                amountDateTv.text = bundle.getInt("userVolume").decimalComma() + "주"
                totalPriceTv.text = bundle.getInt("userPrice").decimalComma() + "원"


                val customText = SpannableString(getString(R.string.notice_title_txt))
                val faqSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        startActivity(Intent(activity, FaqTabActivity::class.java))
                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.parseColor("#757983")
                        ds.isUnderlineText = true
                    }
                }
                customText.setSpan(faqSpan, 10, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                val descriptionSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        detailDefaultVo?.let {
                            stockVo?.let {
                                filesVo?.let {
                                    startActivity(BasePdfActivity.getPurchaseBtPdfIntent(
                                        activity,
                                        bundle.getString("attachFileCode").toString(),
                                        getString(R.string.purchase_manual_txt),
                                        detailDefaultVo = detailDefaultVo,
                                        stockVo = stockVo,
                                        attachFileItemVo = filesVo,
                                        "PurchaseBtDlg"
                                    ))
                                }
                            }
                        }

                    }
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.parseColor("#757983")
                        ds.isUnderlineText = true
                    }
                }
                customText.setSpan(descriptionSpan, 16, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                notiTitleTv.text = customText
                notiTitleTv.movementMethod = LinkMovementMethod.getInstance()

                purchaseBtn.onThrottleClick {
                    listener?.sendValue(
                        detailDefaultVo?.portfolioId.default(),
                        detailDefaultVo?.title.default(),
                        bundle.getInt("userVolume"),
                        detailDefaultVo?.prizeAt.default())
                }
            }


        }

        isCancelable = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;
    }


    interface OnSendFromBottomSheetDialog {
        fun sendValue(portfolioId: String, portfolioTitle: String, userVolume: Int, endDate: String)
    }

    fun setCallback(listener: OnSendFromBottomSheetDialog) {
        this.listener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as PurchaseRenewalActivity
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState) as BottomSheetDialog
    }

    private fun getBottomSheetDialogDefaultHeight(): Double {
        if(display != null) {
            // 폴드 펼침
            if(display.widthPixels > 1600) {
                return getWindowHeight() * 68.9 / 100
            }
            // 미니 , 폴드 닫힘
            else if(display.widthPixels < 980) {
                return getWindowHeight() * 65.0 / 100
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

}
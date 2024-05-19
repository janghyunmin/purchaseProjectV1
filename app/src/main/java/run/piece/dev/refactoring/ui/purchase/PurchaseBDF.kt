package run.piece.dev.refactoring.ui.purchase

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import run.piece.dev.R
import run.piece.dev.databinding.BdfPurchaseCancelBinding
import run.piece.dev.refactoring.base.BaseBDF
import run.piece.dev.refactoring.base.BasePdfActivity
import run.piece.dev.refactoring.ui.faq.FaqTabActivity
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toDateFormat
import run.piece.dev.refactoring.utils.toDecimalComma
import run.piece.domain.refactoring.portfolio.model.PortfolioDetailDefaultVo
import run.piece.domain.refactoring.purchase.model.PurchaseInfoVo

class PurchaseBDF : BaseBDF() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return when (arguments?.getString("viewType")) {
            "cancel" -> {
                updateBdfStyle(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                inflater.inflate(R.layout.bdf_purchase_cancel, container, false)
            }
            else -> null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (arguments?.getString("viewType")) {
            "cancel" -> {
                BdfPurchaseCancelBinding.bind(view).apply {
                    lifecycleOwner = this@PurchaseBDF

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
                            arguments?.getString("investmentProspectusUrl")?.let {
                                if (it.isNotBlank()) {
                                    startActivity(BasePdfActivity.getBasePdfIntent(requireContext(), it, getString(R.string.purchase_manual_txt), "PortfolioNotBtn"))
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

                    val purchaseInfoVo = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arguments?.getParcelable("purchaseInfoVo", PurchaseInfoVo::class.java)
                    } else arguments?.get("purchaseInfoVo") as PurchaseInfoVo

                    val portfolioDetailDefaultVo = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        arguments?.getParcelable("portfolioDetailDefaultVo", PortfolioDetailDefaultVo::class.java)
                    } else arguments?.get("portfolioDetailDefaultVo") as PortfolioDetailDefaultVo

                    portfolioDetailDefaultVo?.let {
                        var requestOptions = RequestOptions()
                        requestOptions = requestOptions
                            .transform(CenterCrop(), RoundedCorners(15))
                            .skipMemoryCache(false)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)

                        Glide.with(requireContext())
                            .load(it.representThumbnailImagePath)
                            .apply(requestOptions)
                            .into(portfolioIv)

                        portfolioNumberTv.text = it.subTitle
                        portfolioTitleTv.text = it.title
                    }

                    purchaseInfoVo?.let {
                        withdrawContentTv.text = purchaseInfoVo.purchaseAt.toDateFormat("year")
                        amountContentTv.text = "${purchaseInfoVo.offerPieceVolume.toDecimalComma()}주"
                        offeringPriceContentTv.text = "${purchaseInfoVo.minPurchaseAmount.toDecimalComma()}원(1주당)"
                        totalPriceContentTv.text = "${purchaseInfoVo.offerTotalAmount.toDecimalComma()}원"
                    }

                    purchaseBtn.onThrottleClick {
                        cancelEvent?.invoke()
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        private var requestEvent: (() -> Unit)? = null
        private var cancelEvent: (() -> Unit)? = null

        fun newPurchaseCancel(purchaseInfoVo: PurchaseInfoVo,
                              portfolioDetailDefaultVo: PortfolioDetailDefaultVo,
                              investmentProspectusUrl: String = "",
                              cancelEvent: (() -> Unit)? = null): PurchaseBDF {
            this.cancelEvent = cancelEvent

            val bdf = PurchaseBDF()
            val bundle = Bundle()
            bundle.putString("investmentProspectusUrl", investmentProspectusUrl)
            bundle.putString("viewType", "cancel")
            bundle.putParcelable("purchaseInfoVo", purchaseInfoVo)
            bundle.putParcelable("portfolioDetailDefaultVo", portfolioDetailDefaultVo)
            bdf.arguments = bundle
            return bdf
        }

        fun newPurchaseRequest(receiveEvent: (() -> Unit)? = null): PurchaseBDF {
            this.requestEvent = receiveEvent

            val bdf = PurchaseBDF()
            val bundle = Bundle()
            bundle.putString("viewType", "request")
            bdf.arguments = bundle
            return bdf
        }
    }
}
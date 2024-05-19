package run.piece.dev.refactoring

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import run.piece.dev.R
import run.piece.dev.databinding.DfAppConfirmBinding
import run.piece.dev.refactoring.base.BaseDF
import run.piece.dev.refactoring.utils.onThrottleClick
import javax.inject.Singleton

@Singleton
class AppConfirmDF: BaseDF() {
    companion object {
        private var dismissAction: ((Unit) -> Unit)? = null

        private var positiveAction: ((AppConfirmDF) -> Unit)? = null
        private var negativeAction: ((AppConfirmDF) -> Unit)? = null

        private var _binding: DfAppConfirmBinding? = null
        private val binding get() = _binding!!

        fun newInstance(title: String,
                        message: String,
                        cancelable: Boolean = false,
                        @StringRes positiveStrRes: Int,
                        positiveAction: ((AppConfirmDF) -> Unit)? = null,
                        @StringRes negativeStrRes: Int? = null,
                        negativeAction: ((AppConfirmDF) -> Unit)? = null,
                        backgroundDrawable: Int? = null,
                        strongText: String? = null,
                        dismissAction: ((Unit) -> Unit)? = null): AppConfirmDF {

            val df = AppConfirmDF()
            val bundle = Bundle()

            this.positiveAction = positiveAction
            this.negativeAction = negativeAction
            this.dismissAction = dismissAction

            bundle.putString("title", title)
            bundle.putString("message", message)
            bundle.putString("strongText", strongText)
            bundle.putInt("positiveStrRes", positiveStrRes)
            bundle.putBoolean("cancelable", cancelable)

            if (backgroundDrawable != null) { bundle.putInt("backgroundDrawable", backgroundDrawable) }
            if (negativeStrRes != null) { bundle.putInt("negativeStrRes", negativeStrRes) }

            df.arguments = bundle
            return df
        }
    }

    override fun getWidthMarginDp(): Float {
        return 16f
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.df_app_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = DfAppConfirmBinding.bind(view)
        _binding?.lifecycleOwner = this

        val valContext = context
        val valArguments = arguments

        binding.apply {
            if (valContext != null && valArguments != null) {
                val title = valArguments.getString("title")
                val message = valArguments.getString("message")
                val positiveStrRes = valArguments.getInt("positiveStrRes", R.string.confirm)

                contentTv.text = message
                titleTv.text = title

                okBtn.text = getString(positiveStrRes)

                if (valArguments.containsKey("backgroundDrawable")) {
                    val drawableId = valArguments.getInt("backgroundDrawable")
                    okBtn.setBackgroundResource(drawableId)
                }

                if (valArguments.containsKey("strongText")) {

                    valArguments.getString("strongText")?.let { strong ->

                        message?.let { message ->
                            val start: Int = message.indexOf(strong)
                            val end = start + strong.length
                            val ssb = SpannableStringBuilder(message)

                            ssb.setSpan(
                                ForegroundColorSpan(Color.parseColor("#F95D5D")),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )

                            contentTv.text = ssb
                        }
                    }
                }

                okBtn.onThrottleClick {
                    positiveAction?.invoke(this@AppConfirmDF)
                    this@AppConfirmDF.dismiss()
                }

                if (valArguments.containsKey("negativeStrRes")) {
                    val negativeStrRes = valArguments.getInt("negativeStrRes")
                    cancelBtn.visibility = View.VISIBLE
                    cancelBtn.text = getString(negativeStrRes)

                    cancelBtn.onThrottleClick {
                        negativeAction?.invoke(this@AppConfirmDF)
                        this@AppConfirmDF.dismiss()
                    }

                    //버튼이 2개인 경우 okBtn의 margin 설정
                    val params = okBtn.layoutParams as ViewGroup.MarginLayoutParams
                    params.setMargins(
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10F, valContext.resources.displayMetrics).toInt(),
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28F, valContext.resources.displayMetrics).toInt(),
                        0,
                        0)

                    okBtn.layoutParams = params

                } else {
                    //버튼이 1개인 경우
                    cancelBtn.visibility = View.GONE
                }

                val cancelable = valArguments.getBoolean("cancelable", false)

                isCancelable = cancelable
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissAction?.invoke(Unit)
    }
}
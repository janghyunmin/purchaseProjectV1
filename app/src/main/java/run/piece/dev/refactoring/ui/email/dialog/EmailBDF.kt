package run.piece.dev.refactoring.ui.email.dialog

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import run.piece.dev.R
import run.piece.dev.databinding.BdfEmailCertificateBinding
import run.piece.dev.databinding.BdfEmailRegisterBinding
import run.piece.dev.refactoring.base.BaseBDF
import run.piece.dev.refactoring.utils.onThrottleClick
import java.util.regex.Pattern

class EmailBDF : BaseBDF() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return when (arguments?.getString("viewType")) {
            "certificate" -> {
                updateBdfStyle(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                inflater.inflate(R.layout.bdf_email_certificate, container, false)
            }
            "register" -> {
                updateBdfStyle(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                inflater.inflate(R.layout.bdf_email_register, container, false)
            }
            else -> null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (arguments?.getString("viewType")) {
            "certificate" -> {
                BdfEmailCertificateBinding.bind(view).apply {
                    lifecycleOwner = this@EmailBDF

                    retouchCv.onThrottleClick {
                        dismiss()
                        retouchEvent?.invoke()
                    }

                    receiveCv.onThrottleClick {
                        dismiss()
                        receiveEvent?.invoke()
                    }

                    arguments?.getString("email")?.let {
                        emailTv.text = it
                    }
                }
            }
            "register" -> {
                BdfEmailRegisterBinding.bind(view).apply {
                    lifecycleOwner = this@EmailBDF

                    emailEt.doOnTextChanged { text, start, before, count ->
                        if (text.toString().isNotEmpty()) {
                            clearIv.visibility = View.VISIBLE
                            registerCv.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.p500_10CFC9))
                        } else {
                            clearIv.visibility = View.GONE
                            registerCv.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.g400_DADCE3))
                        }
                        emailErrorTv.visibility = View.GONE
                    }

                    registerCv.onThrottleClick {
                        val pattern: Pattern = Patterns.EMAIL_ADDRESS
                        val isSuccess = pattern.matcher(emailEt.text.toString()).matches()

                        if (isSuccess) emailErrorTv.visibility = View.GONE
                        else emailErrorTv.visibility = View.VISIBLE

                        emailVerification(isSuccess, emailEt.text.toString())
                    }

                    clearIv.onThrottleClick {
                        emailEt.setText("")
                    }
                }
            }
        }
    }

    private fun emailVerification(isSuccess: Boolean, email: String) {
        verificationEvent?.invoke(isSuccess, email)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        private var receiveEvent: (() -> Unit)? = null
        private var retouchEvent: (() -> Unit)? = null

        fun newCertificateInstance(email: String, receiveEvent: (() -> Unit)? = null, retouchEvent: (() -> Unit)? = null): EmailBDF {
            this.receiveEvent = receiveEvent
            this.retouchEvent = retouchEvent

            val bdf = EmailBDF()
            val bundle = Bundle()
            bundle.putString("viewType", "certificate")
            bundle.putString("email", email)
            bdf.arguments = bundle
            return bdf
        }

        private var verificationEvent:((Boolean, String) -> Unit)? = null

        fun newRegisterInstance(verificationEvent: ((Boolean, String) -> Unit)? = null): EmailBDF {
            this.verificationEvent = verificationEvent
            val bdf = EmailBDF()
            val bundle = Bundle()
            bundle.putString("viewType", "register")
            bdf.arguments = bundle
            return bdf
        }
    }
}
package run.piece.dev.view.fragment.dialog

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import run.piece.dev.R
import run.piece.dev.data.api.NetworkInfo
import run.piece.dev.data.api.RetrofitService
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.data.model.BaseDTO
import run.piece.dev.databinding.SlideupProfitBinding
import run.piece.dev.refactoring.utils.setClickEvent
import run.piece.dev.view.common.ErrorActivity
import run.piece.dev.widget.utils.DialogManager
import java.text.DecimalFormat

/**
 *packageName    : com.bsstandard.piece.view.fragment.dialog
 * fileName       : ProfitBottomSheetDialog
 * author         : piecejhm
 * date           : 2022/10/26
 * description    : 분배금 정산 알림 BottomSheetDialog
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/10/26        piecejhm       최초 생성
 */


class ProfitBottomSheetDialog() : BottomSheetDialogFragment() {
    lateinit var binding: SlideupProfitBinding;

    // 분배 받을 시점 변경으로 인한 api 호출 - jhm 2023/02/23
    val apiResponse: RetrofitService? = NetworkInfo.getRetrofit().create(RetrofitService::class.java)


    val accessToken: String = PrefsHelper.read("accessToken", "")
    val deviceId: String = PrefsHelper.read("deviceId", "")
    val memberId: String = PrefsHelper.read("memberId", "")

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        binding = SlideupProfitBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.loading.visibility = View.GONE

        Glide.with(requireContext()).load(R.raw.withdraw_complete_lopping).into(binding.lottie)
        var totalProfitAmount = arguments?.getString("totalProfitAmount").toString()

        val decimal = DecimalFormat("###,###")
        var depositText : String = ""
        depositText = decimal.format(totalProfitAmount.toInt())
        // 분배금 전환 금액 - jhm 2022/10/26
        binding.number.text = depositText

        binding.notice.text = "수익 분배금이 ${PrefsHelper.read("name","")}님을 기다리고 있어요! \n 실명인증을 하시면 예치금으로 입금해 드려요."


        // 지금 실명인증 하기 - jhm 2022/10/26
        binding.confirmBtn.setClickEvent(lifecycleScope) {
            binding.loading.visibility = View.VISIBLE

            apiResponse?.postDepositBalance(
                "Bearer $accessToken", deviceId, memberId
            )?.enqueue(object : Callback<BaseDTO> {
                override fun onResponse(
                    call: Call<BaseDTO>,
                    response: Response<BaseDTO>
                ) {
                    try {
                        binding.loading.visibility = View.GONE

                        if (response.code() == 200) {

                            DialogManager.openNotGoDalog(
                                requireContext(),
                                "실명 인증 완료",
                                "분배금이 예치금으로 입금되었어요."
                            )
                        } else {

                            DialogManager.openNotGoDalog(
                                requireContext(),
                                "분배금 전환에 실패했어요!",
                                "분배금이 존재하지 않습니다."
                            )
                        }
                    } catch (ex: Exception) {
                        binding.loading.visibility = View.GONE
                        dismiss()

                        val intent = Intent(context, ErrorActivity::class.java)
                        startActivity(intent)

                        ex.printStackTrace()

                    }
                }

                override fun onFailure(
                    call: Call<BaseDTO>,
                    t: Throwable
                ) {
                    binding.loading.visibility = View.GONE
                    t.printStackTrace()


                    dismiss()

                    val intent = Intent(context, ErrorActivity::class.java)
                    startActivity(intent)


                }
            })
            dismiss()
        }

        return binding.root
    }

}
package run.piece.dev.refactoring.ui.more

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient
import com.tbuonomo.viewpagerdotsindicator.setBackgroundCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.BuildConfig
import run.piece.dev.R
import run.piece.dev.data.db.datasource.shared.PrefsHelper
import run.piece.dev.databinding.FragmentNewMoreBinding
import run.piece.dev.refactoring.ui.certification.NewCertificationActivity
import run.piece.dev.refactoring.ui.consent.NewConsentActivity
import run.piece.dev.refactoring.ui.event.EventActivity
import run.piece.dev.refactoring.ui.info.NewMyInfoActivity
import run.piece.dev.refactoring.ui.intro.IntroActivity
import run.piece.dev.refactoring.ui.newinvestment.InvestMentResultActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.newinvestment.InvestmentIntroActivity
import run.piece.dev.refactoring.ui.notice.NoticeActivity
import run.piece.dev.refactoring.ui.notification.NewNotificationSettingActivity
import run.piece.dev.refactoring.ui.question.QuestionActivity
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.investment.model.InvestMentVo

@AndroidEntryPoint // Dagger Hilt를 사용하여 코드베이스에서 의존성 주입을 설정하고 관리하기 위해 사용되는 어노테이션입니다.
class NewFragmentMore : Fragment(R.layout.fragment_new_more) { // 더 보기 화면
    private lateinit var coroutineScope: CoroutineScope
    private var _binding: FragmentNewMoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoreViewModel by viewModels()
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewMoreBinding.inflate(inflater, container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.fragment = this@NewFragmentMore
        _binding?.viewModel = viewModel

        coroutineScope = viewLifecycleOwnerLiveData.value?.lifecycleScope ?: lifecycleScope


        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            versionContentTv.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            versionContentTv.text = "v ${BuildConfig.VERSION_NAME}"
            setStatusBarIconColor()

            App()
        }

        /* 이 블록 안의 코드는 fragment 또는 activity가 STARTED 상태에 있을 때 실행됩니다. 메모리 누수를 방지 가능 ! */
        coroutineScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch(Dispatchers.IO) {
                    if(viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                        viewModel.getMemberDeviceCheck()
                    }
                }

                launch(Dispatchers.Main) {
                    if(viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
                        try {
                            viewModel.deviceChk.collect {
                                when(it) {
                                    is MoreViewModel.MemberDeviceState.Success -> {
//                                        LogUtil.e("=== MemberDeviceChk Success === ${it.isSuccess}")
                                    }
                                    is MoreViewModel.MemberDeviceState.Failure -> {
//                                        LogUtil.e("=== MemberDeviceChk Failure === ${it.message}")

                                        val statusCode = extractStatusCode(it.message)

                                        if(statusCode != 406) {
                                            PrefsHelper.removeKey("inputPinNumber")
                                            PrefsHelper.removeKey("memberId")
                                            PrefsHelper.removeKey("isLogin")

                                            startActivity(getIntroActivity(requireContext()))
                                            BackPressedUtil().activityFinish(mainActivity,mainActivity)
                                        }


                                    }
                                    else -> {
//                                        LogUtil.e("=== MemberDeviceChk More === $it")
                                    }
                                }
                            }
                        } catch (ex : Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                arguments?.let { vo ->
                    //초기 값 설정
                    binding.userNameTv.text = "${vo.getString("userName")?: ""} 님"
                    binding.dateTv.text = "${vo.getString("joinDay")?: ""} 일"
                    binding.investResultTv.text = vo.getString("result")?: getString(R.string.investment_more_item_null_title)
                    binding.investBtn.text = vo.getString("btnTitle")?: getString(R.string.investment_more_item_null_title)
                    binding.arrowRightLayout.onThrottleClick {
                        //통신 실패시 MainActivity에서 전달받은 최초의 유저 정보를 전달합니다.
                        goMyInfo(
                            userName = vo.getString("userName")?: "",
                            birthDay = vo.getString("birthDay")?: "",
                            phoneNumber = vo.getString("phoneNumber")?: "",
                            baseAddress = vo.getString("baseAddress")?: "",
                            detailAddress = vo.getString("detailAddress")?: "",
                            email = vo.getString("email")?: ""
                        )
                    }
                }


                launch(Dispatchers.Main) {
                    with(binding) {
                        userNameTv.text = ""
                        dateTv.text = ""
                        investBtn.text = ""
                        investResultTv.text = ""
                    }
                }.join()

                launch(Dispatchers.Main) {
                    viewModel.memberInfo.collect { vo ->
                        when(vo) {
                            is MoreViewModel.MembeInfoState.Success -> {

                                //통신 성공시 새로운 데이터를 전달합니다.
                                binding.userNameTv.text = "${vo.memberVo.name} 님"
                                binding.dateTv.text = "${vo.memberVo.joinDay} 일"

                                // 투자 성향 분석 ( 투자 DNA )
                                // 투자 성향 분석을 한번도 진행 하지 않은 사용자는 preference 객체가 null
                                if(vo.memberVo.preference == null) {
                                    binding.investBtn.text = getString(R.string.investment_more_item_null_btn_title)
                                    binding.investBtn.setTextColor(ContextCompat.getColor(requireContext(),R.color.c_21b5b5))
                                    binding.investBtn.setBackgroundCompat(ContextCompat.getDrawable(requireContext(),R.drawable.layout_round_cff5f4))
                                    binding.investResultTv.text = getString(R.string.investment_more_item_null_title)
                                    binding.investBtn.onThrottleClick {
                                        startActivity(InvestmentIntroActivity.getIntent(requireContext(), userName = vo.memberVo.name))
                                        //startActivity(getInvestMentIntroActivity(requireContext(),vo.memberVo.name))
                                    }
                                } else {
                                    binding.investBtn.text = getString(R.string.investment_more_item_btn_title)
                                    binding.investBtn.setTextColor(ContextCompat.getColor(requireContext(),R.color.c_757983))
                                    binding.investResultTv.text = vo.memberVo.preference?.result
                                    binding.investBtn.setBackgroundCompat(ContextCompat.getDrawable(requireContext(),R.drawable.layout_round_eaecf0))
                                    binding.investBtn.onThrottleClick {
                                        // 내정보 vo를 investmentVo로 데이터를 넣어주고 화면 이동
                                        val investVo = InvestMentVo(
                                            vo.memberVo.preference!!.resultId,
                                            vo.memberVo.preference!!.minScore,
                                            vo.memberVo.preference!!.maxScore,
                                            vo.memberVo.preference!!.result,
                                            vo.memberVo.preference!!.description,
                                            vo.memberVo.preference!!.resultImagePath,
                                            vo.memberVo.preference!!.interestProductDescription,
                                            vo.memberVo.preference!!.memberId,
                                            vo.memberVo.preference!!.name,
                                            vo.memberVo.preference!!.score,
                                            vo.memberVo.preference!!.count,
                                            vo.memberVo.preference!!.isVulnerableInvestors,
                                            vo.memberVo.preference!!.createdAt,
                                        )
                                        startActivity(getInvestMentResultActivity(requireContext(),investVo))
                                    }
                                }

                                binding.arrowRightLayout.onThrottleClick {
                                    goMyInfo(
                                        userName = vo.memberVo.name,
                                        birthDay = vo.memberVo.birthDay,
                                        phoneNumber = vo.memberVo.cellPhoneNo,
                                        baseAddress = vo.memberVo.baseAddress,
                                        detailAddress = vo.memberVo.detailAddress,
                                        email = vo.memberVo.email
                                    )
                                }
                            }

                            is MoreViewModel.MembeInfoState.Failure -> {
//                                startActivity(ErrorActivity.getIntent(requireActivity()))
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = viewLifecycleOwnerLiveData.value?.lifecycleScope ?: lifecycleScope
        if(viewModel.isLogin.isNotEmpty() && viewModel.memberId.isNotEmpty()) {
            viewModel.getMemberData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    private fun goMyInfo(userName: String, birthDay: String, phoneNumber: String, baseAddress: String, detailAddress: String, email: String) {
        context?.startActivity(NewMyInfoActivity.getIntent(
            requireActivity(),
            userName = userName,
            birthDay = birthDay,
            phoneNumber = phoneNumber,
            baseAddress = baseAddress,
            detailAddress = detailAddress,
            email = email)
        ) // 내정보 페이지로 이동
    }
    // 공지사항으로 이동
    fun goNotice() {
        context?.startActivity(Intent(context, NoticeActivity::class.java))
    }

    // 이벤트로 이동
    fun goEvent() {
        context?.startActivity(Intent(context, EventActivity::class.java))
    }

    // 인증 및 보안으로 이동
    fun goAccess() {
        context?.startActivity(Intent(context, NewCertificationActivity::class.java))
    }

    // 알림 및 설정으로 이동
    fun goNotiSetting() {
        context?.startActivity(NewNotificationSettingActivity.getIntent(requireActivity()))
    }

    // 약관 및 개인정보 처리로 이동
    fun goTerms() {
        context?.startActivity(NewConsentActivity.getIntent(requireActivity()))
    }

    // 자주 묻는 질문으로 이동
    fun goQuestion() {
        context?.startActivity(Intent(context, QuestionActivity::class.java))
    }
    fun goKakao() {
        try {
            KakaoSdk.init(requireActivity(), "", "", false)
            // chatChannelUrl
            // 카카오톡 채널 추가하기 URL
            val url = TalkApiClient.instance.chatChannelUrl("_XLxjmK")
            // CustomTabs 로 열기
            // 1. 크롬 브라우저로 실행하는 경우
            try {
                KakaoCustomTabsClient.openWithDefault(requireContext(), url)
            } catch (e: UnsupportedOperationException) {
                Toast.makeText(context, "크롬 브라우저가 없습니다", Toast.LENGTH_SHORT).show()
            }
            // 2 .디바이스에 설치된 인터넷 브라우저로 실행하는 경우
            try {
                KakaoCustomTabsClient.openWithDefault(requireContext(), url)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "디바이스에 설치된 인터넷 브라우저가 없습니다", Toast.LENGTH_SHORT).show()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun setStatusBarIconColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else activity?.window?.decorView?.systemUiVisibility = activity?.window?.decorView!!.systemUiVisibility
    }

    private fun extractStatusCode(errorMessage: String): Int {
        val regex = Regex("""(\d{3})""")
        val matchResult = regex.find(errorMessage)
        return matchResult?.value?.toInt() ?: -1
    }

    companion object {
        fun newInstance(title: String) : NewFragmentMore {
            return NewFragmentMore().apply {
                arguments = bundleOf("title" to title)
            }
        }

        // 로그인, 서비스 둘러보기 화면 이동
        fun getIntroActivity(context: Context): Intent {
            val intent = Intent(context, IntroActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("another","another")
            return intent
        }

        fun getInvestMentResultActivity(context: Context, investMentVo: InvestMentVo): Intent {
            val intent = Intent(context, InvestMentResultActivity::class.java)
            intent.putExtra("data",investMentVo)
            return intent
        }
    }
}
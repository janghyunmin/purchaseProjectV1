package run.piece.dev.refactoring.ui.address

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.R
import run.piece.dev.databinding.NewSlideupAddressBinding
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.domain.refactoring.common.model.JusoVo

class NewAddressBtDlg(context: Context) : BottomSheetDialogFragment() {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var binding: NewSlideupAddressBinding
    private lateinit var viewModel: AddressViewModel
    private var addressDetailBtDlg: NewAddressDetailBtDlg? = null
    private val display = context.resources?.displayMetrics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(requireActivity())[AddressViewModel::class.java]
        binding = NewSlideupAddressBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = this@NewAddressBtDlg
            viewModel = viewModel
            loading.visibility = View.GONE

            closeTouchLayout.onThrottleClick {
                dismiss()
            }
        }

        coroutineScope = viewLifecycleOwner.lifecycleScope

        isCancelable = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View);
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO;

        binding.addressSearchEt.addTextChangedListener { edit ->
            if (edit != null) {
                if (edit.isNotEmpty() || edit.isNotBlank()) {
                    binding.clearIv.visibility = View.VISIBLE
                    binding.clearIv.onThrottleClick {
                        binding.addressSearchEt.text = null
                        binding.clearIv.visibility = View.GONE
                        emptyText()
                    }
                } else {
                    binding.clearIv.visibility = View.GONE
                }
            }
        }

        // 검색창을 클릭 후 키패드에서 Done 누를때 로직
        binding.addressSearchEt.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (binding.addressSearchEt.text.isNullOrEmpty() || binding.addressSearchEt.text.isNullOrBlank()) {
                binding.emptyTv.visibility = View.VISIBLE // 검색 결과가 없어요 Text
                binding.addressRv.visibility = View.GONE // 검색 결과 RecyclerView
            } else {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    LogUtil.v("Action Done")
                    // 키보드 내리기
                    val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(binding.addressSearchEt.windowToken, 0)

                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.getSearchAddress(binding.addressSearchEt.text.toString(),countPerPage = 300, currentPage = 1)
                    }

                    coroutineScope.launch(coroutineScope.coroutineContext + Dispatchers.Main) {
                        viewModel.addressList.collect { vo ->
                            when (vo) {
                                is AddressViewModel.SearchAddressState.Success -> {
                                    delay(600)

                                    viewInit(vo.data.results.jusoVo)
                                }

                                is AddressViewModel.SearchAddressState.Failure -> {
                                    binding.loading.visibility = View.GONE
                                    LogUtil.v("주소 검색 Fail : ${vo.message}")
                                }

                                else -> {
                                    LogUtil.i("주소 검색 Loading : $vo")
                                    binding.loading.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    return@setOnEditorActionListener true
                }
            }
            return@setOnEditorActionListener false
        }
    }


    override fun onResume() {
        super.onResume()
        coroutineScope = viewLifecycleOwner.lifecycleScope
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
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

    private fun emptyText() {
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.addressSearchEt, InputMethodManager.SHOW_IMPLICIT)
        binding.addressSearchEt.run {
            post {
                isFocusableInTouchMode = true
                requestFocus()
                setSelection(this.length())
            }
        }
    }

    private fun viewInit(vo: List<JusoVo>) {
        binding.loading.visibility = View.GONE
        try {
            if (vo.isEmpty()) {
                binding.emptyTv.visibility = View.VISIBLE
                binding.addressRv.visibility = View.GONE
            } else {
                val addressRvAdapter = NewAddressRvAdapter(requireContext())

                binding.emptyTv.visibility = View.GONE
                binding.addressRv.visibility = View.VISIBLE
                binding.addressRv.itemAnimator = null
                binding.addressRv.setHasFixedSize(true)
                binding.addressRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

                binding.addressRv.adapter = addressRvAdapter
                addressRvAdapter.setOnItemClickListener(object : NewAddressRvAdapter.OnItemClickListener {
                    override fun onItemClick(v: View, zipNo: String, roadAddr: String, jibunAddr: String) {
                        dismiss()
                        addressDetailBtDlg = NewAddressDetailBtDlg(requireContext(), zipNo = zipNo, roadAddr = roadAddr, jibunAddr = jibunAddr)
                        addressDetailBtDlg?.show(requireActivity().supportFragmentManager, "상세 주소 Dlg Open")
                    }
                })

                addressRvAdapter.submitList(vo)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            LogUtil.e("Search Error ! ${exception.message}")
        }

    }
}
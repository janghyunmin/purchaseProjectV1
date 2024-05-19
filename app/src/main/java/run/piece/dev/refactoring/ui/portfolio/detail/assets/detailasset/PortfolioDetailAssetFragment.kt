package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.UiThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.FragmentPortfolioDetailAssetBinding
import run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo.DetailAssetProductItem
import run.piece.dev.refactoring.utils.toDecimalComma
import run.piece.dev.refactoring.utils.toDouble
import run.piece.domain.refactoring.portfolio.model.PortfolioProductVo


@AndroidEntryPoint
class PortfolioDetailAssetFragment : Fragment(R.layout.fragment_portfolio_detail_asset), OnMapReadyCallback {

    private var _binding: FragmentPortfolioDetailAssetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailAssetViewModel by viewModels()

    var location = LatLng(37.2970683, 127.3299056)
    lateinit var map: NaverMap

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPortfolioDetailAssetBinding.bind(view)
        _binding?.lifecycleOwner = this
        _binding?.viewModel = viewModel

        binding.apply {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@PortfolioDetailAssetFragment)

            arguments?.let {
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable("data", PortfolioProductVo::class.java)
                } else it.getParcelable("data")

                val recruitmentState = it.getString("recruitmentState")

                data?.let { vo ->
                    val detailAssetBrandRvAdapter = DetailAssetBrandRvAdapter()
                    val detailAssetFileInfoRvAdapter = DetailAssetFileInfoRvAdapter(requireActivity())
                    val detailAssetRvAdapter = DetailAssetProductRvAdapter(this@PortfolioDetailAssetFragment.viewModel)


                    if (vo.productJoinBizInfo.productJoinBizDetails.isNotEmpty()) {
                        brandRv.apply {
                            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                            adapter = detailAssetBrandRvAdapter
                        }
                        detailAssetBrandRvAdapter.submitList(vo.productJoinBizInfo.productJoinBizDetails)
                    } else brandGroup.visibility = View.GONE

                    // productAttachFiles 배열 길이만큼 submitList 로 넘겨준다.
                    // 배열이 비어있을때에는 attachFileGroup 을 보여주지 않는다.
                    // 또한 분배가 끝났을 경우에도 해당 영역을 보여주지 않는다.
                    if (vo.productAttachFiles.isNotEmpty()) {
                        attachFileInfoRv.apply {
                            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
                            adapter = detailAssetFileInfoRvAdapter
                        }

                        detailAssetFileInfoRvAdapter.submitList(vo.productAttachFiles)
                    } else attachFileGroup.visibility = View.GONE

                    recruitmentState?.let { recruitmentStat ->
                        when (recruitmentStat) {
                            "PRS0111" -> {
                                binding.prs0111Group.visibility = View.GONE
                                binding.attachFileGroup.visibility = View.GONE
                            }
                        }
                    }

                    if (vo.xcoordinates == "0" || vo.ycoordinates == "0") {
                        binding.prs0111Group.visibility = View.GONE
                    }

                    initView(vo, detailAssetRvAdapter)
                }
            }
        }
        /*네이버지도 터치시 우선순위가 스크롤뷰로 뺏겨서 비정상적으로 동작하는 것을 방지하는 코드*/

        val scrollView = (requireActivity() as PortfolioDetailAssetActivity).getScrollView()

        binding.mapView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> scrollView.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> scrollView.requestDisallowInterceptTouchEvent(false)
            }

            return@setOnTouchListener binding.mapView.onTouchEvent(event)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initView(data: PortfolioProductVo, detailAssetRvAdapter: DetailAssetProductRvAdapter) {
        data.isClicked = true

        Glide
            .with(this)
            .load(data.representThumbnailImagePath)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .into(binding.productIv)

        Glide
            .with(this)
            .load(data.productJoinBizInfo.bizThumbnailPath)
            .placeholder(R.drawable.business_no_image)
            .error(R.drawable.business_no_image)
            .into(binding.brandProfileIv)

        binding.productTitleTv.text = data.title
        binding.productPriceTv.text = "${data.recruitmentAmount.toDecimalComma()}원"

        binding.brandCategoryTv.text = data.productJoinBizInfo.bizSubName
        binding.brandNameTv.text = data.productJoinBizInfo.bizName
        location = LatLng(data.ycoordinates.toDouble(), data.xcoordinates.toDouble())

        binding.mapTv.text = data.storageLocation

        // 기초자산 수정부분
        // owner , productDate, ProductScale, productOther isEmpty , "" 처리
        // product attachList 배열 확인 ( 증빙자료 ) 자산구성 - 영수증 , 친필싸인 , 정품인증서 추가 ( 감정평가서 , 보험증권 영역 )
        //

        FlexboxLayoutManager(requireActivity()).apply {
            flexWrap = FlexWrap.WRAP
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }.let {
            binding.productItemRv.layoutManager = it
            binding.productItemRv.adapter = detailAssetRvAdapter
        }

        val productList = ArrayList<DetailAssetProductItem>()
        if (data.owner.isNotBlank()) { productList.add(DetailAssetProductItem(data.owner)) }
        if (data.productDate.isNotBlank()) { productList.add(DetailAssetProductItem(data.productDate)) }
        if (data.productScale.isNotBlank()) { productList.add(DetailAssetProductItem(data.productScale)) }
        if (data.productOther.isNotBlank()) { productList.add(DetailAssetProductItem(data.productOther)) }
        detailAssetRvAdapter.submitList(productList)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        map = naverMap

        naverMap.moveCamera(CameraUpdate.scrollTo(location))

        val marker = Marker()
        marker.position = location
        marker.map = naverMap

        naverMap.moveCamera(CameraUpdate.zoomTo(17.0))

        val uiSettings = naverMap.uiSettings

        uiSettings.isZoomControlEnabled = false

    }

    companion object {
        fun newInstance(data: PortfolioProductVo, recruitmentState: String): PortfolioDetailAssetFragment {
            val fragment = PortfolioDetailAssetFragment()
            val bundle = Bundle().apply {
                putParcelable("data", data)
                putString("recruitmentState", recruitmentState)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
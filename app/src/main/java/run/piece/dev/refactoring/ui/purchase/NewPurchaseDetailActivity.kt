package run.piece.dev.refactoring.ui.purchase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import run.piece.dev.App
import run.piece.dev.databinding.ActivityNewPurchaseDetailBinding
import run.piece.dev.refactoring.base.BasePdfActivity
import run.piece.dev.refactoring.ui.deposit.DepositViewModel
import run.piece.dev.refactoring.ui.deposit.adapter.DepositPurchaseAdapter
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.LogUtil
import run.piece.dev.refactoring.utils.decimalComma
import run.piece.dev.refactoring.utils.default
import run.piece.dev.refactoring.utils.onThrottleClick
import run.piece.dev.refactoring.utils.toBaseDateFormat
import run.piece.dev.refactoring.utils.toDecimalComma
import run.piece.dev.refactoring.utils.toKoreanDateFormat
import run.piece.dev.view.common.NetworkActivity
import run.piece.dev.widget.utils.ConvertMoney
import run.piece.dev.widget.utils.NetworkConnection
import run.piece.domain.refactoring.deposit.model.PurchaseVo
import java.security.MessageDigest

@AndroidEntryPoint
class NewPurchaseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewPurchaseDetailBinding
    private lateinit var coroutineScope: CoroutineScope
    private val vm: DepositViewModel by viewModels()
    private var position: Int = 0
    private var displayType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App()

        binding = ActivityNewPurchaseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            val networkConnection = NetworkConnection(this@NewPurchaseDetailActivity)
            networkConnection.observe(this@NewPurchaseDetailActivity) { isConnected ->
                if (!isConnected) startActivity(getNetworkActivity(this@NewPurchaseDetailActivity))
            }

            lifecycleOwner = this@NewPurchaseDetailActivity
            activity = this@NewPurchaseDetailActivity
            viewModel = vm
            coroutineScope = lifecycleScope

            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                WindowInsetsControllerCompat(this, this.decorView).isAppearanceLightStatusBars = true
            }


            intent?.let {
                position = it.getIntExtra("position", 0)
            }

            initialize(position)

        }


        BackPressedUtil().activityCreate(this@NewPurchaseDetailActivity, this@NewPurchaseDetailActivity)
        BackPressedUtil().systemBackPressed(this@NewPurchaseDetailActivity, this@NewPurchaseDetailActivity)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope = lifecycleScope
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun initialize(position: Int) {
        val apiJob = coroutineScope.launch(Dispatchers.IO) {
            vm.getDepositPurchaseV1("v0.0.1")
        }

        val displayTypeJob = coroutineScope.launch(Dispatchers.Main) {
            displayType = getDisplayType(resources.displayMetrics)
        }

        coroutineScope.launch(Dispatchers.Main) {
            apiJob.join()
            displayTypeJob.join()

            this@NewPurchaseDetailActivity.vm.purchaseListV1.collect { vo ->
                when (vo) {
                    is DepositViewModel.DepositPurchaseV1State.Success -> {
                        updateUI(vo.data, position)
                    }

                    is DepositViewModel.DepositPurchaseV1State.Failure -> {
                        LogUtil.v("구매 목록 조회 실패")
                        binding.loading.visibility = View.VISIBLE
                    }

                    else -> {
                        binding.loading.visibility = View.VISIBLE
                        binding.parentLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(vo: List<PurchaseVo>, position: Int) {
        binding.apply {
            coroutineScope.launch {
                delay(300)
                binding.loading.visibility = View.GONE
                binding.parentLayout.visibility = View.VISIBLE

                var requestOptions = RequestOptions()

                when (displayType) {
                    "FOLD_DISPLAY_EXPAND" -> {
                        requestOptions = requestOptions
                            .transform(RotateTransform(90f))
                            .skipMemoryCache(false)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    }

                    "FOLD_DISPLAY_COLLAPSE" -> {
                        requestOptions = requestOptions
                            .transform(CenterCrop(), RoundedCorners(40))
                            .transform(RotateTransform(90f))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                        portfolioIv.scaleType = ImageView.ScaleType.FIT_XY
                    }

                    "BASIC_DISPLAY" -> {
                        requestOptions = requestOptions
                            .transform(CenterCrop(), RoundedCorners(40))
                            .transform(RotateTransform(90f))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                    }
                }

                Glide.with(this@NewPurchaseDetailActivity)
                    .load(vo[position].portfolio?.representThumbnailImagePath)
                    .apply(requestOptions)
                    .into(roundAlIIv(portfolioIv, 40f))
                portfolioIv.clipToOutline = true

                vo[position].let { data ->

                    // 최상단 제목
                    topTitleTv.text = data.portfolio?.title.default()

                    // 조각 구매 수
                    volumeTv.text = data.purchasePieceVolume.toString()

                    // 구매 날짜
                    dateTv.text = data.purchaseAt?.toBaseDateFormat() ?: ""

                    // 청약 일자
                    purchaseDateTv.text = data.purchaseAt?.toKoreanDateFormat() ?: ""

                    // 배정 수량
                    purchaseCountTv.text = (data.purchasePieceVolume?.decimalComma() + "주")

                    // 배정 금액
                    purchaseBuyTv.text = data.purchaseTotalAmount?.toLong()?.formatAmount()



                    data.portfolioAttachFile?.let { attachVo ->
                        // 소유증서 제목
                        documentNameTv.text = attachVo.title
                    }

                    // 하단 데이터

                    // 포트폴리오 이름
                    portfolioNameTv.text = data.portfolio?.title

                    // 구성 자산
                    val productRvAdapter = DepositPurchaseAdapter(this@NewPurchaseDetailActivity)
                    productRv.run {
                        adapter = productRvAdapter
                        this.layoutManager = LinearLayoutManager(this@NewPurchaseDetailActivity, RecyclerView.VERTICAL, false)
                        itemAnimator = null

                        productRvAdapter.submitList(data.portfolio?.products)
                    }

                    // 공모 총액
                    totalOfferingAmountTv.text = data.portfolio?.recruitmentAmount?.toLong()?.formatAmount()

                    // 청약 가능 금액
                    availableAmountTv.text =
                        "최소 " + ConvertMoney().getNumKorString(data.portfolio?.minPurchaseAmount?.toLong().default()) + "만 원 ~ " +
                                "최대 " + ConvertMoney().getNumKorString(data.portfolio?.maxPurchaseAmount?.toLong().default()) + "만 원"

                    // 청약 가능 조각 수
                    availableAmountSubTv.text =
                        "최소 " + ConvertMoney().getNumKorString(data.portfolio?.minPurchaseAmount?.toLong().default()) + "주 ~ " +
                                "최대 " + ConvertMoney().getNumKorString(data.portfolio?.maxPurchaseAmount?.toLong().default()) + "주"

                    // 공모가액
                    publicOfferingPriceTv.text = data.portfolio?.minPurchaseAmount?.toDecimalComma() + "원(1주당)"

                    // 만기일
                    expirationDateTv.text = data.portfolio?.dividendsExpectationDate?.toKoreanDateFormat()


                    // 소유권 증명서 OnClick
                    documentLayout.onThrottleClick {
                        startActivity(
                            BasePdfActivity.getDeedIntent(
                                this@NewPurchaseDetailActivity,
                                "NewPurchaseDetailActivity",
                                data.portfolioAttachFile?.attachFilePath.default(),
                                data.portfolioAttachFile?.codeName.default(),
                                data.purchaseId.default()
                            )
                        )
                    }

                    backLayout.onThrottleClick {
                        BackPressedUtil().activityFinish(this@NewPurchaseDetailActivity,this@NewPurchaseDetailActivity)
                    }
                }
            }
        }
    }

    private fun getDisplayType(displayMetrics: DisplayMetrics): String {
        return when {
            displayMetrics.widthPixels > 1600 -> {
                "FOLD_DISPLAY_EXPAND"
            }

            displayMetrics.widthPixels < 980 -> {
                "FOLD_DISPLAY_COLLAPSE"
            }

            else -> {
                "BASIC_DISPLAY"
            }
        }
    }

    class RotateTransform(rotateRotationAngle: Float) : BitmapTransformation() {
        private var rotateRotationAngle = 0f

        init {
            this.rotateRotationAngle = rotateRotationAngle
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(("rotate$rotateRotationAngle").toByte());
        }

        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(rotateRotationAngle)
            return Bitmap.createBitmap(
                toTransform,
                0,
                0,
                toTransform.width,
                toTransform.height,
                matrix,
                true
            )
        }
    }


    fun roundAlIIv(iv: ImageView, curveRadius: Float): ImageView {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            iv.outlineProvider = object : ViewOutlineProvider() {

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view!!.width, view.height, curveRadius)
                }
            }

            iv.clipToOutline = true
        }
        return iv
    }

    fun Long.formatAmount(): String {
        val billion = 100000000
        val price = 10000

        return when {
            this >= billion -> {
                val billionPart = this / billion
                val remainder = this % billion
                val tenThousandPart = remainder / price
                if (tenThousandPart == 0L) {
                    "${billionPart}억 원"
                } else {
                    "${billionPart}억 ${tenThousandPart}만 원"
                }
            }

            this >= price -> "${this / price}만 원"
            else -> "${this}원"
        }
    }


    companion object {
        fun getNetworkActivity(context: Context): Intent {
            return Intent(context, NetworkActivity::class.java)
        }

        // 소유증서
        fun getDeedIntent(
            context: Context,
            pdfUrl: String,
            title: String,
            purchaseId: String
        ): Intent {
            val intent = Intent(context, BasePdfActivity::class.java)
            intent.putExtra("pdfUrl", pdfUrl)
            intent.putExtra("purchaseId", purchaseId)
            intent.putExtra("title", title)
            return intent
        }
    }
}
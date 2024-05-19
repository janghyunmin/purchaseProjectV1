package run.piece.dev.refactoring.ui.portfolio.detail.assets.detailasset.objectinfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint
import run.piece.dev.R
import run.piece.dev.databinding.ActivityPortfolioObjectInfoBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.refactoring.utils.onThrottleClick


@AndroidEntryPoint
class PortfolioObjectInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPortfolioObjectInfoBinding
    private val viewModel: PortfolioObjectInfoViewModel by viewModels()

    private val colorList = intArrayOf(R.color.pie_chart_color_A591FF, R.color.pie_chart_color_49A9FF, R.color.pie_chart_color_B8BCC8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortfolioObjectInfoBinding.inflate(layoutInflater)
        binding.activity = this
        binding.viewModel = viewModel
        setContentView(binding.root)

        val objectInfoChartRvAdapter = ObjectInfoChartRvAdapter(this)
        val objectInfoProductRvAdapter = ObjectInfoProductRvAdapter(this)

        val test = arrayListOf(
            ObjectInfoItem(false, R.color.pie_chart_color_A591FF, "암소", 26F, R.drawable.test_cow_female_gray, "일련번호 002 1674 1234 1", "한우 • 암소 • 13개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_49A9FF, "숫소", 4F, R.drawable.test_cow_female_blue, "일련번호 002 3465 1415 5", "한우 • 암소 • 25개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_B8BCC8, "거세", 70F, R.drawable.test_cow_female_purple, "일련번호 002 1574 2864 8", "한우 • 암소 • 63개월"),
        )

        val test2 = arrayListOf(
            ObjectInfoItem(false, R.color.pie_chart_color_A591FF, "암소", 26F, R.drawable.test_cow_female_purple, "일련번호 502 1674 624 5", "한우 • 암소 • 16개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_49A9FF, "숫소", 4F, R.drawable.test_cow_female_gray, "일련번호 002 174 64 6", "한우 • 암소 • 26개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_B8BCC8, "거세", 70F, R.drawable.test_cow_female_blue, "일련번호 003 1574 864 6", "한우 • 암소 • 34개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_B8BCC8, "숫소", 70F, R.drawable.test_cow_female_purple, "일련번호 012 574 68 6", "한우 • 암소 • 3개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_49A9FF, "거세", 70F, R.drawable.test_cow_female_purple, "일련번호 052 2674 6864 2", "한우 • 암소 • 11개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_A591FF, "숫소", 70F, R.drawable.test_cow_female_gray, "일련번호 022 1674 986 6", "한우 • 암소 • 44개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_B8BCC8, "숫소", 70F, R.drawable.test_cow_female_blue, "일련번호 012 6674 6864 4", "한우 • 암소 • 52개월"),
            ObjectInfoItem(false, R.color.pie_chart_color_49A9FF, "암소", 70F, R.drawable.test_cow_female_purple, "일련번호 002 5674 6860 8", "한우 • 암소 • 31개월"),
        )

        binding.apply {
            chartRv.apply {
                layoutManager = LinearLayoutManager(this@PortfolioObjectInfoActivity, RecyclerView.VERTICAL, false)
                adapter = objectInfoChartRvAdapter
            }

            infoRv.apply {
                layoutManager = LinearLayoutManager(this@PortfolioObjectInfoActivity, RecyclerView.VERTICAL, false)
                adapter = objectInfoProductRvAdapter
            }

            val chartList = ArrayList<PieEntry>()

            test.forEachIndexed { index, objectInfoItem ->
                chartList.add(PieEntry(objectInfoItem.percent))
            }

            objectInfoChartRvAdapter.submitList(test)
            objectInfoProductRvAdapter.submitList(test2)

            pieChart.holeRadius = 28F
            pieChart.setTransparentCircleAlpha(0)
            pieChart.setUsePercentValues(true)
            pieChart.legend.isEnabled = false
            pieChart.description.isEnabled = false
            pieChart.animateY(1400, Easing.EaseInExpo)

            val dataSet = PieDataSet(chartList, "")
            dataSet.setDrawValues(false)
            dataSet.sliceSpace = 0f
            dataSet.selectionShift = 2f
            dataSet.setColors(colorList, this@PortfolioObjectInfoActivity)

            val data = PieData(dataSet)

            pieChart.data = data
        }

        binding.backLayout.onThrottleClick {
            BackPressedUtil().activityFinish(this@PortfolioObjectInfoActivity,this@PortfolioObjectInfoActivity)
        }

        BackPressedUtil().activityCreate(this@PortfolioObjectInfoActivity,this@PortfolioObjectInfoActivity)
        BackPressedUtil().systemBackPressed(this@PortfolioObjectInfoActivity,this@PortfolioObjectInfoActivity)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, PortfolioObjectInfoActivity::class.java)
            return intent
        }
    }
}
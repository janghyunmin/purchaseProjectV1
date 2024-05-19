package run.piece.dev.refactoring.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import com.android.tools.build.jetifier.core.utils.Log
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import run.piece.dev.App
import run.piece.dev.databinding.ActivityMapBinding
import run.piece.dev.refactoring.utils.BackPressedUtil
import run.piece.dev.view.common.NetworkActivity

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding

    val default = LatLng(37.5589788, 126.8290684)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        App()

        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        binding.activity = this
        binding.lifecycleOwner = this

        binding.apply {
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this@MapActivity)

            intent?.getStringExtra("address")?.let { address ->
                addressTv.text = address
            }
        }

        BackPressedUtil().activityCreate(this@MapActivity,this@MapActivity)
        BackPressedUtil().systemBackPressed(this@MapActivity,this@MapActivity)
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

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        intent?.getStringExtra("addressUrl")?.let {
            if (!it.contains("http")) {
                val latLng = addressRedefine(it)
                naverMap.moveCamera(CameraUpdate.scrollTo(latLng))

                val marker = Marker()
                marker.position = latLng
                marker.map = naverMap
            }
            else {
                val intent = Intent(applicationContext, NetworkActivity::class.java)
                startActivity(intent)
                finish()
            }
        } ?: run {
            naverMap.moveCamera(CameraUpdate.scrollTo(default))

            val marker = Marker()
            marker.position = default
            marker.map = naverMap
        }

        naverMap.moveCamera(CameraUpdate.zoomTo(17.0))

        val uiSettings = naverMap.uiSettings

        uiSettings.isZoomControlEnabled = false

    }

    private fun addressRedefine(address: String): LatLng {
        var lat: Double = 0.0 //위도
        var lng: Double = 0.0 //경도

        val data = address.replace("\"|=".toRegex(), "") // " 와 = 제거
        val array = data.split(",") // ,로 문자를 나누고

        for (item: String in array) { //x, y를 확인 및 대입 후 x, y를 지운다
            if (item.contains("x")) {
                lng = item.replace("x".toRegex(), "").toDouble()
            } else if (item.contains("y")) {
                lat = item.replace("y".toRegex(), "").toDouble()
            }
        }

        Log.e("위경도", "$lat , $lng")

        return LatLng(lat, lng)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            val intent = Intent(context, MapActivity::class.java)
            return intent
        }

        fun getIntent(context: Context, documentId: String, addressUrl: String, address: String): Intent {
            val intent = Intent(context, MapActivity::class.java)
            intent.putExtra("documentId", documentId)
            intent.putExtra("addressUrl", addressUrl)
            intent.putExtra("address", address)
            return intent
        }
    }
}
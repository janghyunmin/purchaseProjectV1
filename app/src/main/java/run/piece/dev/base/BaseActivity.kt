package run.piece.dev.base

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import io.reactivex.disposables.CompositeDisposable

/**
 *packageName    : com.bsstandard.piece.base
 * fileName       : BaseActivity
 * author         : piecejhm
 * date           : 2022/04/27
 * description    : BaseActivity 공통 모듈
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/04/27        piecejhm       최초 생성
 */


//BaseActivity.kt
abstract class BaseActivity<T : ViewDataBinding>(
    @LayoutRes private val layoutResId: Int
) : AppCompatActivity() {
    lateinit var binding: T
    private val compositeDisposable = CompositeDisposable()
    private var waitTime = 0L



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, layoutResId)
        binding.lifecycleOwner = this

    }

    // 공통 Toast Short 버전 호출 함수 - jhm 2023/03/20
    protected fun shortShowToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // 공통 Toast Long 버전 호출 함수 - jhm 2023/03/20
    protected fun longShowToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()


    // 공통 화면 Destroy 제어 함수 - jhm 2023/03/20
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
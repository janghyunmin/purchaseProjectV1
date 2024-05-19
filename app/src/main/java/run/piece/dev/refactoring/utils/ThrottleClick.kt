package run.piece.dev.refactoring.utils

import android.os.SystemClock
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class OnThrottleClickListener(
    private val onClickListener: View.OnClickListener,
    private val interval: Long = 300L
) : View.OnClickListener {
    private var clickable = true

    override fun onClick(v: View?) {
        if (clickable) {
            clickable = false
            v?.run {
                postDelayed({
                    clickable = true
                }, interval)
                onClickListener.onClick(v)
            }
        } else {
            Log.i("OnThrottleClickListener_onClick", "waiting for a while")
        }
    }
}

fun View.onThrottleClick(action: (v: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener))
}

fun View.onThrottleClick(action: (v: View) -> Unit, interval: Long) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnThrottleClickListener(listener, interval))
}




// 클릭 이벤트를 flow 로 변환
fun View.clicks(): Flow<Unit> = callbackFlow {
    setOnClickListener {
        this.trySend(Unit)
    }
    awaitClose { setOnClickListener(null) }
}

// 마지막 발행 시간과 현재 시간 비교해서 이벤트 발행, 나머지는 무시.
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var lastEmissionTime = 0L
    collect { upstream ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastEmissionTime > windowDuration) {
            lastEmissionTime = currentTime
            emit(upstream)
        }
    }
}

// 입,출금 및 계좌와 돈과 관련된 ClickEvent는 해당 Click Event로 사용
fun View.setClickEvent(
    uiScope: CoroutineScope,
    windowDuration: Long = 10000L,
    onClick: () -> Unit,
) {
    clicks()
        .throttleFirst(windowDuration)
        .onEach { onClick.invoke() }
        .launchIn(uiScope)
}


// 다중 클릭 제어 클릭 이벤트
var isClicked = false
fun View.onThrottleFirstClick(interval: Long = 600, action: (v: View) -> Unit) {
    setOnClickListener { v ->
        if (isClicked.not()) {
            isClicked = true
            v?.run {
                postDelayed({
                    isClicked = false
                }, interval)
                action(v)
            }
        }
    }
}




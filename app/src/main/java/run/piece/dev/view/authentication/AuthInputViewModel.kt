package run.piece.dev.view.authentication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

/**
 *packageName    : com.bsstandard.piece.view.authentication
 * fileName       : AuthInputViewModel
 * author         : piecejhm
 * date           : 2022/10/26
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2022/10/26        piecejhm       최초 생성
 */

class AuthInputViewModel(application: Application) :
    AndroidViewModel(application) {

    var userFirst = MutableLiveData<String>()
    fun getFirst(): MutableLiveData<String> {
        return userFirst
    }

    var userLast = MutableLiveData<String>()
    fun getLast(): MutableLiveData<String> {
        return userLast
    }

    open fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

}
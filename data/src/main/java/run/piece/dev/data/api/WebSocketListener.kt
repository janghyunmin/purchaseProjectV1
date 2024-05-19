package run.piece.dev.data.api
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

open class WebSocketListener : WebSocketListener() {

    private val _liveData = MutableLiveData<JsonObject>()
    val liveData: LiveData<JsonObject> get() = _liveData

    private fun outputData(string: String) {
        val resp: JsonObject = JsonParser().parse(string).asJsonObject
        var jsonObject = JsonObject()
        jsonObject.add("data", resp)

        _liveData.postValue(resp)
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("")
//        webSocket.close(NORMAL_CLOSURE_STATUS, "Socket Close!!") //없을 경우 끊임없이 서버와 통신함
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        outputData(text)
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        outputData(bytes.hex())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        outputData("$code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("Socket Error : ","${t.message}")
    }

    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
    }
}
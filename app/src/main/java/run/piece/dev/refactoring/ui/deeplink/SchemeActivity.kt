package run.piece.dev.refactoring.ui.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import com.android.tools.build.jetifier.core.utils.Log
import run.piece.dev.refactoring.ui.main.MainActivity

class SchemeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink()
    }

    private fun handleDeepLink() {
        val deepLinkUri = intent.data

        Log.e("deepLinkUri", deepLinkUri.toString())

        val deepLinkIntent = deepLinkUri?.let {
            DeepLinkInfo(deepLinkUri).getIntent(this, it)
        } ?: DeepLinkInfo.getSplash(this)

        if (isTaskRoot) {
            TaskStackBuilder.create(this).apply {
                if (needAddMainForParent(deepLinkIntent)) {
                    addNextIntentWithParentStack(DeepLinkInfo.getSplash(this@SchemeActivity))
                }
                addNextIntent(deepLinkIntent)
            }.startActivities()

        } else startActivity(deepLinkIntent)

        finish()
    }

    private fun needAddMainForParent(intent: Intent): Boolean =
        when (intent.component?.className) {
            MainActivity::class.java.name -> false
            else -> true
        }
}
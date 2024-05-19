package run.piece.dev.refactoring.ui.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import com.android.tools.build.jetifier.core.utils.Log
import run.piece.dev.App
import run.piece.dev.R
import run.piece.dev.refactoring.base.html.BaseWebViewActivity
import run.piece.dev.refactoring.ui.alarm.AlarmActivity
import run.piece.dev.refactoring.ui.event.EventActivity
import run.piece.dev.refactoring.ui.magazine.NewMagazineDetailWebViewActivity
import run.piece.dev.refactoring.ui.main.MainActivity
import run.piece.dev.refactoring.ui.notice.NoticeActivity
import run.piece.dev.refactoring.ui.portfolio.detail.PortfolioDetailNewActivity
import run.piece.dev.refactoring.ui.splash.SplashActivity

enum class DeepLinkInfo(@StringRes val hostStringResId: Int) {
    /* url deep link */
    SPLASH(R.string.scheme_host_splash) {
        override fun getIntent(context: Context, deepLinkUri: Uri) = getSplash(context)
    },
    PORTFOLIO(R.string.scheme_host_portfolio) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getPortfolio(context, deepLinkUri)
    },
    MAGAZINE_POST(R.string.scheme_host_magazine_post) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getMagazine(context, deepLinkUri)
    },
    NOTICE(R.string.scheme_host_notice) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getNotice(context, deepLinkUri)
    },
    EVENT(R.string.scheme_host_event) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getEvent(context, deepLinkUri)
    },
    ALARM(R.string.scheme_host_alarm) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = Intent(context, AlarmActivity::class.java)
    },

    HOME(R.string.scheme_host_home) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getHome(context)
    },
    MAGAZINE(R.string.scheme_host_magazine) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getMagazine(context)
    },
    WALLET(R.string.scheme_host_wallet) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getWallet(context)
    },
    MORE(R.string.scheme_host_more) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getMore(context)
    },

    /* push link deep link */
    PLC0101(R.string.scheme_host_plc0101) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent {
            val splits = "$deepLinkUri".split("/")
            val magazineId = splits[splits.size - 1]

            return if (magazineId.isEmpty()) getMagazine(context)
            else getMagazine(context, deepLinkUri)
        }
    },
    PLC0102(R.string.scheme_host_plc0102) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent {
            //piece://portfolio/db87677d-fd25-43e8-b907-1632a36b69a1
            //piece://PLC0102/db87677d-fd25-43e8-b907-1632a36b69a1
            val splits = "$deepLinkUri".split("/")
            val portfolioId = splits[splits.size -1]

            return if (portfolioId.isEmpty()) getSplash(context)
            else getPortfolio(context, deepLinkUri)
        }
    },
    PLC0103(R.string.scheme_host_plc0103) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent {
            //piece://event/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            //piece://PLC0103/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            val splits = "$deepLinkUri".split("/")
            val eventId = splits[splits.size -1]

            return if (eventId.isEmpty()) getEvent(context)
            else getEvent(context, deepLinkUri)
        }
    },
    PLC0104(R.string.scheme_host_plc0104) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent {
            //piece://notice/98a29261-9d2f-415f-bb75-347c8a26f8aa
            //piece://PLC0104/98a29261-9d2f-415f-bb75-347c8a26f8aa
            val splits = "$deepLinkUri".split("/")
            val boardId = splits[splits.size -1]

            return if (boardId.isEmpty()) getNotice(context)
            else getNotice(context, deepLinkUri)
        }
    },
    PLC0105(R.string.scheme_host_plc0105) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getHome(context)
    },
    PLC0106(R.string.scheme_host_plc0106) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getWallet(context)
    },
    PLC0107(R.string.scheme_host_plc0107) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getMore(context)
    },

    /*PLC0108(R.string.scheme_host_plc0108) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getPDF(context, deepLinkUri)
    },
    PLC0109(R.string.scheme_host_plc0109) {
        override fun getIntent(context: Context, deepLinkUri: Uri): Intent = getWeb(context, deepLinkUri)
    }*/;

    private val host: String = App.getInstance().getString(hostStringResId)

    abstract fun getIntent(context: Context, deepLinkUri: Uri): Intent

    companion object {
        fun getSplash(context: Context) = SplashActivity.getIntent(context)

        fun getPortfolio(context: Context, deepLinkUri: Uri): Intent {
            //piece://portfolio/db87677d-fd25-43e8-b907-1632a36b69a1
            //piece://PLC0102/db87677d-fd25-43e8-b907-1632a36b69a1
            val splits = "$deepLinkUri".split("/")
            val portfolioId = splits[splits.size -1]

            return PortfolioDetailNewActivity.getIntent(context, portfolioId)
        }

        fun getMagazine(context: Context, deepLinkUri: Uri): Intent {
            //piece://magazinePost/a30f5ee5-80a8-4f59-9eb0-534b538cadb2
            //piece://PLC0101/a30f5ee5-80a8-4f59-9eb0-534b538cadb2
            val splits = "$deepLinkUri".split("/")
            val magazineId = splits[splits.size -1]
            val intent = Intent(context, NewMagazineDetailWebViewActivity::class.java)
            intent.putExtra("magazineId", magazineId)
            return intent
        }

        fun getNotice(context: Context, deepLinkUri: Uri): Intent {
            //piece://notice/98a29261-9d2f-415f-bb75-347c8a26f8aa
            //piece://PLC0104/98a29261-9d2f-415f-bb75-347c8a26f8aa
            val splits = "$deepLinkUri".split("/")
            val boardId = splits[splits.size -1]
            val intent = BaseWebViewActivity.getNoticeDetail(context, "공지사항 상세","공지사항", boardId)
            intent.putExtra("boardId", boardId)
            return intent
        }

        fun getNotice(context: Context): Intent = Intent(context, NoticeActivity::class.java)

        fun getEvent(context: Context, deepLinkUri: Uri): Intent {
            //piece://event/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            //piece://PLC0103/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            val splits = "$deepLinkUri".split("/")
            val eventId = splits[splits.size -1]
            val intent = BaseWebViewActivity.getEventDetail(context,"이벤트 상세", eventId)
            intent.putExtra("viewName","이벤트 상세")
            intent.putExtra("eventId", eventId)
            return intent
        }

        /*fun getPDF(context: Context, deepLinkUri: Uri): Intent {
            //piece://pdf/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            //piece://PLC0108/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            val splits = "$deepLinkUri".split("/")
            val pdfUrl = splits[splits.size -1]
            return Intent()
        }

        fun getWeb(context: Context, deepLinkUri: Uri): Intent {
            //piece://web/9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            //piece://PLC0109//9c14180a-ae3e-11ec-ae3c-f220af1d46ae
            val splits = "$deepLinkUri".split("/")
            val webUrl = splits[splits.size -1]
            return Intent()
        }*/

        fun getEvent(context: Context): Intent = Intent(context, EventActivity::class.java)

        fun getHome(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("deepLink", "home")
            return intent
        }

        fun getMagazine(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("deepLink", "magazine")
            return intent
        }


        fun getWallet(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("deepLink", "wallet")
            return intent
        }


        fun getMore(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("deepLink", "more")
            return intent
        }

        operator fun invoke(uri: Uri): DeepLinkInfo =
            values().find {
                //등록된 딥링크
                Log.e("딥링크 테스트 ok", it.host)
                it.host == uri.host
            } ?: run {
                Log.e("딥링크 테스트 fail", "등록되지 않음 SPLASH 이동")
                SPLASH
            }
    }
}
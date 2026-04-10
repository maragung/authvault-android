package auth.vault.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import auth.vault.R
import auth.vault.data.local.database.VaultDatabase
import auth.vault.domain.usecase.TotpGenerator
import auth.vault.util.TimeSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TokenWidgetProvider : AppWidgetProvider() {

    private val totpGenerator = TotpGenerator()
    private val timeSource = TimeSource()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        runCatching {
            val views = RemoteViews(context.packageName, R.layout.widget_token)

            val database = VaultDatabase.getInstance(context, ByteArray(32))
            val tokens = runBlocking { database.tokenDao().getAllTokens().first() }

            if (tokens.isNotEmpty()) {
                val adjustedTime = timeSource.currentSeconds()
                val firstToken = tokens.first()
                val code = totpGenerator.generateCode(
                    firstToken.secretKey,
                    adjustedTime,
                    firstToken.digitCount,
                    firstToken.algorithm
                )
                val remaining = totpGenerator.getRemainingSeconds(adjustedTime, firstToken.timeStep)

                views.setTextViewText(R.id.widget_title, firstToken.serviceLabel)
                views.setTextViewText(R.id.widget_code, code)
                views.setTextViewText(R.id.widget_timer, "${remaining}s")
            }

            val intent = Intent(context, auth.vault.ui.main.VaultActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_code, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "android.appwidget.action.APPWIDGET_UPDATE") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TokenWidgetProvider::class.java)
            appWidgetManager.getAppWidgetIds(componentName)?.forEach { appWidgetId ->
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}

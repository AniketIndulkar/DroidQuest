package dev.novanest.droidquest.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import dev.novanest.droidquest.DroidQuestApplication
import dev.novanest.droidquest.MainActivity
import dev.novanest.droidquest.R
import dev.novanest.droidquest.content.ContentLoadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LearningBiteWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val items = loadItems(context)
                val store = LearningBiteWidgetStore(context)
                appWidgetIds.forEach { appWidgetId ->
                    val item = items?.let { store.selectNext(appWidgetId, it, System.currentTimeMillis()) }
                    manager.updateAppWidget(appWidgetId, render(context, appWidgetId, item, store))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action != ACTION_NEXT && intent.action != ACTION_ANSWER) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return@launch
                val items = loadItems(context)
                val store = LearningBiteWidgetStore(context)
                val item = when (intent.action) {
                    ACTION_NEXT -> items?.let { store.selectNext(appWidgetId, it, System.currentTimeMillis()) }
                    ACTION_ANSWER -> {
                        val itemId = intent.getStringExtra(EXTRA_ITEM_ID)
                        val question = items?.filterIsInstance<LearningBite.Question>()?.firstOrNull { it.id == itemId }
                        val recorded = question != null && store.recordAnswer(
                            appWidgetId = appWidgetId,
                            question = question,
                            selectedIndex = intent.getIntExtra(EXTRA_OPTION_INDEX, -1),
                            nowEpochMillis = System.currentTimeMillis(),
                        )
                        if (recorded) question else items?.firstOrNull { it.id == store.currentItemId(appWidgetId) }
                    }
                    else -> null
                }
                AppWidgetManager.getInstance(context).updateAppWidget(
                    appWidgetId,
                    render(context, appWidgetId, item, store),
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val store = LearningBiteWidgetStore(context)
        appWidgetIds.forEach(store::deleteWidget)
    }

    private fun render(
        context: Context,
        appWidgetId: Int,
        item: LearningBite?,
        store: LearningBiteWidgetStore,
    ): RemoteViews = RemoteViews(context.packageName, R.layout.learning_bite_widget).apply {
        setOnClickPendingIntent(R.id.widget_header, openAppIntent(context))
        setOnClickPendingIntent(R.id.widget_next, actionIntent(context, appWidgetId, ACTION_NEXT))

        if (item == null) {
            showEmptyState(context, appWidgetId)
            return@apply
        }

        setTextViewText(R.id.widget_topic, item.topicTitle)
        setViewVisibility(R.id.widget_result, View.GONE)
        setViewVisibility(R.id.widget_explanation, View.GONE)
        OPTION_IDS.forEach { setViewVisibility(it, View.GONE) }

        when (item) {
            is LearningBite.Fact -> {
                setTextViewText(R.id.widget_kind, context.getString(R.string.widget_quick_fact))
                setTextViewText(R.id.widget_prompt, item.text)
                setTextViewText(R.id.widget_supporting, context.getString(R.string.widget_recall_cue, item.recallCue))
                setViewVisibility(R.id.widget_supporting, View.VISIBLE)
                setTextViewText(R.id.widget_next, context.getString(R.string.widget_next_bite))
            }
            is LearningBite.Question -> {
                setTextViewText(R.id.widget_kind, context.getString(R.string.widget_quick_check))
                setTextViewText(R.id.widget_prompt, item.prompt)
                setViewVisibility(R.id.widget_supporting, View.GONE)
                val answeredIndex = store.answerIndex(appWidgetId)
                if (answeredIndex == LearningBiteWidgetStore.NO_ANSWER) {
                    item.options.forEachIndexed { index, option ->
                        val viewId = OPTION_IDS[index]
                        setTextViewText(viewId, option)
                        setViewVisibility(viewId, View.VISIBLE)
                        setOnClickPendingIntent(viewId, answerIntent(context, appWidgetId, item.id, index))
                    }
                    setTextViewText(R.id.widget_next, context.getString(R.string.widget_skip))
                } else {
                    val correct = answeredIndex == item.correctOptionIndex
                    setTextViewText(
                        R.id.widget_result,
                        context.getString(
                            if (correct) R.string.widget_correct else R.string.widget_not_quite,
                            item.correctOption,
                        ),
                    )
                    setTextViewText(R.id.widget_explanation, item.explanation)
                    setViewVisibility(R.id.widget_result, View.VISIBLE)
                    setViewVisibility(R.id.widget_explanation, View.VISIBLE)
                    setTextViewText(R.id.widget_next, context.getString(R.string.widget_next_bite))
                }
            }
        }
    }

    private fun RemoteViews.showEmptyState(context: Context, appWidgetId: Int) {
        setTextViewText(R.id.widget_kind, context.getString(R.string.widget_unavailable_title))
        setTextViewText(R.id.widget_topic, context.getString(R.string.app_name))
        setTextViewText(R.id.widget_prompt, context.getString(R.string.widget_unavailable_body))
        setViewVisibility(R.id.widget_supporting, View.GONE)
        setViewVisibility(R.id.widget_result, View.GONE)
        setViewVisibility(R.id.widget_explanation, View.GONE)
        OPTION_IDS.forEach { setViewVisibility(it, View.GONE) }
        setTextViewText(R.id.widget_next, context.getString(R.string.widget_retry))
        setOnClickPendingIntent(R.id.widget_next, actionIntent(context, appWidgetId, ACTION_NEXT))
    }

    private suspend fun loadItems(context: Context): List<LearningBite>? {
        cachedItems?.let { return it }
        val application = context.applicationContext as DroidQuestApplication
        val state = application.container.contentRepository.load()
        val loaded = (state as? ContentLoadState.Success)?.content ?: return null
        return LearningBiteCatalog.from(loaded).takeIf(List<LearningBite>::isNotEmpty)?.also { cachedItems = it }
    }

    private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun actionIntent(context: Context, appWidgetId: Int, action: String): PendingIntent {
        val intent = Intent(context, LearningBiteWidgetProvider::class.java).apply {
            this.action = action
            data = "droidquest://widget/$appWidgetId/$action".toUri()
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun answerIntent(context: Context, appWidgetId: Int, itemId: String, optionIndex: Int): PendingIntent {
        val intent = Intent(context, LearningBiteWidgetProvider::class.java).apply {
            action = ACTION_ANSWER
            data = "droidquest://widget/$appWidgetId/answer/$optionIndex".toUri()
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_OPTION_INDEX, optionIndex)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId * 10 + optionIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val ACTION_NEXT = "dev.novanest.droidquest.widget.NEXT"
        const val ACTION_ANSWER = "dev.novanest.droidquest.widget.ANSWER"
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_OPTION_INDEX = "option_index"
        val OPTION_IDS = intArrayOf(R.id.widget_option_1, R.id.widget_option_2, R.id.widget_option_3, R.id.widget_option_4)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        @Volatile
        var cachedItems: List<LearningBite>? = null
    }
}

package dev.novanest.droidquest.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dev.novanest.droidquest.content.AssetContentSource
import dev.novanest.droidquest.content.DroidQuestContentRepository
import dev.novanest.droidquest.progress.DataStoreProgressRepository
import dev.novanest.droidquest.progress.ProgressRepository

private val Context.progressDataStore by preferencesDataStore(name = "droidquest_progress")

/**
 * Tiny manual dependency container — constructor injection without a DI framework.
 * Holds the singleton content and progress repositories for the app process.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val contentRepository: DroidQuestContentRepository =
        DroidQuestContentRepository(AssetContentSource(appContext))

    val progressRepository: ProgressRepository =
        DataStoreProgressRepository(appContext.progressDataStore)
}

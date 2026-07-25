package dev.novanest.droidquest

import android.app.Application
import dev.novanest.droidquest.di.AppContainer

class DroidQuestApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

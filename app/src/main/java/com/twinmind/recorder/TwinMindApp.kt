package com.twinmind.recorder

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TwinMindApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        // Case 5: check for any sessions stuck in RECORDING state from a prior crash
        RecoveryWorker.enqueue(this)
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


}

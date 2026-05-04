package com.twinmind.recorder


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

/**
 * Case 5 — Process death recovery.
 *
 * Triggered on device boot (BOOT_COMPLETED) and also called manually from
 * Application.onCreate() to handle the case where the process was killed
 * mid-recording without a reboot.
 *
 * Finds any session left in RECORDING status (meaning the service was killed
 * before it could clean up) and enqueues RecoveryWorker to finalize it.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "com.twinmind.recorder.CHECK_RECOVERY") return

        RecoveryWorker.enqueue(context)
    }
}
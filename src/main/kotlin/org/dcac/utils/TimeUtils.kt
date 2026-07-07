package org.dcac.utils

import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Utility functions used to format and display execution durations.
 */
object TimeUtils {

    fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000.0
        return String.format(Locale.US, "%.2fs", seconds)
    }

    fun startProgressTimer(
        label: String,
        isRunning: AtomicBoolean,
        startTime: Long
    ): Thread {
        val thread = Thread {
            while (isRunning.get()) {
                val elapsedMs = System.currentTimeMillis() - startTime
                print("\r$label | elapsed=${formatDuration(elapsedMs)}")
                runCatching {
                    Thread.sleep(1000)
                }
            }
        }

        thread.start()
        return thread
    }
}
package com.minhabateria.app.audio

object AppVisibility {
    @Volatile
    var resumedActivities: Int = 0
        private set

    val isForeground: Boolean get() = resumedActivities > 0

    @Synchronized fun onResumed() { resumedActivities += 1 }
    @Synchronized fun onPaused() { resumedActivities = (resumedActivities - 1).coerceAtLeast(0) }
}

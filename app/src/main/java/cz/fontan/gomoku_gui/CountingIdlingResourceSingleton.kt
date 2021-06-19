package cz.fontan.gomoku_gui

import androidx.test.espresso.idling.CountingIdlingResource

/**
 * Idling resource limited to one instance
 */
object CountingIdlingResourceSingleton {

    private const val RESOURCE = "GLOBAL"

    /**
     * Idling resource instance
     */
    val countingIdlingResource: CountingIdlingResource = CountingIdlingResource(RESOURCE)

    /**
     * Increase usage count
     */
    fun increment() {
        countingIdlingResource.increment()
    }

    /**
     * Decrease usage count
     */
    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
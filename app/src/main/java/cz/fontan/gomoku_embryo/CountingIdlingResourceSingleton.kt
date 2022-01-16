@file:Suppress("MemberVisibilityCanBePrivate")

package cz.fontan.gomoku_embryo

import androidx.test.espresso.idling.CountingIdlingResource

/**
 * Idling resource limited to one instance
 */
object CountingIdlingResourceSingleton {

    private const val RESOURCE = "GLOBAL"

    /**
     * Idling resource instance
     * do not make it private
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
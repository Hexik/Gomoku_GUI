package cz.fontan.gomoku_embryo

import android.app.Application
import android.content.Context

/**
 * Professional version registration handling
 */
object ProfiVersion {
    /**
     * Return Registration status
     */
    fun isProfi(): Boolean {
        return mStatus
    }

    /**
     * Check Status at Google Play
     */
    fun checkProfi() {
        mStatus = when (checkRegistration()) {
            EnumStatus.REGISTERED -> saveStatus(true)
            EnumStatus.NOT_REGISTERED -> saveStatus(false)
            EnumStatus.NO_CONNECTION -> loadStatus()
        }
    }

    private fun checkRegistration(): EnumStatus {
        return EnumStatus.NOT_REGISTERED
    }

    private fun saveStatus(status: Boolean): Boolean {
        return try {
            val sharedPreference =
                application.applicationContext.getSharedPreferences(
                    "GAME_STATUS",
                    Context.MODE_PRIVATE
                )
            sharedPreference.edit().putBoolean("Registered", status).apply()
            status
        } catch (e: Throwable) {
            false
        }
    }

    private fun loadStatus(): Boolean {
        return try {
            val sharedPreference =
                application.applicationContext.getSharedPreferences(
                    "GAME_STATUS",
                    Context.MODE_PRIVATE
                )
            sharedPreference.getBoolean("Registered", false)
        } catch (e: Throwable) {
            false
        }
    }

    /**
     *  placeholder for App
     */
    lateinit var application: Application
    private var mStatus = false

    private enum class EnumStatus { REGISTERED, NOT_REGISTERED, NO_CONNECTION }
}
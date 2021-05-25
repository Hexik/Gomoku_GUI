package cz.fontan.gomoku_gui

import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class NativeLibrary {
    @Before
    fun init() {
        try {
            System.loadLibrary("native-lib")
        } catch (e: UnsatisfiedLinkError) {
            // log the error or track it in analytics
        }
    }

    @After
    fun teardown() {
        NativeInterface.stopBrain()
    }

    @Test
    fun empty() {
        NativeInterface.startBrain(BOARD_SIZE)
        assert(true)
    }

    @Test
    fun about() {
        NativeInterface.startBrain(BOARD_SIZE)
        NativeInterface.writeToBrain("about")
        assert(NativeInterface.readFromBrain(10) == "ABOUT BRAIN")
    }

}
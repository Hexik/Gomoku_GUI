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
        assert(NativeInterface.readFromBrain(10) == "Generic Engine")
    }

    @Test
    fun start_ok() {
        NativeInterface.startBrain(BOARD_SIZE)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
    }

    @Test
    fun start_error() {
        NativeInterface.startBrain(BOARD_SIZE)
        NativeInterface.writeToBrain("start 33")
        assert(NativeInterface.readFromBrain(10).startsWith("ERROR"))
    }

    @Test
    fun begin() {
        NativeInterface.startBrain(BOARD_SIZE)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
        NativeInterface.writeToBrain("begin")
        assert(NativeInterface.readFromBrain(10).contains(","))
    }

    @Test
    fun board() {
        NativeInterface.startBrain(BOARD_SIZE)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
        NativeInterface.writeToBrain("info TIMEOUT_TURN 200\ninfo TIME_left 20000\nboard\n1,1,1\n2,2,2\ndone")
        assert(NativeInterface.readFromBrain(10).contains(","))
    }

}
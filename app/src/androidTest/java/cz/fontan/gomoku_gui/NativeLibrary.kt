package cz.fontan.gomoku_gui

import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.fontan.gomoku_gui.game.BOARD_SIZE_MAX
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
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        assert(true)
    }

    @Test
    fun about() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("about")
        assert(NativeInterface.readFromBrain(10).contains("Generic Engine"))
    }

    @Test
    fun start_ok() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
    }

    @Test
    fun start_error() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("start 33")
        assert(NativeInterface.readFromBrain(10).startsWith("ERROR"))
    }

    @Test
    fun result() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
        NativeInterface.writeToBrain("yxresult")
        assert(NativeInterface.readFromBrain(10) == "MESSAGE RESULT NONE")
    }

    @Test
    fun begin() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
        NativeInterface.writeToBrain("begin")
        assert(NativeInterface.readFromBrain(10).contains(","))
    }

    @Test
    fun board() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(10) == "OK")
        NativeInterface.writeToBrain(
            """
            |info TIMEOUT_TURN 200
            |info TIME_left 20000
            |board
            |1,1,1
            |2,2,2
            |done
            |end""".trimMargin()
        )
        assert(NativeInterface.readFromBrain(10).contains(","))
    }

    @Test
    fun single_existing_response() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
        NativeInterface.writeToBrain("start 5")
        assert(NativeInterface.readFromBrain(10) == "OK")
        NativeInterface.writeToBrain(
            """
            |info TIMEOUT_TURN 200
            |info TIME_left 20000
            |board
            |0,0,1
            |0,1,2
            |0,2,1
            |0,3,2
            |0,4,1
            |1,0,1
            |1,1,2
            |1,2,1
            |1,3,2
            |1,4,1
            |2,0,2
            |2,1,1
            |2,2,2
            |2,3,1
            |2,4,2
            |3,0,2
            |3,1,1
            |3,2,2
            |3,3,1
            |3,4,2
            |4,0,1
            |4,1,2
            |4,2,1
            |4,3,2
            |done""".trimMargin()
        )
        assert(NativeInterface.readFromBrain(10) == "4,4")
        NativeInterface.writeToBrain("yxresult")
        assert(NativeInterface.readFromBrain(10) == "MESSAGE RESULT DRAW")
    }

}
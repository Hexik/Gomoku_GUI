package cz.fontan.gomoku_gui

import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.fontan.gomoku_gui.game.BOARD_SIZE_MAX
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class NativeLibraryTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun oneTimeSetup() {
            try {
                System.loadLibrary("native-lib")
            } catch (e: UnsatisfiedLinkError) {
                // log the error or track it in analytics
            }
        }
    }

    @Before
    fun runBeforeEveryTest() {
        NativeInterface.startBrain(BOARD_SIZE_MAX)
    }

    @After
    fun cleanAfterEveryTest() {
        NativeInterface.stopBrain()
    }

    @Test
    fun empty() {
        assert(true)
    }

    @Test
    fun about() {
        NativeInterface.writeToBrain("about")
        assert(NativeInterface.readFromBrain(1000).contains("Mira Fontan"))
    }

    @Test
    fun start_ok() {
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(1000) == "OK")
    }

    @Test
    fun start_error() {
        NativeInterface.writeToBrain("start 33")
        assert(NativeInterface.readFromBrain(1000).startsWith("ERROR"))
    }

    @Test
    fun result() {
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(1000) == "OK")
        NativeInterface.writeToBrain("yxresult")
        assert(NativeInterface.readFromBrain(1000) == "MESSAGE RESULT NONE")
    }

    @Test
    fun begin() {
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(1000) == "OK")
        NativeInterface.writeToBrain("begin")
        assert(NativeInterface.readFromBrain(1000).contains(","))
    }

    @Test
    fun board() {
        NativeInterface.writeToBrain("start 15")
        assert(NativeInterface.readFromBrain(1000) == "OK")
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
        assert(NativeInterface.readFromBrain(1000).contains(","))
    }

    @Test
    fun single_existing_response() {
        NativeInterface.writeToBrain("start 5")
        assert(NativeInterface.readFromBrain(1000) == "OK")
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
        var s: String
        do {
            s = NativeInterface.readFromBrain(1000)
        } while (s.startsWith("DEBUG") || s.startsWith("MESSAGE"))
        assert(s == "4,4")
        NativeInterface.writeToBrain("yxresult")
        assert(NativeInterface.readFromBrain(1000) == "MESSAGE RESULT DRAW")
        NativeInterface.writeToBrain("end")
    }
}
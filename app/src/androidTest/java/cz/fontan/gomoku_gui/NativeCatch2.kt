package cz.fontan.gomoku_embryo

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NativeCatch2 {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            try {
                System.loadLibrary("native-lib")
            } catch (e: UnsatisfiedLinkError) {
                // log the error or track it in analytics
            }
        }
    }

    @Test
    fun basic() {
        assert(0 == NativeInterface.runCatch2Test("Basic*"))
    }

    @Test
    fun config() {
        assert(0 == NativeInterface.runCatch2Test("Config*"))
    }

    @Test
    fun engine() {
        assert(0 == NativeInterface.runCatch2Test("Engine*"))
    }

    @Test
    fun inputQueue() {
        assert(0 == NativeInterface.runCatch2Test("InputQueue*"))
    }

    @Test
    fun ray() {
        assert(0 == NativeInterface.runCatch2Test("Ray*"))
    }

    @Test
    fun all() {
        assert(0 == NativeInterface.runCatch2Test("*"))
    }
}
package cz.fontan.gomoku_gui

/**
 * JNI interface to C++
 */
class NativeInterface {
    /**
     * A native methods that are implemented by the 'native-lib' native library,
     * Interface to the C++ world
     */
    companion object {
        external fun startBrain(dimension: Int)
        external fun stopBrain()
        external fun readFromBrain(timeoutMillis: Int): String
        external fun writeToBrain(s: String)
    }
}
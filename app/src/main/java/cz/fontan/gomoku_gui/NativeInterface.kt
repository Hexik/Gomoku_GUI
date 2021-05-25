package cz.fontan.gomoku_gui

class NativeInterface {
    /**
     * A native methods that are implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    companion object {
        external fun readFromBrain(timeoutMillis: Int): String
        external fun writeToBrain(s: String)
        external fun startBrain(dimension: Int)
        external fun stopBrain()
    }
}
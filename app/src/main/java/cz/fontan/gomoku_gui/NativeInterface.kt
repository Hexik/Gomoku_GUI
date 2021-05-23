package cz.fontan.gomoku_gui

class NativeInterface {
    /**
     * A native methods that are implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    companion object {
        external fun helloStringFromJNI(s: String): String
        external fun readStringFromJNI(): String
        external fun writeStringToJNI(s: String)
    }
}
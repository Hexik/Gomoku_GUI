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
        /**
         *  C++ engine is started
         *  @param dimension Board size
         */
        external fun startBrain(dimension: Int)

        /**
         * Stops and destroys brain
         */
        external fun stopBrain()

        /**
         * Reads responses from brain, empty string if no data
         * @param timeoutMillis read timeout, 0 works as peek()
         * @return
         */
        external fun readFromBrain(timeoutMillis: Int): String

        /**
         * Sends command(s), Gomocup protocol is used
         * @param command data for brain
         */
        external fun writeToBrain(command: String)
    }
}
package cz.fontan.gomoku_gui.model

import cz.fontan.gomoku_gui.NativeInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AnswersRepository {
    /**
     * This method is used to get data from brain in pseudo real time
     */
    fun fetchStrings(): Flow<ConsumableValue<String>> = flow {
        while (true) {
            val s = NativeInterface.readFromBrain(0)
            when (s.isEmpty()) {
                true -> delay(5)
                else -> emit(ConsumableValue(s))
            }
        }
    }
}

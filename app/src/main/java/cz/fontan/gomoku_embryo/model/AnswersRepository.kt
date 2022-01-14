package cz.fontan.gomoku_embryo.model

import cz.fontan.gomoku_embryo.NativeInterface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Read all data from C++ brain as flow
 * @property inTest longer delay in test mode, should be at least 15ms to make Espresso happy
 */
class AnswersRepository(val inTest: Boolean) {
    /**
     * This method is used to get data from brain in pseudo real time
     */
    fun fetchStrings(): Flow<ConsumableValue<String>> = flow {
        while (true) {
            val s = NativeInterface.readFromBrain(0)
            when (s.isEmpty()) {
                true -> delay(if (inTest) 50 else 5)
                else -> emit(ConsumableValue(s))
            }
        }
    }
}

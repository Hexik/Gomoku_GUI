package cz.fontan.gomoku_embryo.model

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Class defining one shot variable to prevent duplicities by orientation change
 */
class ConsumableValue<T>(private val data: T) {

    private val consumed = AtomicBoolean(false)

    /**
     * Value is consumed at first get()
     */
    fun consume(block: ConsumableValue<T>.(T) -> Unit) {
        if (!consumed.getAndSet(true)) {
            block(data)
        }
    }
}
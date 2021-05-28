package cz.fontan.gomoku_gui.game

import org.junit.Test

class MoveTest {
    @Test
    fun empty() {
        val m = Move()
        assert(m.hashCode() == 0)
    }
}
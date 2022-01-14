package cz.fontan.gomoku_embryo.game

import org.junit.Test

class MoveTest {
    @Test
    fun empty() {
        val m = Move()
        assert(m.hashCode() == 0)
        assert(m.type == EnumMove.Empty)
    }

    @Test(expected = IllegalArgumentException::class)
    fun wrong_coordinates_negative() {
        Move(-1, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun wrong_coordinates_too_big() {
        Move(BOARD_SIZE_MAX, BOARD_SIZE_MAX)
    }
}
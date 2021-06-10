package cz.fontan.gomoku_gui.game

/**
 * LimitBoard dimension
 */
const val BOARD_SIZE_MAX: Int = 20

/**
 * Move types
 */
enum class EnumMove {
    /**
     * No Move
     */
    Empty,

    /**
     * Black player
     */
    Black,

    /**
     * White player
     */
    White,

    /**
     * Wall/Edge of the board
     */
    Wall
}

/**
 * Move structure
 * @throws IllegalArgumentException
 */
class Move(
    /**
     * x-coordinate, 0 <= x < Game.dim
     */
    val x: Int = 0,

    /**
     * y-coordinate, 0 <= x < Game.dim
     */
    val y: Int = 0,

    /**
     * Move type one of EnumMove
     */
    val type: EnumMove = EnumMove.Empty
) {

    init {
        require(x >= 0)
        require(y >= 0)
        require(x < BOARD_SIZE_MAX)
        require(y < BOARD_SIZE_MAX)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return (other is Move) && (x == other.x) && (y == other.y) && (type == other.type)
    }

    override fun hashCode(): Int {
        return (x * BOARD_SIZE_MAX + y) * EnumMove.values().size + type.ordinal
    }
}

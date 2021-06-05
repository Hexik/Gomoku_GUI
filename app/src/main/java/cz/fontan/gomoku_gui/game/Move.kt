package cz.fontan.gomoku_gui.game

const val BOARD_SIZE_MAX: Int = 20

enum class EnumMove {
    Empty, Black, White, Wall
}

class Move(val x: Int = 0, val y: Int = 0, val type: EnumMove = EnumMove.Empty) {

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

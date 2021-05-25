package cz.fontan.gomoku_gui.game

const val BOARD_SIZE: Int = 15

enum class EnumMove {
    Empty, Black, White, Wall
}

class Move(val x: Int = 0, val y: Int = 0, val type: EnumMove = EnumMove.Empty) {

    init {
        require(x >= 0)
        require(y >= 0)
        require(x < BOARD_SIZE)
        require(y < BOARD_SIZE)
    }

    fun isPlayable(): Boolean {
        return type == EnumMove.Black || type == EnumMove.White
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return (other is Move) && (x == other.x) && (y == other.y) && (type == other.type)
    }

    override fun hashCode(): Int {
        return (x * BOARD_SIZE + y) * EnumMove.values().size + type.ordinal
    }
}

package cz.fontan.gomoku_gui.game

const val BOARD_SIZE = 15

enum class EnumMove {
    Empty, XX, OO, Block
}

class Move(val x: Int = 0, val y: Int = 0, val type: EnumMove = EnumMove.Empty) {

    init {
        require(x >= 0)
        require(y >= 0)
        require(x < BOARD_SIZE)
        require(y < BOARD_SIZE)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return (other is Move) && (x == other.x) && (y == other.y) && (type == other.type)
    }

    override fun hashCode(): Int {
        return (x * BOARD_SIZE + y) * EnumMove.values().size + type.ordinal
    }
}

class MoveList : Iterator<Move> {
    private var moveList: ArrayList<Move> = arrayListOf()
    private var lastMoveIndex: Int = -1
    private var currentMoveIndex: Int = -1

    fun isValid(): Boolean {
        return currentMoveIndex <= lastMoveIndex && lastMoveIndex < moveList.size
    }

    fun getCurrentMove(): Move {
        require(isValid())
        return when (currentMoveIndex) {
            -1 -> Move()
            else -> moveList[currentMoveIndex]
        }
    }

    fun reset() {
        moveList.clear()
        currentMoveIndex = -1
        lastMoveIndex = -1
        check(isValid())
    }

    fun undo() {
        if (currentMoveIndex >= 0) {
            --currentMoveIndex
        }
        check(isValid())
    }

    fun redo() {
        if (currentMoveIndex < lastMoveIndex) {
            ++currentMoveIndex
        }
        check(isValid())
    }

    fun add(move: Move) {
        require(isValid())
        when (currentMoveIndex) {
            moveList.size - 1 -> {
                moveList.add(move)
                ++currentMoveIndex
            }
            else -> {
                moveList[++currentMoveIndex] = move  // replace existing move
            }
        }
        lastMoveIndex = currentMoveIndex
        check(isValid())
    }

    fun getIndex(): Int {
        return currentMoveIndex
    }

    fun getLastIndex(): Int {
        return lastMoveIndex
    }

    fun size(): Int {
        return moveList.size
    }

    // Simple iterator implementation
    private var iteratorPtr = 0

    override fun hasNext(): Boolean {
        return iteratorPtr < moveList.size
    }

    override fun next(): Move {
        return moveList[iteratorPtr++]
    }

    fun rewind() {
        iteratorPtr = 0
    }
}

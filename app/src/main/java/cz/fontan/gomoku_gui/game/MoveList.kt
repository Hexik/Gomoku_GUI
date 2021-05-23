package cz.fontan.gomoku_gui.game

const val BOARD_SIZE = 15

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
        // move type validation
        // odd moves are Black, even are White
        when (move.type) {
            EnumMove.Black -> require(currentMoveIndex % 2 != 0)
            EnumMove.White -> require(currentMoveIndex % 2 == 0)
            else -> require(false)
        }
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

    fun toBoard(stdBoard: Boolean): String {
        require(isValid())

        val sb = StringBuilder()
        when (stdBoard) {
            true -> sb.appendLine("board")
            false -> sb.appendLine("yxboard")
        }
        rewind()
        var player = if (lastMoveIndex % 2 == 0) 2 else 1
        for (it in this) {
            sb.append(it.x).append(',').append(it.y).append(',').append(player).appendLine()
            player = 1 + player % 2
        }

        sb.appendLine("done")
        return sb.toString()
    }

    // Simple iterator implementation
    private var iteratorPtr = 0

    override fun hasNext(): Boolean {
        return iteratorPtr <= currentMoveIndex
    }

    override fun next(): Move {
        return moveList[iteratorPtr++]
    }

    fun rewind() {
        iteratorPtr = 0
    }
}

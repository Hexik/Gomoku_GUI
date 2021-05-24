package cz.fontan.gomoku_gui.game

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
        require(canUndo())
        --currentMoveIndex
        check(isValid())
    }

    fun canUndo(): Boolean {
        return currentMoveIndex >= 0
    }

    fun redo() {
        require(canRedo())
        ++currentMoveIndex
        check(isValid())
    }

    fun canRedo(): Boolean {
        return currentMoveIndex < lastMoveIndex
    }

    fun add(move: Move) {
        // move type validation, odd moves are Black, even are White
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

    fun get(index: Int): Move {
        return moveList[index]
    }
}

package cz.fontan.gomoku_gui.game

/**
 * Class keeping the list of Moves, user can browse the list in both direction
 */
class MoveList : Iterator<Move> {
    private val moveList: ArrayList<Move> = arrayListOf()
    private var lastMoveIndex: Int = -1
    private var currentMoveIndex: Int = -1

    /**
     * Invariant check
     */
    fun isValid(): Boolean {
        return -1 <= currentMoveIndex && currentMoveIndex <= lastMoveIndex && lastMoveIndex < moveList.size
    }

    /**
     * Get move at cursor position
     * @throws IllegalArgumentException
     */
    fun getCurrentMove(): Move {
        require(isValid())
        return when (currentMoveIndex) {
            -1 -> Move()
            else -> moveList[currentMoveIndex]
        }
    }

    /**
     * All data to start position
     * @throws IllegalStateException
     */
    fun reset() {
        moveList.clear()
        currentMoveIndex = -1
        lastMoveIndex = -1
        check(isValid())
    }

    /**
     * One move backward
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    fun undo() {
        require(canUndo())
        --currentMoveIndex
        check(isValid())
    }

    /**
     * Checks if can backward one move
     */
    fun canUndo(): Boolean {
        return currentMoveIndex >= 0
    }

    /**
     * One move forward
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    fun redo() {
        require(canRedo())
        ++currentMoveIndex
        check(isValid())
    }

    /**
     * Checks if can forward one move
     */
    fun canRedo(): Boolean {
        return currentMoveIndex < lastMoveIndex
    }

    /**
     * Add move to the list
     * position can be at the end or replace move in the middle and set new lastIndex
     * @param move add this
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
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

    /**
     * Get current cursor into MoveList
     */
    fun getIndex(): Int {
        return currentMoveIndex
    }

    /**
     * Get index of last valid move, can be != size - 1
     */
    fun getLastIndex(): Int {
        return lastMoveIndex
    }

    /**
     * Get size aka Move count
     */
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

    /**
     * Reset iterator to begin
     */
    fun rewind() {
        iteratorPtr = 0
    }

    /**
     * Get Move at position index
     * @param index position
     */
    fun get(index: Int): Move {
        return moveList[index]
    }
}

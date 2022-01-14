package cz.fontan.gomoku_embryo.game

import android.util.Log

// TODO MF add game type to fromStream(), toStream(), now is fixed line(2) = 1

private const val TAG = "Game"

/**
 * Information about current game, position, dimension, moveList, serialize position
 */
class Game(
    /**
     * Board dimension
     */
    var dim: Int
) {
    private val moveList = MoveList()
    private val desk = Array(dim * dim) { EnumMove.Empty }

    /**
     * side to move
     */
    var playerToMove: EnumMove = EnumMove.Black

    /**
     * best move during the search
     */
    var bestMove: Move = Move()

    /**
     * List of forbidden moves
     */
    var forbid: String = String()

    init {
        Log.d(TAG, "Init")
        newGame()
    }

    /**
     * Prepare new game
     */
    fun newGame(): Game {
        Log.d(TAG, "New Game")
        return reset()
    }

    /**
     * Put move on desk with all safety checks
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    fun makeMove(move: Move): Game {
        val localMove = Move(move.x, move.y, playerToMove)
        require(canMakeMove(localMove))
        desk[deskIndex(localMove)] = localMove.type
        moveList.add(localMove)
        check(!canMakeMove(localMove))
        return switchPlayerToMove()
    }

    /**
     * Remove last move from desk with all safety checks
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    fun undoMove(): Game {
        Log.d(TAG, "Undo")
        require(moveCount() > 0)
        require(canUndo())

        val lastMove = moveList.getCurrentMove()
        require(!canMakeMove(lastMove))
        require(playerToMove != lastMove.type)

        desk[deskIndex(lastMove)] = EnumMove.Empty
        moveList.undo()

        check(canMakeMove(lastMove))
        return switchPlayerToMove()
    }

    /**
     * Add move back after previous undoMove() action(s)
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    fun redoMove(): Game {
        Log.d(TAG, "Redo")
        require(canRedo())

        moveList.redo()
        val lastMove = moveList.getCurrentMove()
        require(canMakeMove(lastMove))

        desk[deskIndex(lastMove)] = lastMove.type

        check(!canMakeMove(lastMove))
        return switchPlayerToMove()
    }

    /**
     * Game at clean starting phase
     */
    fun reset(): Game {
        moveList.reset()
        playerToMove = EnumMove.Black
        bestMove = Move()
        desk.fill(EnumMove.Empty)
        return this
    }

    /**
     * Tests if move can be done
     */
    fun canMakeMove(m: Move): Boolean {
        return desk[deskIndex(m)] == EnumMove.Empty
    }

    /**
     * Checks if can backward one move
     */
    fun canUndo(): Boolean {
        return moveList.canUndo()
    }

    /**
     * Checks if can forward one move
     */
    fun canRedo(): Boolean {
        return moveList.canRedo()
    }

    /**
     * How many moves is on board
     */
    fun moveCount(): Int {
        return 1 + moveList.getIndex()
    }

    /**
     * @see MoveList.get
     */
    operator fun get(index: Int): Move = moveList.get(index)

    /**
     * Serialize position to String
     * Gomocup BOARD command is constructed
     * @throws IllegalArgumentException
     */
    fun toBoard(commonBoardCommand: Boolean): String {
        require(moveList.isValid())

        val sb = StringBuilder()
        when (commonBoardCommand) {
            true -> sb.appendLine("board")
            false -> sb.appendLine("yxboard")
        }
        moveList.rewind()
        var player = if (moveList.getIndex() % 2 == 0) 2 else 1
        for (it in moveList) {
            sb.appendLine("${it.x},${it.y},$player")
            player = 1 + player % 2
        }

        return sb.append("done").toString()
    }

    private fun deskIndex(m: Move): Int {
        require(m.x < dim)
        require(m.y < dim)
        return m.x + m.y * dim
    }

    private fun switchPlayerToMove(): Game {
        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        return this
    }

    /**
     * Serialize game to String, Yixin game format is used from compatibility reason
     * @throws IllegalArgumentException
     */
    fun toStream(): String {
        require(moveList.isValid())

        val sb =
            StringBuilder().appendLine(dim).appendLine(dim).appendLine("1")
        moveList.rewind()
        for (it in moveList) {
            sb.appendLine("${it.x} ${it.y}")
        }
        return sb.toString()
    }

    /**
     * Set game positions from String
     * Yixin format is expected
     * @param data serialized Game
     * @throws IllegalArgumentException
     */
    fun fromStream(data: String?): Game {
        data ?: return this

        val lines = data.reader().readLines()
        require(lines.size >= 3)
        require(lines[0].toInt() <= BOARD_SIZE_MAX)
        require(lines[1].toInt() <= BOARD_SIZE_MAX)
        require(lines[0].toInt() == dim)
        require(lines[1].toInt() == dim)
        require(lines[0].toInt() == lines[1].toInt())
        require(lines[2].toInt() == 1)

        reset()
        for (i in 3 until lines.size) {
            val numbers = lines[i].split(" ")
            require(numbers.size == 2)
            makeMove(Move(numbers[0].toInt(), numbers[1].toInt()))
        }
        return this
    }
}

package cz.fontan.gomoku_gui.game

import android.util.Log

private const val TAG = "Game"

class Game(val dim: Int) {
    private val moveList = MoveList()
    private val desk = Array(dim * dim) { EnumMove.Empty }
    var playerToMove: EnumMove = EnumMove.Black
    var gameOver:Boolean = false

    init {
        Log.d(TAG, "Init")
        newGame()
    }

    fun newGame(): Game {
        Log.d(TAG, "New Game")
        return reset()
    }

    fun makeMove(move: Move): Game {
        val localMove = Move(move.x, move.y, playerToMove)
        require(canMakeMove(localMove))
        desk[deskIndex(localMove)] = localMove.type
        moveList.add(localMove)
        check(!canMakeMove(localMove))
        return switchPlayerToMove()
    }

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

    fun reset(): Game {
        moveList.reset()
        playerToMove = EnumMove.Black
        gameOver = false
        desk.fill(EnumMove.Empty)
        return this
    }

    fun canMakeMove(m: Move): Boolean {
        return desk[deskIndex(m)] == EnumMove.Empty
    }

    fun canUndo(): Boolean {
        return moveList.canUndo()
    }

    fun canRedo(): Boolean {
        return moveList.canRedo()
    }

    fun moveCount(): Int {
        return 1 + moveList.getIndex()
    }

    operator fun get(index: Int): Move = moveList.get(index)

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
        return m.x + m.y * dim
    }

    private fun switchPlayerToMove(): Game {
        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        return this
    }

    fun toStream(): String {
        require(moveList.isValid())

        val sb = StringBuilder().appendLine(BOARD_SIZE).appendLine(BOARD_SIZE).appendLine("1")
        moveList.rewind()
        for (it in moveList) {
            sb.appendLine("${it.x} ${it.y}")
        }
        return sb.toString()
    }

    fun fromStream(data: String?): Game {
        data ?: return this

        val lines = data.reader().readLines()
        require(lines.size >= 3)
        require(lines[0].toInt() == BOARD_SIZE)
        require(lines[1].toInt() == BOARD_SIZE)
        require(lines[2].toInt() == 1)

        reset()
        for (i in 3..lines.size - 1) {
            val numbers = lines[i].split(" ")
            require(numbers.size == 2)
            makeMove(Move(numbers[0].toInt(), numbers[1].toInt()))
        }
        return this
    }
}

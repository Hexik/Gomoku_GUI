package cz.fontan.gomoku_gui.game

import android.util.Log

private const val TAG = "Game"

class Game(val dim: Int) {
    private val moveList = MoveList()
    private val desk = Array(dim * dim) { EnumMove.Empty }
    var playerToMove: EnumMove = EnumMove.Black

    init {
        Log.d(TAG, "Init")
        newGame()
    }

    fun newGame() {
        Log.d(TAG, "New Game")
        reset()
    }

    fun makeMove(move: Move) {
        val localMove = Move(move.x, move.y, playerToMove)
        require(canMakeMove(localMove))
        desk[deskIndex(localMove)] = localMove.type
        moveList.add(localMove)
        switchPlayerToMove()
        check(!canMakeMove(localMove))
    }

    fun undoMove() {
        Log.d(TAG, "Undo")
        require(moveCount() > 0)
        require(canUndo())

        val lastMove = moveList.getCurrentMove()
        require(!canMakeMove(lastMove))
        require(playerToMove != lastMove.type)

        desk[deskIndex(lastMove)] = EnumMove.Empty
        moveList.undo()

        switchPlayerToMove()
        check(canMakeMove(lastMove))
    }

    fun redoMove() {
        Log.d(TAG, "Redo")
        require(canRedo())

        moveList.redo()
        val lastMove = moveList.getCurrentMove()
        require(canMakeMove(lastMove))

        desk[deskIndex(lastMove)] = lastMove.type

        switchPlayerToMove()
        check(!canMakeMove(lastMove))
    }

    fun reset() {
        moveList.reset()
        playerToMove = EnumMove.Black
        desk.fill(EnumMove.Empty)
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

        sb.append("done")
        return sb.toString()
    }

    private fun deskIndex(m: Move): Int {
        return m.x + m.y * dim
    }

    private fun switchPlayerToMove() {
        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
    }

    fun toStream(): String {
        require(moveList.isValid())

        val sb = StringBuilder()
        sb.appendLine(BOARD_SIZE)
        sb.appendLine(BOARD_SIZE)
        sb.appendLine("1")
        moveList.rewind()
        for (it in moveList) {
            sb.appendLine("${it.x} ${it.y}")
        }
        return sb.toString()
    }

    fun fromStream(data: String?) {
        data ?: return

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
    }
}
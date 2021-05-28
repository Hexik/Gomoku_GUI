package cz.fontan.gomoku_gui.game

import android.util.Log

private const val TAG = "Game"

class Game(private val dim: Int) {
    private val moveList = MoveList()
    private val desk = Array(dim * dim) { EnumMove.Empty }
    private var playerToMove: EnumMove = EnumMove.Black
    private var searchMode: Boolean = false

    init {
        Log.d(TAG, "Init")
        reset()
    }

    fun newGame() {
        Log.d(TAG, "New Game")
        require(!searchMode)
        reset()
    }

    fun makeMove(move: Move) {
        require(!searchMode)
        val localMove = Move(move.x, move.y, playerToMove)
        require(canMakeMove(localMove))
        desk[deskIndex(localMove)] = localMove.type
        moveList.add(localMove)
        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        check(!canMakeMove(localMove))
    }

    fun undoMove() {
        require(!searchMode)
        Log.d(TAG, "Undo")
        require(moveCount() > 0)
        require(canUndo())

        val lastMove = moveList.getCurrentMove()
        require(!canMakeMove(lastMove))
        require(playerToMove != lastMove.type)

        desk[deskIndex(lastMove)] = EnumMove.Empty
        moveList.undo()

        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        check(canMakeMove(lastMove))
    }

    fun redoMove() {
        require(!searchMode)
        Log.d(TAG, "Redo")
        require(canRedo())

        moveList.redo()
        val lastMove = moveList.getCurrentMove()
        require(canMakeMove(lastMove))

        desk[deskIndex(lastMove)] = lastMove.type

        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
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
}
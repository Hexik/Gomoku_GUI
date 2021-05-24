package cz.fontan.gomoku_gui.game

class Game(private val dim: Int) {
    private val moveList = MoveList()
    private val desk = Array(dim * dim) { EnumMove.Empty }
    var playerToMove: EnumMove = EnumMove.Black

    init {
        reset()
    }

    fun reset() {
        moveList.reset()
        playerToMove = EnumMove.Black
        desk.fill(EnumMove.Empty)
    }

    fun makeMove(move: Move) {
        val localMove = Move(move.x, move.y, playerToMove)
        require(canMakeMove(localMove))
        desk[deskIndex(localMove)] = localMove.type
        moveList.add(localMove)
        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        check(!canMakeMove(localMove))
    }

    fun undoMove() {
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
        require(canRedo())

        moveList.redo()
        val lastMove = moveList.getCurrentMove()
        require(canMakeMove(lastMove))

        desk[deskIndex(lastMove)] = lastMove.type

        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        check(!canMakeMove(lastMove))
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

    fun toBoard(stdBoard: Boolean): String {
        require(moveList.isValid())

        val sb = StringBuilder()
        when (stdBoard) {
            true -> sb.appendLine("board")
            false -> sb.appendLine("yxboard")
        }
        moveList.rewind()
        var player = if (moveList.getIndex() % 2 == 0) 2 else 1
        for (it in moveList) {
            sb.appendLine("${it.x},${it.y},$player")
            player = 1 + player % 2
        }

        sb.appendLine("done")
        return sb.toString()
    }


    private fun deskIndex(m: Move): Int {
        return m.x + m.y * dim
    }
}
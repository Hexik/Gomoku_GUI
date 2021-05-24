package cz.fontan.gomoku_gui.game

class Game(val dim: Int) {
    private val moveList = MoveList()
    private val desk = Array(dim * dim) { EnumMove.Empty }
    var playerToMove = EnumMove.Black

    init {
        reset()
    }

    fun reset() {
        moveList.reset()
        playerToMove = EnumMove.Black
        desk.fill(EnumMove.Empty)
    }

    fun makeMove(m: Move) {
        require(canMakeMove(m))

        desk[deskIndex(m)] = playerToMove
        moveList.add(m)
        playerToMove = if (playerToMove == EnumMove.Black) EnumMove.White else EnumMove.Black
        check(!canMakeMove(m))
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

    private fun deskIndex(m: Move): Int {
        return m.x + m.y * dim
    }
}
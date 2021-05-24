package cz.fontan.gomoku_gui.game

import android.util.Log

private const val TAG = "Engine"

class Engine(private val dim: Int) {
    val game: Game = Game(dim)

    init {
        Log.d(TAG, "Init")
    }

    fun startSearch() {
        Log.d(TAG, "Start")
    }

    fun stopSearch() {
        Log.d(TAG, "Stop")
    }

    fun undoMove() {
        Log.d(TAG, "Undo")
        game.undoMove()
    }

    fun redoMove() {
        Log.d(TAG, "Redo")
        game.redoMove()
    }

    fun newGame() {
        Log.d(TAG, "New Game")
        game.reset()
    }

    fun addMove(move: Move) {
        require(game.canMakeMove(move))
        game.makeMove(move)
    }


}
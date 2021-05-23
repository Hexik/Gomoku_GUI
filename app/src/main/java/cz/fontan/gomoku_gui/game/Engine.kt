package cz.fontan.gomoku_gui.game

import android.util.Log

private const val TAG = "Engine"

class Engine {
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
    }

    fun redoMove() {
        Log.d(TAG, "Redo")
    }

    fun newGame() {
        Log.d(TAG, "New Game")
    }


}
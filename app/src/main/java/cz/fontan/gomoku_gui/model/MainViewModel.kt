package cz.fontan.gomoku_gui.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.fontan.gomoku_gui.InterfaceMain
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.Game
import cz.fontan.gomoku_gui.game.Move

class MainViewModel : ViewModel(), InterfaceMain {
    private val game = Game(BOARD_SIZE)

    val isSearching = MutableLiveData<Boolean>()
    val canRedo = MutableLiveData<Boolean>()
    val canUndo = MutableLiveData<Boolean>()

    init {
        isSearching.value = false
        canRedo.value = false
        canUndo.value = false
    }

    fun startSearch() {
        isSearching.value = true
        canUndo.value = false
        canRedo.value = false
    }

    fun stopSearch() {
        isSearching.value = false
        canUndo.value = game.canUndo()
        canRedo.value = game.canRedo()
    }

    fun undoMove() {
        game.undoMove()
        canUndo.value = game.canUndo()
        canRedo.value = game.canRedo()
    }

    fun redoMove() {
        game.redoMove()
        canUndo.value = game.canUndo()
        canRedo.value = game.canRedo()
    }

    fun newGame() {
        game.newGame()
        canUndo.value = game.canUndo()
        canRedo.value = game.canRedo()
    }

    override fun canMakeMove(move: Move): Boolean {
        return game.canMakeMove(move)
    }

    override fun makeMove(move: Move) {
        game.makeMove(move)
        canUndo.value = isSearching.value == false && game.canUndo()
        canRedo.value = isSearching.value == false && game.canRedo()
    }

    override fun moveCount(): Int {
        return game.moveCount()
    }

    override fun getIthMove(i: Int): Move {
        return game[i]
    }

    override fun isSearching(): Boolean {
        return game.searchMode
    }

}
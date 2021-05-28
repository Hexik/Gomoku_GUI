package cz.fontan.gomoku_gui.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cz.fontan.gomoku_gui.InterfaceMain
import cz.fontan.gomoku_gui.NativeInterface
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.Game
import cz.fontan.gomoku_gui.game.Move

class MainViewModel : ViewModel(), InterfaceMain {
    private val game = Game(BOARD_SIZE)

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean>
        get() = _isSearching

    private val _canRedo = MutableLiveData<Boolean>()
    val canRedo: LiveData<Boolean>
        get() = _canRedo

    private val _canUndo = MutableLiveData<Boolean>()
    val canUndo: LiveData<Boolean>
        get() = _canUndo

    val fromBrain: MutableLiveData<ArrayList<String>> = MutableLiveData<ArrayList<String>>()


    init {
        _isSearching.value = false
        _canRedo.value = false
        _canUndo.value = false
    }

    fun startSearch() {
        _isSearching.value = true
        _canUndo.value = false
        _canRedo.value = false
        NativeInterface.writeToBrain(game.toBoard(true))
    }

    fun stopSearch() {
        _isSearching.value = false
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
    }

    fun undoMove() {
        game.undoMove()
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
    }

    fun redoMove() {
        game.redoMove()
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
    }

    fun newGame() {
        game.newGame()
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
    }

    override fun canMakeMove(move: Move): Boolean {
        return game.canMakeMove(move)
    }

    override fun makeMove(move: Move) {
        game.makeMove(move)
        _canUndo.value = isSearching.value == false && game.canUndo()
        _canRedo.value = isSearching.value == false && game.canRedo()
    }

    override fun moveCount(): Int {
        return game.moveCount()
    }

    override fun getIthMove(i: Int): Move {
        return game[i]
    }

    override fun isSearching(): Boolean {
        return isSearching.value == true
    }

}
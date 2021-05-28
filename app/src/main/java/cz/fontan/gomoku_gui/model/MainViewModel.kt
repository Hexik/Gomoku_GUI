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

    private val _isDirty = MutableLiveData<Boolean>()
    val isDirty: LiveData<Boolean>
        get() = _isDirty

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
        _isDirty.value = false
        setIdle()
    }

    private fun setIdle() {
        _isSearching.value = false
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
    }

    fun startSearch() {
        _isSearching.value = true
        _canUndo.value = false
        _canRedo.value = false
        NativeInterface.writeToBrain(game.toBoard(true))
    }

    fun stopSearch() {
        NativeInterface.writeToBrain("YXSTOP")
        setIdle()
    }

    fun undoMove() {
        game.undoMove()
        setIdle()
    }

    fun redoMove() {
        game.redoMove()
        setIdle()
    }

    fun newGame() {
        game.newGame()
        setIdle()
    }

    override fun canMakeMove(move: Move): Boolean {
        return game.canMakeMove(move)
    }

    override fun makeMove(move: Move) {
        game.makeMove(move)
        setIdle()
        _isDirty.value = true
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
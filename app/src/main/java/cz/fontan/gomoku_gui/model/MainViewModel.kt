package cz.fontan.gomoku_gui.model

import android.util.Log
import androidx.lifecycle.*
import cz.fontan.gomoku_gui.InterfaceMain
import cz.fontan.gomoku_gui.NativeInterface
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.Game
import cz.fontan.gomoku_gui.game.Move
import kotlinx.coroutines.Dispatchers

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

    private val answersRepository = AnswersRepository()

    private val _dataFromBrain = answersRepository
        .fetchStrings()
        .asLiveData(
            // Use Default dispatcher for CPU intensive work and
            // viewModel scope for auto cancellation when viewModel
            // is destroyed
            Dispatchers.Default + viewModelScope.coroutineContext
        )

    val dataFromBrain: LiveData<ConsumableValue<String>>
        get() = _dataFromBrain

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

    fun processResponse(response: String) {
        val upper = response.uppercase()
        when {
            upper.startsWith("DEBUG") -> return
            upper.startsWith("ERROR") -> return
            upper.startsWith("MESSAGE") -> return
            upper.startsWith("OK") -> return
            upper.startsWith("SUGGEST") -> return
            upper.startsWith("UNKNOWN") -> return
            else -> parseMoveResponse(upper)
        }
    }

    private fun parseMoveResponse(response: String) {
        Log.d("Res", response)
        val splitted = response.split(",")
        if (splitted.size == 2) {
            makeMove(Move(splitted[0].toInt(), splitted[1].toInt()))
            setIdle()
        }
    }

}
package cz.fontan.gomoku_gui.model

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import cz.fontan.gomoku_gui.InterfaceMain
import cz.fontan.gomoku_gui.NativeInterface
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.Game
import cz.fontan.gomoku_gui.game.Move
import kotlinx.coroutines.Dispatchers

class MainViewModel(application: Application) : AndroidViewModel(application), InterfaceMain {
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
        loadGame()
        setIdleStatus()
    }

    private fun setIdleStatus() {
        _isDirty.value = true
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
        setIdleStatus()
    }

    fun undoMove() {
        game.undoMove()
        setIdleStatus()
    }

    fun redoMove() {
        game.redoMove()
        setIdleStatus()
    }

    fun newGame() {
        game.newGame()
        NativeInterface.writeToBrain("start ${game.dim}")
        setIdleStatus()
    }

    override fun canMakeMove(move: Move): Boolean {
        return game.canMakeMove(move)
    }

    override fun makeMove(move: Move) {
        game.makeMove(move)
        setIdleStatus()
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
            setIdleStatus()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("MainVM", "onCleared")
        saveGame()
    }

    fun saveGame() {
        val sharedPreference =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "PREFERENCE_NAME",
                Context.MODE_PRIVATE
            )
        val editor = sharedPreference.edit()
        editor.putString("Moves", game.toStream())
        editor.apply()
    }

    private fun loadGame() {
        val sharedPreference =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "PREFERENCE_NAME",
                Context.MODE_PRIVATE
            )
        game.fromStream(sharedPreference.getString("Moves", ""))
    }
}
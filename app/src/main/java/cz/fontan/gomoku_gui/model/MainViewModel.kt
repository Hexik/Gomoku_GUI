package cz.fontan.gomoku_gui.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import cz.fontan.gomoku_gui.InterfaceMain
import cz.fontan.gomoku_gui.NativeInterface
import cz.fontan.gomoku_gui.R
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.EnumMove
import cz.fontan.gomoku_gui.game.Game
import cz.fontan.gomoku_gui.game.Move
import kotlinx.coroutines.Dispatchers

class MainViewModel(application: Application) : AndroidViewModel(application), InterfaceMain {
    private val game = Game(BOARD_SIZE)

    // LiveData variables
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

    private val _msgDepth = MutableLiveData<String>()
    val msgDepth: LiveData<String>
        get() = _msgDepth

    private val _msgEval = MutableLiveData<String>()
    val msgEval: LiveData<String>
        get() = _msgEval

    private val _msgNodes = MutableLiveData<String>()
    val msgNodes: LiveData<String>
        get() = _msgNodes

    private val _msgSpeed = MutableLiveData<String>()
    val msgSpeed: LiveData<String>
        get() = _msgSpeed

    private val _msgResult = MutableLiveData<String>()
    val msgResult: LiveData<String>
        get() = _msgResult

    private val _dataFromBrain = AnswersRepository()
        .fetchStrings()
        .asLiveData(
            // Use Default dispatcher for CPU intensive work and
            // viewModel scope for auto cancellation when viewModel
            // is destroyed
            Dispatchers.Default + viewModelScope.coroutineContext
        )

    val dataFromBrain: LiveData<ConsumableValue<String>>
        get() = _dataFromBrain

    // Settings variables
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(getApplication<Application>().applicationContext)
    private var autoBlack: Boolean = false
    private var autoWhite: Boolean = false

    private var stopWasPressed = false

    init {
        loadGame()
        afterAction()
    }

    fun startSearch(forceSearch: Boolean) {
        if (!game.gameOver && (!stopWasPressed || forceSearch)) {
            _isSearching.value = true
            _canUndo.value = false
            _canRedo.value = false
            readAutoSettings()
            _isDirty.value = true
            stopWasPressed = false
            NativeInterface.writeToBrain(game.toBoard(true))
        }
    }

    fun stopSearch() {
        NativeInterface.writeToBrain("YXSTOP")
        queryGameResult()
        setIdleStatus()
        stopWasPressed = true
    }

    fun undoMove() {
        game.undoMove()
        stopWasPressed = false
        afterAction()
    }

    fun redoMove() {
        game.redoMove()
        afterAction()
    }

    fun newGame() {
        game.newGame()
        stopWasPressed = false
        NativeInterface.writeToBrain("start ${game.dim}")
        afterAction()
    }

    private fun afterAction() {
        NativeInterface.writeToBrain(game.toBoard(false))
        queryGameResult()
        setIdleStatus()
    }

    private fun setIdleStatus() {
        _isSearching.value = false
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
        readAutoSettings()
        _isDirty.value = true
    }

    private fun readAutoSettings() {
        autoBlack = sharedPreferences.getBoolean("check_box_preference_AI_black", false)
        autoWhite = sharedPreferences.getBoolean("check_box_preference_AI_white", false)
    }

    // InterfaceMain overrides
    override fun canMakeMove(move: Move): Boolean {
        return game.canMakeMove(move)
    }

    override fun makeMove(move: Move) {
        game.makeMove(move)
        readAutoSettings()
        queryGameResult()
        when {
            autoBlack && game.playerToMove == EnumMove.Black -> startSearch(false)
            autoWhite && game.playerToMove == EnumMove.White -> startSearch(false)
            else -> setIdleStatus()
        }
        stopWasPressed = false
    }

    private fun queryGameResult() {
        NativeInterface.writeToBrain("YXRESULT")
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

    // Parse incoming data from brain
    fun processResponse(response: String) {
        val upper = response.uppercase()
        when {
            upper.startsWith("DEBUG ") -> return
            upper.startsWith("ERROR ") -> return
            upper.startsWith("MESSAGE ") -> parseMessage(upper.removePrefix("MESSAGE "))
            upper.startsWith("OK") -> return
            upper.startsWith("SUGGEST ") -> return
            upper.startsWith("UNKNOWN") -> return
            upper.contains("NAME") -> return // ABOUT response
            else -> parseMoveResponse(upper)
        }
    }

    private fun parseMessage(response: String) {
        // MESSAGE ...
        when {
            response.startsWith("DEPTH ") -> parseStatus(response)
            response.startsWith("REALTIME ") -> parseRealTime(response.removePrefix("REALTIME "))
            response.startsWith("RESULT ") -> parseResult(response.removePrefix("RESULT "))
            else -> return
        }
    }

    private fun parseResult(response: String) {
        // MESSAGE RESULT ...
        when (response) {
            "BLACK" -> {
                _msgResult.value = getResourceString(R.string.result_black)
                game.gameOver = true
            }
            "WHITE" -> {
                _msgResult.value = getResourceString(R.string.result_white)
                game.gameOver = true
            }
            "DRAW" -> {
                _msgResult.value = getResourceString(R.string.result_draw)
                game.gameOver = true
            }
            else -> {
                _msgResult.value = getResourceString(R.string.none)
                game.gameOver = false
            }
        }
    }

    private fun getResourceString(resID: Int): String {
        return getApplication<Application>().applicationContext.resources.getString(resID)
    }

    private fun parseRealTime(response: String) {
        // MESSAGE REALTIME ...
        when {
            response.startsWith("BEST ") -> return
            response.startsWith("LOSE  ") -> return
            response.startsWith("POS  ") -> return
            response.startsWith("PV  ") -> return
            response.startsWith("REFRESH") -> return
            else -> return
        }
    }

    private fun parseStatus(response: String) {
        // MESSAGE [DEPTH] ...
        val splitted = response.split(" ")
        val it = splitted.iterator()
        // Caution: the message should be well-formed
        while (it.hasNext()) {
            when (it.next()) {
                "DEPTH" -> _msgDepth.value = it.next()
                "EV" -> _msgEval.value = it.next()
                "N" -> _msgNodes.value = it.next()
                "N/MS" -> _msgSpeed.value = it.next()
            }
        }
    }

    private fun parseMoveResponse(response: String) {
        // x,y
        Log.v("Res", response)
        val splitted = response.split(",")
        try {
            require(splitted.size == 2)
            makeMove(Move(splitted[0].toInt(), splitted[1].toInt()))
        } catch (e: IllegalArgumentException) {
            Log.wtf("Res", response)
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
                "GAME_DATA",
                Context.MODE_PRIVATE
            )
        val editor = sharedPreference.edit()
        editor.putString("Moves", game.toStream())
        editor.apply()
    }

    private fun loadGame() {
        val sharedPreference =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "GAME_DATA",
                Context.MODE_PRIVATE
            )
        try {
            game.fromStream(sharedPreference.getString("Moves", ""))
        } catch (e: IllegalArgumentException) {
            game.reset()
        }
    }
}
package cz.fontan.gomoku_embryo.model

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import cz.fontan.gomoku_embryo.*
import cz.fontan.gomoku_embryo.game.BOARD_SIZE_MAX
import cz.fontan.gomoku_embryo.game.EnumMove
import cz.fontan.gomoku_embryo.game.Game
import cz.fontan.gomoku_embryo.game.Move
import kotlinx.coroutines.Dispatchers

/**
 * ViewModel, current game status, can be saved and restored
 */
class MainViewModel(application: Application) : AndroidViewModel(application),
    InterfaceMainViewModel {
    private val game = Game(BOARD_SIZE_MAX)

    // LiveData variables
    private val _isDirty = MutableLiveData<Boolean>()

    /**
     * Model was changed
     */
    val isDirty: LiveData<Boolean>
        get() = _isDirty

    private val _canSearch = MutableLiveData<Boolean>()

    /**
     * canSearch status
     */
    val canSearch: LiveData<Boolean>
        get() = _canSearch

    private val _canStop = MutableLiveData<Boolean>()

    /**
     * canStop status
     */
    val canStop: LiveData<Boolean>
        get() = _canStop

    private val _canRedo = MutableLiveData<Boolean>()

    /**
     * canRedo status
     */
    val canRedo: LiveData<Boolean>
        get() = _canRedo

    private val _canUndo = MutableLiveData<Boolean>()

    /**
     * canUndo status
     */
    val canUndo: LiveData<Boolean>
        get() = _canUndo

    private val _msgDepth = MutableLiveData<String>()

    /**
     * Current search depth parsed from message
     */
    val msgDepth: LiveData<String>
        get() = _msgDepth

    private val _msgEval = MutableLiveData<String>()

    /**
     * Current search evaluation parsed from message
     */
    val msgEval: LiveData<String>
        get() = _msgEval

    private val _msgNodes = MutableLiveData<String>()

    /**
     * Current node count parsed from message
     */
    val msgNodes: LiveData<String>
        get() = _msgNodes

    private val _msgSpeed = MutableLiveData<String>()

    /**
     * Current search speed in N/ms parsed from message
     */
    val msgSpeed: LiveData<String>
        get() = _msgSpeed

    private val _msgResult = MutableLiveData<String>()

    /**
     * Game result as answer from brain
     */
    val msgResult: LiveData<String>
        get() = _msgResult

    private val _msgLabel = MutableLiveData<String>()

    /**
     * Game result as answer from brain
     */
    val msgLabel: LiveData<String>
        get() = _msgLabel

    private val isRunningTest: Boolean by lazy {
        try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private val _dataFromBrain = AnswersRepository(isRunningTest)
        .fetchStrings()
        .asLiveData(
            // Use Default dispatcher for CPU intensive work and
            // viewModel scope for auto cancellation when viewModel
            // is destroyed
            Dispatchers.Default + viewModelScope.coroutineContext
        )

    /**
     * LiveData from C++ brain, messages sent from brain
     */
    val dataFromBrain: LiveData<ConsumableValue<String>>
        get() = _dataFromBrain

    // Settings variables
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(getApplication<Application>().applicationContext)
    private var autoBlack: Boolean = false
    private var autoWhite: Boolean = false
    private var showForbid: Boolean = false
    private var moveTime: Int = 1000
    private var cacheSize: Int = 64
    private var threadNum: Int = 0

    private var stopWasPressed = false
    private var inSearch: Boolean = false
    private var showStatus: Boolean = false

    init {
        readSettings()
        loadGamePrivate()
        afterAction()
        setSearchTime()
    }

    private fun setSearchTime() {
        NativeInterface.writeToBrain("INFO timeout_match 3600000")
        NativeInterface.writeToBrain("INFO time_left 3600000")
        when (moveTime) {
            -1 -> NativeInterface.writeToBrain("INFO max_node -1") // unlimited
            else -> {
                NativeInterface.writeToBrain("INFO max_node 0")
                NativeInterface.writeToBrain("INFO timeout_turn $moveTime")
            }
        }
    }

    /**
     * Start search if possible, current position is sent to brain
     */
    fun startSearch(forceSearch: Boolean) {
        if (!inSearch && (!stopWasPressed || forceSearch)) {
            game.loserMoves.clear()
            inSearch = true
            _canSearch.value = false
            _canStop.value = true
            _canUndo.value = false
            _canRedo.value = false
            stopWasPressed = false
            setSearchTime()
            NativeInterface.writeToBrain("yxhashclear")
            NativeInterface.writeToBrain(game.toBoard(true))
            showStatus = false
            _msgLabel.value = getResourceString(R.string.time)
            _isDirty.value = true
        } else {
            setIdleStatus()
        }
    }

    /**
     * Stop running search, the brain should understand the YXSTOP command
     */
    fun stopSearch() {
        NativeInterface.writeToBrain("YXSTOP")
        setIdleStatus()
        stopWasPressed = true
    }

    /**
     * @see Game.undoMove
     */
    fun undoMove() {
        game.undoMove()
        game.loserMoves.clear()
        stopWasPressed = false
        showStatus = true
        afterAction()
    }

    /**
     * @see Game.redoMove
     */
    fun redoMove() {
        game.redoMove()
        game.loserMoves.clear()
        showStatus = true
        afterAction()
    }

    /**
     * Start new game, init C++ brain
     */
    fun newGame() {
        game.newGame()
        game.loserMoves.clear()
        game.blockMoves.clear()
        stopWasPressed = false
        showStatus = true
        NativeInterface.writeToBrain("yxhashclear")
        NativeInterface.writeToBrain("start ${game.dim}")
        afterAction()
        setSearchTime()
    }

    private fun resetProgress() {
        _msgDepth.value = getResourceString(R.string.none)
        _msgEval.value = getResourceString(R.string.none)
        _msgNodes.value = getResourceString(R.string.none)
        _msgSpeed.value = getResourceString(R.string.none)
    }

    private fun afterAction() {
        NativeInterface.writeToBrain(game.toBoard(false))
        queryGameResult()
        setIdleStatus()
        resetProgress()
    }

    private fun setIdleStatus() {
        game.bestMove = Move()
        inSearch = false
        _canStop.value = false
        _canUndo.value = game.canUndo()
        _canRedo.value = game.canRedo()
        _isDirty.value = true
    }

    private fun readSettings() {
        autoBlack = sharedPreferences.getBoolean("check_box_preference_AI_black", false)
        autoWhite = sharedPreferences.getBoolean("check_box_preference_AI_white", false)
        showForbid = sharedPreferences.getBoolean(
            "check_box_preference_forbidden",
            false
        ) && BuildConfig.FLAVOR == "renju"
        (sharedPreferences.getString("list_preference_time", "1000")?.toInt()
            ?: 1000).also { moveTime = it }
        val tmpDim = getDimension()
        if (tmpDim != game.dim) {
            game.dim = tmpDim
            newGame()
        }
        val tmpCacheSize = sharedPreferences.getString("list_preference_cache", "64")?.toInt() ?: 64
        if (tmpCacheSize != cacheSize) {
            cacheSize = tmpCacheSize
            NativeInterface.writeToBrain("INFO CACHE_SIZE " + (cacheSize * 1024).toString())
        }
        val tmpThreadNum =
            sharedPreferences.getString("check_box_preference_multicore", "1")?.toInt() ?: 1
        if (tmpThreadNum != threadNum) {
            threadNum = if (ProfiVersion.isActive) tmpThreadNum else 1
            NativeInterface.writeToBrain("INFO THREAD_NUM $threadNum")
        }
        ProfiVersion.checkProfi()
    }

    // InterfaceMain overrides
    override fun canMakeMove(move: Move): Boolean {
        return game.canMakeMove(move)
    }

    override fun getDeskType(move: Move): EnumMove {
        return game.getDeskType(move)
    }

    override fun makeMove(move: Move, sendBoard: Boolean) {
        game.makeMove(move)

        if (sendBoard) {
            NativeInterface.writeToBrain(game.toBoard(false))
        }
        queryGameResult()

        val isPlayer = move.type == EnumMove.Black || move.type == EnumMove.White
        when {
            isPlayer && autoBlack && game.playerToMove == EnumMove.Black && _canSearch.value == true -> startSearch(
                false
            )
            isPlayer && autoWhite && game.playerToMove == EnumMove.White && _canSearch.value == true -> startSearch(
                false
            )
            else -> setIdleStatus()
        }
        stopWasPressed = false
        _isDirty.value = true
    }

    override fun refresh() {
        readSettings()
    }

    private fun queryGameResult() {
        NativeInterface.writeToBrain("YXRESULT")
        val resp = NativeInterface.readFromBrain(10)
        processResponse(resp)
        if (showForbid) {
            NativeInterface.writeToBrain("YXSHOWFORBID")
        }
    }

    override fun moveCount(): Int {
        return game.moveCount()
    }

    override fun getIthMove(i: Int): Move {
        return game[i]
    }

    override fun isSearching(): Boolean {
        return inSearch
    }

    override fun getBestMove(): Move {
        return game.bestMove
    }

    override fun getLosers(): ArrayList<Move> {
        return game.loserMoves
    }

    override fun getBlockers(): ArrayList<Move> {
        return game.blockMoves
    }

    override fun getForbid(): String {
        return game.forbid
    }

    /**
     * Parse incoming data from brain
     * @param response incoming data
     */
    fun processResponse(response: String) {
        val upper = response.uppercase()
        when {
            upper.startsWith("DEBUG ") -> return
            upper.startsWith("ERROR ") -> return
            upper.startsWith("FORBID ") -> parseForbid(
                upper.removePrefix("FORBID ").removeSuffix(".")
            )
            upper.startsWith("MESSAGE ") -> parseMessage(upper.removePrefix("MESSAGE "))
            upper.startsWith("OK") -> return
            upper.startsWith("SUGGEST ") -> return
            upper.startsWith("UNKNOWN") -> return
            upper.contains("NAME") -> return // ABOUT response
            else -> parseMoveResponse(upper)
        }
    }

    private fun parseForbid(response: String) {
        try {
            require(response.length % 4 == 0)
            require(response.all { char -> char.isDigit() })
            game.forbid = response
            _isDirty.value = true
        } catch (e: IllegalArgumentException) {
            Log.wtf("Forbid", response)
        }
    }

    private fun parseMessage(response: String) {
        // MESSAGE ...
        when {
            response.startsWith("DEPTH ") -> parseStatus(response)
            response.startsWith("POSITION: ") -> parseBenchmarkPosition(response.removePrefix("POSITION: "))
            response.startsWith("REALTIME ") -> parseRealTime(response.removePrefix("REALTIME "))
            response.startsWith("RESULT ") -> parseMessageResult(response.removePrefix("RESULT "))
            response.startsWith("TIME(MS) ") -> parseBenchmarkResult(response)
            else -> return
        }
    }

    private fun parseMessageResult(response: String) {
        // MESSAGE RESULT ...
        var gameOver = true
        when (response) {
            "BLACK" -> _msgResult.value = getResourceString(R.string.result_black)
            "WHITE" -> _msgResult.value = getResourceString(R.string.result_white)
            "DRAW" -> _msgResult.value = getResourceString(R.string.result_draw)
            else -> {
                if (showStatus) {
                    _msgResult.value = getResourceString(R.string.none)
                }
                gameOver = false
            }
        }
        if (gameOver) {
            _msgLabel.value = getResourceString(R.string.status)
        }
        _canSearch.value = !gameOver
    }

    private fun getResourceString(resID: Int): String {
        return getApplication<Application>().applicationContext.getString(resID)
    }

    private fun parseRealTime(response: String) {
        // MESSAGE REALTIME ...
        when {
            response.startsWith("BEST ") -> {
                val splitted = response.removePrefix("BEST ").split(",")
                try {
                    require(splitted.size == 2)
                    game.bestMove = (Move(splitted[0].toInt(), splitted[1].toInt(), EnumMove.Wall))
                    _isDirty.value = true
                } catch (e: IllegalArgumentException) {
                    Log.wtf("Res", response)
                }
            }
            response.startsWith("LOSE ") -> {
                val splitted = response.removePrefix("LOSE ").split(",")
                try {
                    require(splitted.size == 2)
                    game.loserMoves.add(
                        (Move(
                            splitted[0].toInt(),
                            splitted[1].toInt(),
                            EnumMove.Wall
                        ))
                    )
                    _isDirty.value = true
                } catch (e: IllegalArgumentException) {
                    Log.wtf("Res", response)
                }
            }
            response.startsWith("POS  ") -> return
            response.startsWith("PV  ") -> return
            response.startsWith("REFRESH") -> {
                game.loserMoves.clear()
            }
            else -> return
        }
    }

    private fun parseBenchmarkPosition(response: String) {
        // MESSAGE POSITION: ...
        _msgDepth.value = response
        _msgSpeed.value = getResourceString(R.string.none)
        _msgEval.value = getResourceString(R.string.none)
        _msgNodes.value = getResourceString(R.string.none)
        _msgResult.value = getResourceString(R.string.benchmark)

    }

    private fun parseBenchmarkResult(response: String) {
        val splitted = response.split(" ")
        val it = splitted.iterator()
        // Caution: the message should be well-formed
        while (it.hasNext()) {
            when (it.next()) {
                "NPS" -> _msgSpeed.value = it.next().toInt().div(1000).toString()
            }
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
                "TM" -> _msgResult.value = "%.1f".format(it.next().toFloat().div(1000f))
            }
        }
    }

    private fun parseMoveResponse(response: String) {
        // x,y
        Log.v("Res", response)
        val splitted = response.split(",")
        try {
            require(splitted.size == 2)
            inSearch = false
            makeMove(Move(splitted[0].toInt(), splitted[1].toInt(), EnumMove.Black), false)
            game.bestMove = Move()
        } catch (e: IllegalArgumentException) {
            Log.wtf("Res", response)
        }
    }

    /**
     * When the instance is cleared, the game is saved
     */
    override fun onCleared() {
        super.onCleared()
        Log.i("MainVM", "onCleared")
        saveGamePrivate()
    }

    /**
     * Serialize game
     * @see Game.toStream
     */
    fun getGameAsStream(): String {
        return game.toStream()
    }

    /**
     * Save current position as shared preference
     */
    fun saveGamePrivate() {
        val sharedPreference =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "GAME_DATA",
                Context.MODE_PRIVATE
            )
        sharedPreference.edit().putString("Moves", game.toStream()).apply()
    }

    /**
     * Set game position from external data string
     */
    fun loadGameFromStream(data: String?) {
        try {
            game.fromStream(data)
            afterAction()
        } catch (e: IllegalArgumentException) {
            game.reset()
            Log.wtf("Load", data)
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Wrong game size!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadGamePrivate() {
        val sharedPreference =
            getApplication<Application>().applicationContext.getSharedPreferences(
                "GAME_DATA",
                Context.MODE_PRIVATE
            )
        try {
            game.dim = getDimension()
            NativeInterface.writeToBrain("start ${game.dim}")
            game.fromStream(sharedPreference.getString("Moves", "")?.trimMargin())
        } catch (e: IllegalArgumentException) {
            game.reset()
            Log.wtf("Load", "Private")
        }
    }

    private fun getDimension(): Int {
        val defaultDimension = getResourceString(R.string.default_board)
        return sharedPreferences.getString(
            "list_preference_board_size",
            defaultDimension
        )?.toInt() ?: defaultDimension.toInt()
    }
}
package cz.fontan.gomoku_gui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cz.fontan.gomoku_gui.databinding.ActivityMainBinding
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.Game
import cz.fontan.gomoku_gui.game.Move
import kotlin.system.exitProcess


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), InterfaceMain {

    private lateinit var binding: ActivityMainBinding
    private val gameInstance = Game(BOARD_SIZE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork() // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyLog()
                    .build()
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.textViewDataStatus.text = NativeInterface.helloStringFromJNI("Hi from Kotlin")

        binding.boardView.gameDelegate = this

        // Preset buttons
        updateButtons()

        // Game controlling buttons, work delegated to the Engine class
        binding.buttonPlay.setOnClickListener {
            gameInstance.startSearch()
            updateButtons()
        }
        binding.buttonStop.setOnClickListener {
            gameInstance.stopSearch()
            updateButtons()
        }
        binding.buttonUndo.setOnClickListener {
            gameInstance.undoMove()
            binding.boardView.invalidate()
            updateButtons()
        }
        binding.buttonRedo.setOnClickListener {
            gameInstance.redoMove()
            binding.boardView.invalidate()
            updateButtons()
        }
        binding.buttonNew.setOnClickListener {
            gameInstance.newGame()
            binding.boardView.invalidate()
            updateButtons()
        }
    }

    private fun updateButtons() {
        binding.buttonPlay.isEnabled = !gameInstance.searchMode
        binding.buttonStop.isEnabled = gameInstance.searchMode
        binding.buttonRedo.isEnabled = !gameInstance.searchMode && gameInstance.canRedo()
        binding.buttonUndo.isEnabled = !gameInstance.searchMode && gameInstance.canUndo()
        binding.buttonNew.isEnabled = !gameInstance.searchMode
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            Log.d(TAG, "Settings selected")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.menu_about) {
            Log.d(TAG, "About")
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        if (item.itemId == R.id.menu_quit) {
            Log.d(TAG, "Quit")
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask()
            } else {
                finishAffinity()
            }
            exitProcess(0)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Log.v(TAG, "onStart")
    }

    override fun onRestart() {
        super.onRestart()
        Log.v(TAG, "onRestart")
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.v(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }

    override fun canMakeMove(move: Move): Boolean {
        return gameInstance.canMakeMove(move)
    }

    override fun makeMove(move: Move) {
        gameInstance.makeMove(move)
        updateButtons()
    }

    override fun moveCount(): Int {
        return gameInstance.moveCount()
    }

    override fun getIthMove(i: Int): Move {
        return gameInstance[i]
    }

    override fun isSearching(): Boolean {
        return gameInstance.searchMode
    }

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

}

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
import cz.fontan.gomoku_gui.game.Engine
import kotlin.system.exitProcess


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var boardView: BoardView
    private var engine = Engine()

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

        // Preset buttons
        stoppedModeButtons()

        binding.boardView.engineDelegate = engine
        // Game controlling buttons, work delegated to the Engine class
        binding.buttonPlay.setOnClickListener {
            playModeButtons()
            engine.startSearch()
        }
        binding.buttonStop.setOnClickListener {
            stoppedModeButtons()
            engine.stopSearch()
        }
        binding.buttonUndo.setOnClickListener { engine.undoMove() }
        binding.buttonRedo.setOnClickListener { engine.redoMove() }
        binding.buttonNew.setOnClickListener { engine.newGame() }
    }

    private fun playModeButtons() {
        binding.buttonPlay.isEnabled = false
        binding.buttonStop.isEnabled = true
        binding.buttonRedo.isEnabled = false
        binding.buttonUndo.isEnabled = false
        binding.buttonNew.isEnabled = false
    }

    private fun stoppedModeButtons() {
        binding.buttonPlay.isEnabled = true
        binding.buttonStop.isEnabled = false
        binding.buttonRedo.isEnabled = true
        binding.buttonUndo.isEnabled = true
        binding.buttonNew.isEnabled = true

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

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

}

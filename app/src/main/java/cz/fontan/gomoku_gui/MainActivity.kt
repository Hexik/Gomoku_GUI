package cz.fontan.gomoku_gui

import android.content.Intent
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import cz.fontan.gomoku_gui.databinding.ActivityMainBinding
import cz.fontan.gomoku_gui.game.BOARD_SIZE_MAX
import cz.fontan.gomoku_gui.model.MainViewModel
import kotlin.system.exitProcess


private const val TAG = "MainActivity"

/**
 * Main App Activity, setup bindings and observers
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    /**
     * Prepare bindings, buttons, observers
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postAtFrontOfQueue {
            if (BuildConfig.DEBUG) {
                StrictMode.setThreadPolicy(
                    ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork() // or .detectAll() for all detectable problems
                        .detectAll()
                        .penaltyLog()
                        .build()
                )
                StrictMode.setVmPolicy(
                    VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .detectAll()
                        .penaltyLog()
                        .build()
                )
            }
        }

        prepareBindings()
        prepareButtons()
        prepareObservers()
    }

    private fun prepareBindings() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelProvider.AndroidViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        binding.boardView.gameDelegate = viewModel
    }

    // Game controlling buttons
    // work delegated to the ViewModel class
    private fun prepareButtons() {
        binding.buttonPlay.setOnClickListener {
            viewModel.startSearch(true)
        }
        binding.buttonStop.setOnClickListener {
            viewModel.stopSearch()
        }
        binding.buttonUndo.setOnClickListener {
            viewModel.undoMove()
        }
        binding.buttonRedo.setOnClickListener {
            viewModel.redoMove()
        }
        binding.buttonNew.setOnClickListener {
            viewModel.newGame()
        }
    }

    // Observers
    private fun prepareObservers() {
        // Observe data stream from brain
        viewModel.dataFromBrain.observe(this, { it ->
            it?.consume { viewModel.processResponse(it) }
        })

        viewModel.isDirty.observe(
            this, { binding.boardView.invalidate() }
        )

        viewModel.canSearch.observe(this, {
            binding.buttonPlay.isEnabled = it
        })

        viewModel.canStop.observe(this, {
            binding.buttonStop.isEnabled = it
        })

        viewModel.canRedo.observe(this, {
            binding.buttonRedo.isEnabled = it
            binding.buttonNew.isEnabled = it || binding.buttonUndo.isEnabled
        })

        viewModel.canUndo.observe(this, {
            binding.buttonUndo.isEnabled = it
            binding.buttonNew.isEnabled = it || binding.buttonRedo.isEnabled
        })

        viewModel.msgDepth.observe(this, { binding.textViewDataDepth.text = it })
        viewModel.msgEval.observe(this, { binding.textViewDataEval.text = it })
        viewModel.msgNodes.observe(this, { binding.textViewDataNodes.text = it })
        viewModel.msgSpeed.observe(this, { binding.textViewDataSpeed.text = it })
        viewModel.msgResult.observe(this, { binding.textViewDataStatus.text = it })
    }

    /**
     * Create main App menu from resources
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Start action after the menu item was selected
     */
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
            viewModel.saveGame()
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask()
            } else {
                finishAffinity()
            }
            exitProcess(0)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * @see AppCompatActivity.onStart
     */
    override fun onStart() {
        super.onStart()
        Log.v(TAG, "onStart")
    }

    /**
     * @see AppCompatActivity.onRestart
     */
    override fun onRestart() {
        super.onRestart()
        Log.v(TAG, "onRestart")
    }

    /**
     * Refresh Board an ViewModel
     * @see AppCompatActivity.onResume
     */
    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        binding.boardView.invalidate()
        Log.v(TAG, "onResume")
    }

    /**
     * @see AppCompatActivity.onPause
     */
    override fun onPause() {
        super.onPause()
        Log.v(TAG, "onPause")
    }

    /**
     * @see AppCompatActivity.onStop
     */
    override fun onStop() {
        super.onStop()
        Log.v(TAG, "onStop")
    }

    /**
     * @see AppCompatActivity.onDestroy
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
            NativeInterface.startBrain(BOARD_SIZE_MAX)
        }
    }
}

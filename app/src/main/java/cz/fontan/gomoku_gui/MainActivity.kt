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
import androidx.lifecycle.ViewModelProvider
import cz.fontan.gomoku_gui.databinding.ActivityMainBinding
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.model.MainViewModel
import cz.fontan.gomoku_gui.model.MainViewModelFactory
import kotlin.system.exitProcess


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

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

        val factory = MainViewModelFactory()
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

        binding.boardView.gameDelegate = viewModel

        // Game controlling buttons
        // work delegated to the ViewModel class
        binding.buttonPlay.setOnClickListener {
            viewModel.startSearch()
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

        // Observers
        // Observe data stream from brain
        viewModel.dataFromBrain.observe(this, { it ->
            it?.consume { viewModel.processResponse(it) }
        })

        viewModel.isDirty.observe(
            this, { binding.boardView.invalidate() }
        )

        viewModel.isSearching.observe(this, {
            binding.buttonPlay.isEnabled = !it
            binding.buttonStop.isEnabled = it
            binding.buttonNew.isEnabled = !it
            if (it) {
                binding.buttonRedo.isEnabled = false
                binding.buttonUndo.isEnabled = false
            }
        })

        viewModel.canRedo.observe(this, {
            binding.buttonRedo.isEnabled = it
        })

        viewModel.canUndo.observe(this, {
            binding.buttonUndo.isEnabled = it
        })
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
            NativeInterface.startBrain(BOARD_SIZE)
        }
    }
}

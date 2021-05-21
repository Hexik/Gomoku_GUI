package cz.fontan.gomoku_gui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

private const val _TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(_TAG, "onCreate")
        setContentView(R.layout.activity_main)
    }
}
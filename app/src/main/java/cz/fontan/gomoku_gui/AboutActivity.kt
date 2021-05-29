package cz.fontan.gomoku_gui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity(R.layout.activity_about) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build date from BuildConfig
        findViewById<TextView>(R.id.textViewAboutBuildDateValue).text = BuildConfig.BUILD_TIME
    }
}
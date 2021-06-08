package cz.fontan.gomoku_gui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

private const val SPLASH_DELAY: Long = 2000L

/**
 * Postopone start of MainActivity, showa invitation screen
 */
class SplashScreenActivity : AppCompatActivity() {

    /**
     *  onCreate Starts main activity after short delay
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }
}
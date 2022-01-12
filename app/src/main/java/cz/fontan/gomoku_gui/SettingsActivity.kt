package cz.fontan.gomoku_gui

import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

private const val TITLE_TAG = "settingsActivityTitle"

/**
 * Settings (configuration) activity
 */
class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    /**
     * Create Activity, setup FragmentManager
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Save current activity title so we can set it again after a configuration change
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(TITLE_TAG, title)
    }

    /**
     * Back navigation
     */
    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    /**
     * Fragment Start
     */
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        ).apply {
            arguments = pref.extras
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    /**
     * Main (header) fragment
     */
    class HeaderFragment : PreferenceFragmentCompat() {
        /**
         * Create main settings fragment
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
        }
    }

    /**
     * AI fragment
     */
    @Keep
    class AISettingsFragment : PreferenceFragmentCompat() {
        /**
         * Create AI settings fragment
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.ai_preferences, rootKey)
        }
    }

    /**
     * Board fragment
     */
    @Keep
    class BoardFragment : PreferenceFragmentCompat() {
        /**
         * Create board settings fragment
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.board_preferences, rootKey)
        }
    }

    /**
     * Level fragment
     */
    @Keep
    class LevelFragment : PreferenceFragmentCompat() {
        /**
         * Create level settings fragment
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.level_preferences, rootKey)
            preferenceScreen.findPreference<Preference>("check_box_preference_multicore")?.isEnabled =
                ProfiVersion.isProfi()
            preferenceScreen.findPreference<Preference>("check_box_preference_multicore")?.title =
                getString(
                    if (ProfiVersion.isProfi()) R.string.multithreading_profi else R.string.multithreading_no
                )
        }
    }
}
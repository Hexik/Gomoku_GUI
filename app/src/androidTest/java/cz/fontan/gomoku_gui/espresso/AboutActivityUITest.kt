package cz.fontan.gomoku_gui.espresso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.fontan.gomoku_gui.AboutActivity
import cz.fontan.gomoku_gui.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutActivityUITest {
    /**
     * Use [ActivityScenarioRule] to create and launch the activity under test before each test,
     * and close it after each test.
     */
    @get:Rule
    var activityRule: ActivityScenarioRule<AboutActivity> =
        ActivityScenarioRule(AboutActivity::class.java)

    @Test
    fun checkText_AboutActivity() {
        onView(withId(R.id.textViewAboutEngineVersionValue))
            .check(matches(withText("Embryo")))
    }
}
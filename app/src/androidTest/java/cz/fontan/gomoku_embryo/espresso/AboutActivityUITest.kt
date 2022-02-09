package cz.fontan.gomoku_embryo.espresso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.fontan.gomoku_embryo.AboutActivity
import cz.fontan.gomoku_embryo.R
import org.hamcrest.CoreMatchers.containsString
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutActivityUITest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun oneTimeSetup() {
            try {
                System.loadLibrary("native-lib")
            } catch (e: UnsatisfiedLinkError) {
                // log the error or track it in analytics
            }
        }
    }

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
            .check(matches(withText(containsString("Embryo"))))
    }
}
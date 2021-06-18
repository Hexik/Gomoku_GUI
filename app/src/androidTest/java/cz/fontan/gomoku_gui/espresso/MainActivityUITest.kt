package cz.fontan.gomoku_gui.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import cz.fontan.gomoku_gui.MainActivity
import cz.fontan.gomoku_gui.R
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test

class MainActivityUITest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun NewGame_MainActivity() {
        // NewGame
        Espresso.onView(ViewMatchers.withId(R.id.button_new)).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
        // Check Buttons
        Espresso.onView(ViewMatchers.withId(R.id.button_play))
            .check(matches(ViewMatchers.isEnabled()))
        Espresso.onView(ViewMatchers.withId(R.id.button_stop))
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_redo))
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_undo))
            .check(matches(not(ViewMatchers.isEnabled())))
    }

    @Test
    fun PlayStop_MainActivity() {
        // NewGame clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_new)).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
        // Play clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_play))
            .check(matches(ViewMatchers.isEnabled())).perform(
                click()
            ).check(matches(not(ViewMatchers.isEnabled())))
        // Check Buttons
        Espresso.onView(ViewMatchers.withId(R.id.button_play))
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_stop))
            .check(matches(ViewMatchers.isEnabled()))
        Espresso.onView(ViewMatchers.withId(R.id.button_redo))
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_undo))
            .check(matches(not(ViewMatchers.isEnabled())))
        // Stop clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_stop))
            .check(matches(ViewMatchers.isEnabled())).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
    }
}
package cz.fontan.gomoku_gui.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import cz.fontan.gomoku_gui.CountingIdlingResourceSingleton
import cz.fontan.gomoku_gui.MainActivity
import cz.fontan.gomoku_gui.R
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityUITest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance()
            .register(CountingIdlingResourceSingleton.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance()
            .unregister(CountingIdlingResourceSingleton.countingIdlingResource)
    }


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
    fun Play_MainActivity() {
        // NewGame clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_new)).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
        // Play clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_play))
            .check(matches(ViewMatchers.isEnabled())).perform(click())
        // Check Buttons
        Espresso.onView(ViewMatchers.withId(R.id.button_play))
            .check(matches(ViewMatchers.isEnabled()))
        Espresso.onView(ViewMatchers.withId(R.id.button_stop))
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_redo))
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_undo))
            .check(matches(ViewMatchers.isEnabled()))
        Espresso.onView(ViewMatchers.withId(R.id.button_new))
            .check(matches(ViewMatchers.isEnabled()))
        // Undo clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_undo)).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.button_redo))
            .check(matches(ViewMatchers.isEnabled()))

        // Play clicked
        for (i in 0..10) {
            Espresso.onView(ViewMatchers.withId(R.id.button_play))
                .check(matches(ViewMatchers.isEnabled())).perform(click())
        }
        // Undo clicked
        for (i in 0..10) {
            Espresso.onView(ViewMatchers.withId(R.id.button_undo))
                .check(matches(ViewMatchers.isEnabled())).perform(click())
        }
        Espresso.onView(ViewMatchers.withId(R.id.button_redo))
            .check(matches(ViewMatchers.isEnabled()))
        Espresso.onView(ViewMatchers.withId(R.id.button_undo))
            .check(matches(not(ViewMatchers.isEnabled())))

        Espresso.onView(ViewMatchers.withId(R.id.button_new))
            .check(matches(ViewMatchers.isEnabled()))
            .perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
    }
}
package cz.fontan.gomoku_embryo.espresso

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import cz.fontan.gomoku_embryo.CountingIdlingResourceSingleton
import cz.fontan.gomoku_embryo.MainActivity
import cz.fontan.gomoku_embryo.R
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
    fun newGame_MainActivity() {
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
    fun play_MainActivity() {
        // NewGame clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_new)).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
        // Play clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_play))
            .check(matches(ViewMatchers.isEnabled())).perform(click())
        Thread.sleep(100)
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
            Espresso.onView(ViewMatchers.withId(R.id.button_play)).perform(click())
            Thread.sleep(2000)
        }
        // Undo clicked
        for (i in 0..10) {
            Espresso.onView(ViewMatchers.withId(R.id.button_undo))
                .check(matches(ViewMatchers.isEnabled())).perform(click())
            Thread.sleep(100)
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

    @Test
    fun mouseClick_MainActivity() {
        // NewGame clicked
        Espresso.onView(ViewMatchers.withId(R.id.button_new)).perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
        // Mouse clicked
        Espresso.onView(ViewMatchers.withId(R.id.board_view)).perform(clickXY(50f, 50f))
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

        Espresso.onView(ViewMatchers.withId(R.id.button_new))
            .check(matches(ViewMatchers.isEnabled()))
            .perform(click())
            .check(matches(not(ViewMatchers.isEnabled())))
    }

    private fun clickXY(pctX: Float, pctY: Float): ViewAction {
        return GeneralClickAction(
            Tap.SINGLE, { view ->
                val screenPos = IntArray(2)
                view.getLocationOnScreen(screenPos)
                val screenX: Float = screenPos[0] + view.width * pctX / 100f
                val screenY: Float = screenPos[1] + view.height * pctY / 100f
                floatArrayOf(screenX, screenY)
            },
            Press.FINGER,
            InputDevice.SOURCE_MOUSE,
            MotionEvent.BUTTON_PRIMARY
        )
    }
}
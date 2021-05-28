package cz.fontan.gomoku_gui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.preference.PreferenceManager
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.EnumMove
import cz.fontan.gomoku_gui.game.Move

private const val TAG: String = "BoardView"

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val kStepCount = BOARD_SIZE - 1
    private val paint = Paint()
    private var lastMove = Move()
    private var offset = 0f
    private var step = 0f
    private var limitLow = 0f
    private var limitHigh = 0f
    private var showNumbers = false
    private var showCoordinates = false
    var gameDelegate: InterfaceMain? = null
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private fun recalcLimits() {
        Log.d(TAG, "Reca")
        showNumbers = sharedPreferences.getBoolean("check_box_preference_numbers", true)
        showCoordinates = sharedPreferences.getBoolean("check_box_preference_coordinates", true)
        if (showCoordinates) {
            offset = 0.08f * width
            step = (width - 1.6f * offset) / kStepCount
        } else {
            offset = 0.05f * width
            step = (width - 2.0f * offset) / kStepCount
        }
        offset = if (showCoordinates) 0.08f * width else 0.05f * width
        step =
            if (showCoordinates) (width - 1.6f * offset) / kStepCount else (width - 2.0f * offset) / kStepCount
        limitLow = offset - step * 0.5f
        limitHigh = offset + kStepCount * step + step * 0.5f
        paint.textSize = step * 0.5f
    }

    init {
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        // Make one time pre-calculation at correct time,
        // if you ask for width, height too early, the value is 0
        doOnPreDraw {
            Log.d(TAG, "Pre $width,$height,$offset")
            recalcLimits()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val smaller = kotlin.math.min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(smaller, smaller)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        Log.d(TAG, "Draw $width,$height,$offset")

        recalcLimits()
        drawBoard(canvas)
        drawStones(canvas)
    }

    private fun drawStones(canvas: Canvas) {
        val safeDelegate = gameDelegate ?: return
        val oldColor = paint.color

        for (i in 0 until (safeDelegate.moveCount())) {
            val p = move2Point(safeDelegate.getIthMove(i))
            paint.color = if (i % 2 != 0) Color.WHITE else Color.DKGRAY
            canvas.drawCircle(p.x, p.y, step * 0.44f, paint)
            if (showNumbers) {
                paint.color = when {
                    i == safeDelegate.moveCount() - 1 -> Color.RED
                    i % 2 == 0 -> Color.WHITE
                    else -> Color.DKGRAY
                }
                canvas.drawText((i + 1).toString(), p.x, p.y + paint.textSize * 0.33f, paint)
            }
        }
        paint.color = oldColor
    }

    private fun drawBoard(canvas: Canvas) {
        for (i in 0..kStepCount) {
            canvas.drawLine(
                offset + i * step,
                offset,
                offset + i * step,
                offset + kStepCount * step,
                paint
            )

            canvas.drawLine(
                offset,
                offset + i * step,
                offset + kStepCount * step,
                offset + i * step,
                paint
            )

            if (showCoordinates) {
                canvas.drawText(
                    (i + 'A'.code).toChar().toString(),
                    offset + i * step,
                    offset * 0.5f,
                    paint
                )
                canvas.drawText(
                    (BOARD_SIZE - i).toString(),
                    offset * 0.33f,
                    offset + i * step + paint.textSize * 0.33f,
                    paint
                )
            }
        }
    }

    private fun coordinates2Move(x: Float, y: Float): Move {
        return Move(
            ((x - offset + step / 2) / step).toInt(),
            kStepCount - ((y - offset + step / 2) / step).toInt(), EnumMove.Wall
        )
    }

    private fun move2Point(move: Move): PointF {
        return PointF(offset + move.x * step, offset + (kStepCount - move.y) * step)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        gameDelegate ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "Down ${event.x},${event.y}")
                // Clip event coordinates to be max step/2 from board edges
                if (event.x <= limitLow || event.x >= limitHigh) return false
                if (event.y <= limitLow || event.y >= limitHigh) return false
                lastMove = coordinates2Move(event.x, event.y)
                if (gameDelegate?.isSearching()!!) return false
                if (!(gameDelegate!!.canMakeMove(lastMove))) return false
                Log.d(TAG, "Down ${lastMove.x},${lastMove.y}")
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                gameDelegate?.makeMove(lastMove)
                invalidate()
            }
        }
        return true
    }
}

package cz.fontan.gomoku_gui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnPreDraw
import cz.fontan.gomoku_gui.game.BOARD_SIZE
import cz.fontan.gomoku_gui.game.Engine
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
    var engineDelegate: Engine? = null

    init {
        // Make one time precalculation at correct time,
        // if you ask for width, height too early, the value is 0
        doOnPreDraw {
            Log.d(TAG, "Pred $width,$height,$offset")
            offset = 0.05f * width
            step = (width - 2 * offset) / kStepCount
            limitLow = offset - step / 2
            limitHigh = offset + kStepCount * step + step / 2
        }
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        Log.d(TAG, "Draw $width,$height,$offset")

        drawBoard(canvas)
        drawStones(canvas)
    }

    private fun drawStones(canvas: Canvas) {
        val oldColor = paint.color
        for (i in 0 until engineDelegate?.game?.moveCount()!!) {
            val p = move2Point(engineDelegate?.game!![i])
            paint.color = if (i % 2 != 0) Color.RED else Color.DKGRAY
            canvas.drawCircle(p.x, p.y, step / 2.1f, paint)
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
        }
    }

    private fun coords2Move(x: Float, y: Float): Move {
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
        engineDelegate ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "Down ${event.x},${event.y}")
                // Clip event coordinates to be max step/2 from board egdes
                if (event.x <= limitLow || event.x >= limitHigh) return false
                if (event.y <= limitLow || event.y >= limitHigh) return false
                lastMove = coords2Move(event.x, event.y)
                if (!(engineDelegate!!.game.canMakeMove(lastMove))) return false
                Log.d(TAG, "Down ${lastMove.x},${lastMove.y}")
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                engineDelegate?.addMove(lastMove)
                invalidate()
            }
        }
        return true
    }
}
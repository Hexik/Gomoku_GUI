package cz.fontan.gomoku_gui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import cz.fontan.gomoku_gui.game.BOARD_SIZE

private const val TAG: String = "BoardView"

class BoardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val kStepCount = BOARD_SIZE - 1
    private val paint = Paint()
    private var offset = 0f
    private var step = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "Size $widthMeasureSpec,$heightMeasureSpec")
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        Log.d(TAG, "Draw $width,$height,$offset")

        val boardSide = kotlin.math.min(width, height) * scaleFactor
        offset = 0.05f * width
        step = (width - 2 * offset) / (BOARD_SIZE - 1)
        drawBoard(canvas)
        drawStones(canvas)
    }

    private fun drawStones(canvas: Canvas) {

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
            kStepCount - ((y - offset + step / 2) / step).toInt()
        )
    }

    private fun move2Point(move: Move): PointF {
        return PointF(offset + move.x * step, offset + (kStepCount - move.y) * step)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "Down " + event.x.toString() + "," + event.y.toString())
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
                invalidate()
            }
        }
        return true
    }
}
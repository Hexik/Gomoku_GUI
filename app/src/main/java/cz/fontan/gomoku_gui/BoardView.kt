package cz.fontan.gomoku_gui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.view.doOnPreDraw
import androidx.preference.PreferenceManager
import cz.fontan.gomoku_gui.game.BOARD_SIZE_MAX
import cz.fontan.gomoku_gui.game.Move

private const val TAG: String = "BoardView"

class BoardView(context: Context?, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageView(
        context!!, attrs
    ) {

    // constants
    companion object {
        private const val kHandicapDrawCoef = 0.07f
        private const val kHandicapOffset = 3
        private const val kMatrixScaleFactor = 1.6f
        private const val kStoneCoef = 0.44f
    }

    var gameDelegate: InterfaceMain? = null

    // draw related
    private val paint = Paint()
    private var lastMove = Move()
    private var limitLow = 0f
    private var limitHigh = 0f
    private var offset = 0f
    private var step = 0f

    // setting preferences
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private var showNumbers = false
    private var showCoordinates = false
    private var kBoardSize = BOARD_SIZE_MAX
    private var kStepCount = kBoardSize - 1
    private var zoom = false
    private var working = false

    // transformation matrix
    private val originalMatrix = Matrix()
    private val workingMatrix = Matrix()

    init {
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        // Make one time pre-calculation at correct time,
        // if you ask for width, height too early, the value is 0
        doOnPreDraw {
            Log.v(TAG, "Pre $width,$height,$offset")
            recalcLimits()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // be sure the view has square shape
        val smaller = kotlin.math.min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(smaller, smaller)
    }


    override fun onDraw(canvas: Canvas?) {
        canvas ?: return
        if (zoom) {
            canvas.concat(if (working) workingMatrix else originalMatrix)
        }

        recalcLimits()
        drawBoard(canvas)
        drawStones(canvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        gameDelegate ?: return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (zoom && !working) {
                    working = true
                    workingMatrix.set(originalMatrix)
                    workingMatrix.setScale(kMatrixScaleFactor, kMatrixScaleFactor, event.x, event.y)
                    invalidate()
                } else {
                    working = false
                    Log.v(TAG, "Orig ${event.x},${event.y}")

                    if (gameDelegate?.isSearching()!!) return false

                    // Initialize the array with our Coordinate
                    val pts: FloatArray = floatArrayOf(event.x, event.y)

                    if (zoom) {
                        // Use the Matrix to map the points
                        val inverseCopy = Matrix()
                        if (workingMatrix.invert(inverseCopy)) {
                            inverseCopy.mapPoints(pts)
                            //Now transformedPoint is reverted to original state.
                        }
                    }

                    // Clip event coordinates to be max step/2 from board edges
                    if (pts[0] <= limitLow || pts[0] >= limitHigh || pts[1] <= limitLow || pts[1] >= limitHigh) {
                        invalidate()
                        return false
                    }

                    lastMove = coordinates2Move(pts[0], pts[1])

                    if (!(gameDelegate!!.canMakeMove(lastMove))) {
                        invalidate()
                        return false
                    }
                    Log.d(TAG, "Down ${lastMove.x},${lastMove.y}")
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!zoom || !working) {
                    gameDelegate!!.makeMove(lastMove)
                    performClick()
                }
            }
        }
        return true
    }

    private fun drawStones(canvas: Canvas) {
        val safeDelegate = gameDelegate ?: return
        val oldColor = paint.color

        for (i in 0 until (safeDelegate.moveCount())) {
            val p = move2Point(safeDelegate.getIthMove(i))
            paint.color = if (i % 2 != 0) Color.WHITE else Color.DKGRAY
            canvas.drawCircle(p.x, p.y, step * kStoneCoef, paint)
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
        drawHorizontalLines(canvas)
        drawVerticalLines(canvas)
        drawFrame(canvas)
        drawCoordinates(canvas)
        drawHandicapPoints(canvas)
    }

    private fun drawHorizontalLines(canvas: Canvas) {
        for (i in 0..kStepCount) {
            canvas.drawLine(
                offset,
                offset + i * step,
                offset + kStepCount * step,
                offset + i * step,
                paint
            )
        }
    }

    private fun drawVerticalLines(canvas: Canvas) {
        for (i in 0..kStepCount) {
            canvas.drawLine(
                offset + i * step,
                offset,
                offset + i * step,
                offset + kStepCount * step,
                paint
            )
        }
    }

    private fun drawFrame(canvas: Canvas) {
        canvas.drawLine(
            offset - 1, offset - 1, offset + 1 + kStepCount * step, offset - 1, paint
        )
        canvas.drawLine(
            offset - 1, offset - 1, offset - 1, offset + 1 + kStepCount * step, paint
        )
        canvas.drawLine(
            offset - 1,
            offset + 1 + kStepCount * step,
            offset + 1 + kStepCount * step,
            offset + 1 + kStepCount * step,
            paint
        )
        canvas.drawLine(
            offset + 1 + kStepCount * step,
            offset - 1,
            offset + 1 + kStepCount * step,
            offset + 1 + kStepCount * step,
            paint
        )
    }

    private fun drawCoordinates(canvas: Canvas) {
        if (showCoordinates) {
            for (i in 0..kStepCount) {
                canvas.drawText(
                    (i + 'A'.code).toChar().toString(),
                    offset + i * step,
                    offset * 0.5f,
                    paint
                )
                canvas.drawText(
                    (kBoardSize - i).toString(),
                    offset * 0.33f,
                    offset + i * step + paint.textSize * 0.33f,
                    paint
                )
            }
        }
    }

    private fun drawHandicapPoints(canvas: Canvas) {
        if (kBoardSize >= 11) {
            var p = move2Point(Move(kHandicapOffset, kHandicapOffset))
            canvas.drawCircle(p.x, p.y, step * kHandicapDrawCoef, paint)
            p = move2Point(Move(kBoardSize - kHandicapOffset - 1, kHandicapOffset))
            canvas.drawCircle(p.x, p.y, step * kHandicapDrawCoef, paint)
            p = move2Point(Move(kHandicapOffset, kBoardSize - kHandicapOffset - 1))
            canvas.drawCircle(p.x, p.y, step * kHandicapDrawCoef, paint)
            p = move2Point(Move(kBoardSize - kHandicapOffset - 1, kBoardSize - kHandicapOffset - 1))
            canvas.drawCircle(p.x, p.y, step * kHandicapDrawCoef, paint)
            if (kBoardSize % 2 != 0) {
                p = move2Point(Move(kBoardSize / 2, kBoardSize / 2))
                canvas.drawCircle(p.x, p.y, step * kHandicapDrawCoef, paint)
            }
        }
    }

    private fun coordinates2Move(x: Float, y: Float): Move {
        return Move(
            ((x - offset + step / 2) / step).toInt(),
            kStepCount - ((y - offset + step / 2) / step).toInt()
        )
    }

    private fun move2Point(move: Move): PointF {
        return PointF(offset + move.x * step, offset + (kStepCount - move.y) * step)
    }

    fun recalcLimits() {
        Log.v(TAG, "Reca")
        val defaultDimension = context.getString(R.string.default_board).toInt()
        kBoardSize =
            sharedPreferences.getString("list_preference_board_size", defaultDimension.toString())
                ?.toInt()
                ?: defaultDimension
        kStepCount = kBoardSize - 1
        showNumbers = sharedPreferences.getBoolean("check_box_preference_numbers", true)
        showCoordinates = sharedPreferences.getBoolean("check_box_preference_coordinates", true)
        zoom = sharedPreferences.getBoolean("check_box_preference_zoom", false)

        if (showCoordinates) {
            offset = 0.08f * width
            step = (width - 1.6f * offset) / kStepCount
        } else {
            offset = 0.05f * width
            step = (width - 2.0f * offset) / kStepCount
        }
        limitLow = offset - step * 0.5f
        limitHigh = offset + kStepCount * step + step * 0.5f
        paint.textSize = step * 0.5f
    }

    override fun performClick(): Boolean {
        run { } // to silent checker
        return super.performClick()
    }
}

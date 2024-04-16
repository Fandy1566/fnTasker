package com.reee.fntasker.utils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.pow

class TouchAndDragListener(
    private val layoutParams: WindowManager.LayoutParams,
    private val startDragDistance: Int,
    private val onClick: Runnable?,
    private val onDrag: Runnable? = null
) : View.OnTouchListener {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDrag = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                isDrag = false
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDrag && onClick != null) {
                    onClick.run()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDrag && isDragging(event)) {
                    isDrag = true
                }
                if (!isDrag) return true
                layoutParams.x = (initialX + (event.rawX - initialTouchX)).toInt()
                layoutParams.y = (initialY + (event.rawY - initialTouchY)).toInt()
                onDrag?.run()
                return true
            }
        }
        return false
    }

    private fun isDragging(event: MotionEvent): Boolean {
        return ((event.rawX - initialTouchX).toDouble().pow(2.0)
                + (event.rawY - initialTouchY).toDouble().pow(2.0)) > startDragDistance * startDragDistance
    }
}

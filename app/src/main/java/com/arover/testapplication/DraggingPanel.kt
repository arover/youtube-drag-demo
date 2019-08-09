package com.arover.testapplication

/**
 * @author arover
 * created at 2019-08-09 00:50
 */
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper

class DraggingPanel(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    public interface PositionChangedListener{
        fun onPositionChanged(left: Int, top: Int, dx: Int, dy: Int)
    }

    private val TAG: String = "DraggingPanel"
    private val AUTO_OPEN_SPEED_LIMIT = 800.0
    private var mDraggingState = 0
    private var mQueenButton: LinearLayout? = null
    private var mDragHelper: ViewDragHelper? = null
    private var mDraggingBorder: Int = 0
    private var mVerticalRange: Int = 0
    var isOpen: Boolean = false
        private set

    val isMoving: Boolean
        get() = mDraggingState == ViewDragHelper.STATE_DRAGGING || mDraggingState == ViewDragHelper.STATE_SETTLING


    inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun onViewDragStateChanged(state: Int) {
            if (state == mDraggingState) { // no change
                return
            }
            if ((mDraggingState == ViewDragHelper.STATE_DRAGGING || mDraggingState == ViewDragHelper.STATE_SETTLING) && state == ViewDragHelper.STATE_IDLE) {
                // the view stopped from moving.

                if (mDraggingBorder == 0) {
                    onStopDraggingToClosed()
                } else if (mDraggingBorder == mVerticalRange) {
                    isOpen = true
                }
            }
            if (state == ViewDragHelper.STATE_DRAGGING) {
                onStartDragging()
            }
            mDraggingState = state
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            Log.d(TAG,"onViewPositionChanged left=$left,top=$top, dx=$dx, dy=$dy changedView=${changedView.id}")
            mDraggingBorder = top
            if(changedView is PositionChangedListener){
                changedView.onPositionChanged(left, top, dx,dy)
            }
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return mVerticalRange
        }

        override fun tryCaptureView(view: View, i: Int): Boolean {
//            return view.id == R.id.mainLayout
            return true
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val topBound = paddingTop
            val bottomBound = mVerticalRange
            return Math.min(Math.max(top, topBound), bottomBound)
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            Log.d(TAG,"onViewPositionChanged xvel=$xvel,yvel=$yvel")

            val rangeToCheck = mVerticalRange.toFloat()
            if (mDraggingBorder == 0) {
                isOpen = false
                return
            }
            if (mDraggingBorder.toFloat() == rangeToCheck) {
                isOpen = true
                return
            }
            var settleToOpen = false
            if (yvel > AUTO_OPEN_SPEED_LIMIT) { // speed has priority over position
                settleToOpen = true
            } else if (yvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false
            } else if (mDraggingBorder > rangeToCheck / 2) {
                settleToOpen = true
            } else if (mDraggingBorder < rangeToCheck / 2) {
                settleToOpen = false
            }

            val settleDestY = if (settleToOpen) mVerticalRange else 0

            if (mDragHelper!!.settleCapturedViewAt(0, settleDestY)) {
                ViewCompat.postInvalidateOnAnimation(this@DraggingPanel)
            }
        }
    }

    init {
        isOpen = false
    }

    override fun onFinishInflate() {
//        mQueenButton = findViewById(R.id.queenButton)
        mDragHelper = ViewDragHelper.create(this, 1.0f, DragHelperCallback())
        isOpen = false
        super.onFinishInflate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(TAG,"onSizeChanged w=$w,top=$h, oldw=$oldw, oldh=$oldh")
        mVerticalRange = (h * 0.84).toInt()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun onStopDraggingToClosed() {
        // To be implemented
    }

    private fun onStartDragging() {

    }

    private fun isQueenTarget(event: MotionEvent): Boolean {
        val queenLocation = IntArray(2)
        mQueenButton!!.getLocationOnScreen(queenLocation)
        val upperLimit = queenLocation[1] + mQueenButton!!.measuredHeight
        val lowerLimit = queenLocation[1]
        val y = event.rawY.toInt()
        return y > lowerLimit && y < upperLimit
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isQueenTarget(event) && mDragHelper!!.shouldInterceptTouchEvent(event)) {
            true
        } else {
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isQueenTarget(event) || isMoving) {
            mDragHelper!!.processTouchEvent(event)
            return true
        } else {
            return super.onTouchEvent(event)
        }
    }

    override fun computeScroll() { // needed for automatic settling.
        if (mDragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }
}

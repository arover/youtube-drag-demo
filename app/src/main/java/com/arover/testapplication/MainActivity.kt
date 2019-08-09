package com.arover.testapplication

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSizes()
    }

    private var miniBottom: Int = 0
    private var miniPhase2Top: Int = 0
    private var miniTop: Int = 0
    var windowRight: Int = 0
    var windowBottom: Int = 0
    var bottomMargin: Int = 0
    var miniHeight: Int = 0
    var miniWidth: Int = 0
    var phase2Height: Int = 0
    var normalHeight: Int = 0


    private fun initSizes() {

        windowRight = resources.displayMetrics.widthPixels
        windowBottom = resources.displayMetrics.heightPixels

        bottomMargin = resources.getDimensionPixelSize(R.dimen.float_window_bottom_margin)
        miniHeight = resources.getDimensionPixelSize(R.dimen.mini_height)
        miniWidth = resources.getDimensionPixelSize(R.dimen.mini_width)
        phase2Height = resources.getDimensionPixelSize(R.dimen.mini_phase2_height)
        normalHeight = resources.getDimensionPixelSize(R.dimen.normal_height)
        miniTop = windowBottom - bottomMargin - miniHeight
        miniPhase2Top = windowBottom - bottomMargin - phase2Height
        miniBottom = windowBottom - bottomMargin
    }

    private val STYLE_PORTRAINT_FS: Int = 1
    private var style: Int = STYLE_PORTRAINT_FS
    private val STYLE_MINI: Int = 2

    var touchDownX: Int = 0
    var touchDownY: Int = 0

    override fun onStart() {
        super.onStart()
        videoView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                Log.v(TAG, "onTouch $event")
                if (event.action == MotionEvent.ACTION_DOWN) {
                    touchDownX = event.rawX.toInt()
                    touchDownY = event.rawY.toInt()
                    p2HeightY = 0
                    Log.d(TAG, "touchDownX=$touchDownX,touchDownY=$touchDownY")
                    return true
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    if (touchDownX == 0 || touchDownY == 0) return false
                    draggingFloatWindow(event)
                    if (style == STYLE_PORTRAINT_FS) {
                        resizeVideoView(event)
                        fadeInOutContent(event)
                        showOtherView(event)
                    } else if (style == STYLE_MINI) {
                        resizeVideoView(event)
                    } else {

                    }
                    return true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                    touchDownX = 0
                    touchDownY = 0
                    return true
                } else {
                    return true
                }
            }
        })

    }

    private fun showOtherView(event: MotionEvent) {

    }

    private fun fadeInOutContent(event: MotionEvent) {

    }

    private fun resizeVideoView(event: MotionEvent) {

    }

    var p2HeightY = 0
    private fun draggingFloatWindow(event: MotionEvent) {
        val yDistance = (windowBottom - bottomMargin - miniHeight - touchDownY) * 1.0f
        val y2Distance = (windowBottom - bottomMargin - phase2Height - touchDownY) * 1.0f
        //y轴移动距离
        var ym = (event.rawY - touchDownY).toInt()
        if (event.rawY < touchDownY) {
            ym = 0
        }
        var bottom: Int = windowBottom - (ym / yDistance * bottomMargin).toInt()

        Log.d(TAG, "fw top=$ym, event.rawY=${event.rawY},bottom=$bottom,windowRight=$windowRight")

        if (ym >= miniPhase2Top && p2HeightY == 0) {
            p2HeightY = event.rawY.toInt()
        }

        if (ym >= miniTop) {
            ym = miniTop
        }

        if (bottom <= miniBottom) {
            bottom = miniBottom
        }

        floatWindow.layout(0, ym, windowRight, bottom)

        if (style == STYLE_PORTRAINT_FS && event.rawY > touchDownY) {
            var yDelta = if (event.rawY - touchDownY < 0) {
                0
            } else {
                (event.rawY - touchDownY).toInt()
            }
//            yDelta += (yDelta * 0.382 * yDelta / yDistance).toInt()
            var mph = if (yDelta <= 0) {
                normalHeight
            } else {
                normalHeight - (yDelta / y2Distance * (normalHeight - phase2Height)).toInt()
            }
            if (mph < miniPhase2Top) {
                videoView.layout(0, 0, windowRight, mph)
                contentView.layout(0, mph, windowRight, bottom)
            }

            if (p2HeightY != 0) {
                var ym2 = (event.rawY - p2HeightY)
                if (ym2 < 0) {
                    return
                }
                var r =
                    (windowRight - (ym2 / (phase2Height - miniHeight)) * (windowRight - miniWidth)).toInt()
                var bb = (phase2Height - ym2).toInt()
                if (bb < miniHeight) {
                    bb = miniHeight
                }
                if (r < miniWidth) {
                    r = miniWidth
                }
                videoView.layout(0, 0, r, bb)
                contentView.layout(0, bb, windowRight, bottom)
            }
        }
    }

    companion object {
        const val TAG = "main"
    }
}

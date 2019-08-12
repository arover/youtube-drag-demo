package com.arover.testapplication

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private var phase3UpRight: Int = 0
    private var touchDownRawY: Int = 0
    private var floatWindowTop: Int = 0
    private var miniSideMargin: Int = 0
    private var statusBarHeight: Int = 0
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

        statusBarHeight = getStatusBarHeight()

        bottomMargin = resources.getDimensionPixelSize(R.dimen.float_window_bottom_margin)
        miniHeight = resources.getDimensionPixelSize(R.dimen.mini_height)
        miniWidth = resources.getDimensionPixelSize(R.dimen.mini_width)
        miniSideMargin = resources.getDimensionPixelSize(R.dimen.mini_side_margin)
        phase2Height = resources.getDimensionPixelSize(R.dimen.mini_phase2_height)
        normalHeight = resources.getDimensionPixelSize(R.dimen.normal_height)
        //relative position
        miniTop = windowBottom - bottomMargin - miniHeight - statusBarHeight
        miniPhase2Top = windowBottom - bottomMargin - phase2Height- statusBarHeight
        miniBottom = windowBottom - bottomMargin - statusBarHeight
        phase3UpRight = miniWidth
        Log.i(TAG,"windowRight=$windowRight,windowBottom=$windowBottom, bottomMargin=$bottomMargin")
        Log.i(TAG,"miniTop=$miniTop,miniPhase2Top=$miniPhase2Top,miniBottom=$miniBottom, statusBarHeight= $statusBarHeight")
    }

    private val STYLE_PORTRAINT_FS: Int = 1
    private var style: Int = STYLE_PORTRAINT_FS
    private val STYLE_MINI: Int = 2

    var touchDownX: Int = 0
    var touchDownY: Int = 0

    override fun onStart() {
        super.onStart()
        initSizes()
        contentView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touchDownX = event.x.toInt()
                touchDownY = event.x.toInt()
                phase2FingerY = 0
                Log.d(TAG, "contentView touchDownX=$touchDownX,touchDownY=$touchDownY")
            }
            false

        }


        videoView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                Log.v(TAG, "onTouch $event")
                if (event.action == MotionEvent.ACTION_DOWN) {
                    touchDownX = event.x.toInt()
                    touchDownY = event.y.toInt()
                    touchDownRawY = event.rawY.toInt()
                    phase2FingerY = 0
                    floatWindowTop = floatWindow.top
                    if(floatWindowTop != 0){
                        style = STYLE_MINI
                    } else {
                        style = STYLE_PORTRAINT_FS
                    }
                    Log.d(TAG, "ACTION_DOWN y=${event.y},rawY=$touchDownRawY, floatWindowTop=$floatWindowTop")
                    return true
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    draggingFloatWindow(event)
                    return true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                    phase2FingerY = 0
                    if(floatWindow.top in miniTop+statusBarHeight-2..miniTop+statusBarHeight-2){
                        style = STYLE_MINI
                    } else {
                        style = STYLE_PORTRAINT_FS
                    }
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

    var phase2FingerY = 0

    private fun draggingFloatWindow(event: MotionEvent) {
        // 迷你窗口top等于y轴滑动距离
        var top = floatWindowTop + (event.rawY - touchDownRawY).toInt()

        //不能为负，否则滑出顶部边界。
        if (top <= 0) {
            top = 0
        }
        if(top > miniTop){
            top = miniTop
        }
        Log.i(TAG,"top=$top") //,miniTop=$miniTop,miniPhase2Top=$miniPhase2Top")
        //处于滑动第一阶段时， 记录滑道第二阶段时手指Y坐标
        if (style == STYLE_PORTRAINT_FS && top in miniPhase2Top until miniTop && phase2FingerY == 0) {
            Log.i(TAG,"!!! SET phase2FingerY")
            phase2FingerY = event.rawY.toInt()
        }
        //top处于第一阶段和第三阶段的y轴范围时， 清除记录的手指Y坐标
//        if (phase2FingerY != 0 && (top < miniPhase2Top || top >= miniTop)){
//            Log.i(TAG,"!!! RESET phase2FingerY")
//            phase2FingerY = 0
//        }
        //floatwindow 滑动范围以top来定为(1，miniTop)
        if (top in 0..miniTop) {
//            Log.d(TAG, "top <= miniTop top=$top")
            var bottom: Int = windowBottom  - (top / (miniTop*1.0f) * bottomMargin).toInt() - statusBarHeight
            var sideMargin:Int =  (top / (miniTop*1.0f) * miniSideMargin).toInt()
//            Log.d(TAG, "top=$top, rawY=${event.rawY},bottom=$bottom")
            if (bottom <= miniBottom) {
//                Log.i(TAG, "bottom <= miniBottom bottom=$bottom")
                bottom = miniBottom
            }
            val right = windowRight - sideMargin
            Log.d(TAG, "phase1 top=$top, right=$right, bottom=$bottom")
            floatWindow.layout(sideMargin, top, right, bottom)
        }

        //滑动处于第一阶段，即视频宽度还是等于全窗口宽度时，那么视频高度随滑动距离变化
        if(top < miniPhase2Top) {
//            pauseBtn.layout(0,0,0,0)
//            closeBtn.layout(0,0,0,0)

//            yDelta += (yDelta * 0.382 * yDelta / yDistance).toInt()
            val y2Distance = (windowBottom - bottomMargin - phase2Height - touchDownY) * 1.0f
            var factor = 0F
            val phase2Bottom = if (top <= 0) {
                normalHeight
            } else {
                factor = top / y2Distance
                normalHeight - (factor * (normalHeight - phase2Height)).toInt()

            }
            Log.d(TAG, "phase2 y2Distance=$y2Distance, phase2Bottom=$phase2Bottom")
            videoView.layout(0, 0, floatWindow.width, phase2Bottom)
            contentView.layout(0, phase2Bottom, floatWindow.width, floatWindow.bottom)

            var alpha = 1 - factor
            if(alpha < 0 ) {
                alpha = 0F
            }
            contentView.alpha = alpha
        }
        // 滑动处于第二阶段，即top在阶段二和最终阶段的范围内，那么视频高度和宽度都随滑动距离变化
        if (top in miniPhase2Top..miniTop) {
            //往下滑时，记录的有到达第二阶段的y值
            if(phase2FingerY != 0) {

                val ydis = event.rawY - phase2FingerY // windowBottom - bottomMargin - miniHeight

//                Log.d(TAG, "phase3 ydis=$ydis, fy=$phase2FingerY, tdy=$touchDownY")

                var phase3Right = (windowRight - (ydis / (phase2Height - miniHeight)) * (windowRight - miniWidth)).toInt()
                var phase3Bottom = (phase2Height - ydis).toInt()
                if (phase3Bottom < miniHeight) {
                    phase3Bottom = miniHeight
                }
                if (phase3Right < miniWidth) {
                    phase3Right = miniWidth
                }
                Log.d(TAG, "phase3 DOWN right=$phase3Right, bottom=$phase3Bottom")
                videoView.layout(0, 0, phase3Right, phase3Bottom)
                contentView.layout(0, phase3Bottom, windowRight, floatWindow.bottom)
            }
        }

        //小窗口状态时往上滑动
        if( style == STYLE_MINI && top > miniPhase2Top){
            val ydis = event.rawY - touchDownRawY
            if(ydis > 0){ //往下滑不动
                Log.d(TAG,"video r=${videoView.right}, b=${videoView.bottom}")
                videoView.layout(0, 0, phase3UpRight, miniHeight)
                return
            }
//            Log.d(TAG, "phase3 up,ydis=$ydis, tdy=$touchDownRawY")

            phase3UpRight = (miniWidth - (ydis / (phase2Height - miniHeight)) * (windowRight - miniWidth)).toInt()
            var phase3UpBottom = (miniHeight - ydis).toInt()
            if (phase3UpBottom < miniHeight) {
                phase3UpBottom = miniHeight
            }

            if (phase3UpRight > floatWindow.right) {
                phase3UpRight = floatWindow.right
            }
            Log.d(TAG, "phase3 UP right=$phase3UpRight, bottom=$phase3UpBottom")
            videoView.layout(0, 0, phase3UpRight, phase3UpBottom)
            contentView.layout(0, phase3UpBottom, windowRight, floatWindow.bottom)
        }
    }

    companion object {
        const val TAG = "main"
    }
}

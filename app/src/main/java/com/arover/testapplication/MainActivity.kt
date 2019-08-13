package com.arover.testapplication

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Math.abs


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
        mainContentBtn.setOnClickListener {
            restorePortraitFullscreenMode()
        }
    }



    private var fingerUpY: Int = 0
    private var resetThreshold: Int = 0
    private var phase3UpRight: Int = 0
    private var touchDownRawY: Int = 0
    private var floatWindowTop: Int = 0
    private var miniSideMargin: Int = 0
    private var statusBarHeight: Int = 0
    private var miniBottom: Int = 0
    private var miniPhase2Top: Int = 0
    private var miniTop: Int = 0
    private var windowRight: Int = 0
    private var windowBottom: Int = 0
    private var videoViewNormalHeight: Int = 0
    private var bottomMargin: Int = 0
    private var miniHeight: Int = 0
    private var miniWidth: Int = 0
    private var phase2Height: Int = 0
    private var style: Int = STYLE_PORTRAIT_FULL_SIZE
    private var touchDownX: Int = 0
    private var touchDownY: Int = 0
    private fun initSizes() {

        windowRight = resources.displayMetrics.widthPixels
        windowBottom = resources.displayMetrics.heightPixels

        statusBarHeight = getStatusBarHeight()

        bottomMargin = resources.getDimensionPixelSize(R.dimen.float_window_bottom_margin)
        miniHeight = resources.getDimensionPixelSize(R.dimen.mini_height)
        miniWidth = resources.getDimensionPixelSize(R.dimen.mini_width)
        miniSideMargin = resources.getDimensionPixelSize(R.dimen.mini_side_margin)
        phase2Height = resources.getDimensionPixelSize(R.dimen.mini_phase2_height)
        videoViewNormalHeight = resources.getDimensionPixelSize(R.dimen.normal_height)
        resetThreshold = resources.getDimensionPixelSize(R.dimen.float_window_reset_pos_thresold)
        //relative position
        miniTop = windowBottom - bottomMargin - miniHeight - statusBarHeight
        miniPhase2Top = windowBottom - bottomMargin - phase2Height- statusBarHeight
        miniBottom = windowBottom - bottomMargin - statusBarHeight
        phase3UpRight = miniWidth
        Log.i(TAG,"windowRight=$windowRight,windowBottom=$windowBottom, bottomMargin=$bottomMargin")
        Log.i(TAG,"miniTop=$miniTop,miniPhase2Top=$miniPhase2Top,miniBottom=$miniBottom, statusBarHeight= $statusBarHeight")
    }



    override fun onStart() {
        super.onStart()
        initSizes()
        // contentView.setOnTouchListener 


        videoView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                Log.v(TAG, "onTouch $event")
                if (event.action == MotionEvent.ACTION_DOWN) {
                    touchDownX = event.x.toInt()
                    touchDownY = event.y.toInt()
                    touchDownRawY = event.rawY.toInt()
                    phase2FingerY = 0
                    floatWindowTop = floatWindow.top
                    style = if(floatWindowTop != 0){
                        STYLE_MINI
                    } else {
                        STYLE_PORTRAIT_FULL_SIZE
                    }
                    Log.d(TAG, "ACTION_DOWN y=${event.y},rawY=$touchDownRawY, floatWindowTop=$floatWindowTop")
                    return true
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    draggingFloatWindow(event.rawY)
                    return true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                    animateToState(floatWindow.top)
                    return true
                } else {
                    return true
                }
            }
        })

    }

    private fun animateToState(top: Int){
        if(style == STYLE_PORTRAIT_FULL_SIZE && top == miniTop){
            style = STYLE_MINI
            phase2FingerY = 0
            return
        }

        val fingerY = top + statusBarHeight
        fingerUpY = fingerY
        //目标top位置，屏幕绝对Y坐标
        var targetStyle = STYLE_MINI
        val destTop = if(style == STYLE_PORTRAIT_FULL_SIZE){
             if( top < resetThreshold) {
                 targetStyle = STYLE_PORTRAIT_FULL_SIZE
                 0
             } else {
                 Log.d(TAG,"animateToState mini ")
                 targetStyle = STYLE_MINI
                 miniTop + touchDownRawY
             }
        } else {
            //迷你状态下往上滑超过阈值
            if(top < miniTop - resetThreshold){
                Log.d(TAG,"animateToState portrait fullscreen")
                targetStyle = STYLE_PORTRAIT_FULL_SIZE
                0
            //迷你状态下往下滑超过阈值
            } else if(top > miniTop + miniHeight/2 ){
                Log.d(TAG,"animateToState fadeout down")
                targetStyle = STYLE_GONE
                (miniTop + miniHeight * FADE_HEIGHT_FACTOR).toInt()
            } else {
            //迷你状态下往上滑未超过阈值
                targetStyle = STYLE_MINI
                miniTop
            }
        }
        //以滑距离比默认要滑动距离，
        val resetAnimDuration = abs(fingerY - destTop)/miniTop.toFloat()* FULL_RANGE_ANIM_TIME

        Log.d(TAG, "animateToState top=$top, miniTop=$miniTop, range[$fingerY,$destTop], duration=$resetAnimDuration,style=$style")

        if(resetAnimDuration < 60){
            phase2FingerY = 0
            if(targetStyle == STYLE_PORTRAIT_FULL_SIZE){
                Log.d(TAG,"animateToState reset STYLE_PORTRAIT_FS")
                restorePortraitFullscreenMode()
            } else if(targetStyle == STYLE_MINI){
                Log.d(TAG,"animateToState reset STYLE_MINI")
                displayAsMiniWindow()
            } else {
                Log.d(TAG,"animateToState reset STYLE_GONE")
                displayAsDisappeared()
            }
        } else {
            floatWindowTop = floatWindow.top
            Log.d(TAG, "animateToState start floatWindowTop=$floatWindowTop")
            ValueAnimator.ofInt(fingerY, destTop)
            .apply {
                interpolator  = AccelerateDecelerateInterpolator()
                setEvaluator(IntEvaluator())
                duration = resetAnimDuration.toLong()
                addUpdateListener {
                    if(it.animatedValue != 0){
                        draggingFloatWindow(it.animatedValue as Int * 1.0f)
                    }
                    if(it.animatedValue == destTop){
                        floatWindowTop = 0
                        phase2FingerY = 0
                        if(destTop == 0){
                            style = STYLE_PORTRAIT_FULL_SIZE
                        } else if(floatWindow.top == miniTop){
                            style = STYLE_MINI
                        } else {
                            style = STYLE_GONE
                            displayAsDisappeared()
                        }
                        Log.d(TAG, "animateToState style=$style")
                    }
                }
                start()
            }
        }
    }

    private fun displayAsMiniWindow(){
        style = STYLE_MINI
        floatWindow.alpha = 1F
        floatWindow.layout(miniSideMargin, miniTop, windowRight - miniSideMargin, miniBottom)
        videoView.layout(0, 0, miniWidth, floatWindow.bottom)
        contentView.layout(0,0,0,0)
        contentView.alpha = 0F
    }
    private fun displayAsDisappeared(){
        style = STYLE_GONE
        floatWindow.visibility = View.GONE
        floatWindow.layout(0, 0, windowRight, windowBottom - statusBarHeight)
        videoView.layout(0,0, windowRight, videoViewNormalHeight)
        contentView.layout(0, videoView.bottom, windowRight, floatWindow.bottom)
        contentView.alpha = 1.0F
    }
    private fun restorePortraitFullscreenMode(){
        style = STYLE_PORTRAIT_FULL_SIZE
        floatWindow.layout(0, 0, windowRight, windowBottom - statusBarHeight)
        videoView.layout(0,0, windowRight, videoViewNormalHeight)
        contentView.layout(0, videoView.bottom, windowRight, floatWindow.bottom)
        contentView.alpha = 1.0F
        floatWindow.visibility = View.VISIBLE
        floatWindow.alpha = 1.0F
    }

    var phase2FingerY = 0

    private fun draggingFloatWindow(fingerY: Float) {
        // 迷你窗口top等于y轴滑动距离
        var top = floatWindowTop + (fingerY - touchDownRawY).toInt()

        //不能为负，否则滑出顶部边界。
        if (top <= 0) {
            top = 0
        }

        if(style == STYLE_PORTRAIT_FULL_SIZE && top > miniTop){
            top = miniTop
        }

        Log.i(TAG,"top=$top") //,miniTop=$miniTop,miniPhase2Top=$miniPhase2Top")
        //处于滑动第一阶段时， 记录滑道第二阶段时手指Y坐标
        if (style == STYLE_PORTRAIT_FULL_SIZE && top in miniPhase2Top until miniTop && phase2FingerY == 0) {
            Log.i(TAG,"!!! SET phase2FingerY")
            phase2FingerY = fingerY.toInt()
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
//            Log.d(TAG, "top=$top, rawY=${fingerY},bottom=$bottom")
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

            val y2Distance = (windowBottom - bottomMargin - phase2Height - touchDownY) * 1.0f
            var factor = 0F
            val phase2Bottom = if (top <= 0) {
                videoViewNormalHeight
            } else {
                factor = top / y2Distance
                videoViewNormalHeight - (factor * (videoViewNormalHeight - phase2Height)).toInt()

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

                val ydis = fingerY - phase2FingerY // windowBottom - bottomMargin - miniHeight

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
        if( style == STYLE_MINI && top > miniPhase2Top && top <= miniTop){
            val ydis = fingerY - touchDownRawY
            if(ydis > 0){ //往下滑不动
                Log.d(TAG,"video r=${videoView.right}, b=${videoView.bottom}")
                videoView.layout(0, 0, phase3UpRight, miniHeight)
                return
            }

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

            //fadeout down 下滑渐隐
        } else if(style == STYLE_MINI
                && top >= miniTop // 往下滑
                && top < miniTop + miniHeight * FADE_HEIGHT_FACTOR) { //未超过最大Y值
            val bottom = top + miniHeight
            val alpha = 1 - (fingerY - touchDownRawY) / (miniHeight * FADE_HEIGHT_FACTOR)
            if (alpha >= 0) {
                Log.d(TAG, "phase4 top=$top, bottom=$bottom,alpha = $alpha")
                floatWindow.alpha = alpha
                if (alpha <= 0.2F) {
                    floatWindow.visibility = View.GONE
                } else {
                    floatWindow.layout(miniSideMargin, top, windowRight - miniSideMargin, bottom)
                    if (top > miniTop)
                        videoView.layout(0, 0, miniWidth, floatWindow.bottom)
                }
            }
        }
    }

    companion object {
        const val TAG = "main"
        private const val STYLE_PORTRAIT_FULL_SIZE: Int = 1
        private const val STYLE_MINI: Int = 2
        private const val STYLE_GONE: Int = 3
        private const val FULL_RANGE_ANIM_TIME =350
        private const val FADE_HEIGHT_FACTOR: Float = 1.5f
    }
}

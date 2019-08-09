package com.arover.testapplication

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 *
 * @author arover
 * created at 2019-08-09 01:40
 */
class Panel : LinearLayout, DraggingPanel.PositionChangedListener {
    var listener: DraggingPanel.PositionChangedListener? = null
    override fun onPositionChanged(left: Int, top: Int, dx: Int, dy: Int) {
        listener?.onPositionChanged(left, top , dx, dy)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, style: Int) : super(context, attrs, defStyleAttr, style)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr, 0 )
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0, 0 )
    constructor(context: Context) : super(context, null,0, 0)
}
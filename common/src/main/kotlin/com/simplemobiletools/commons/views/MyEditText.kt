package com.simplemobiletools.commons.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.EditText
import com.simplemobiletools.commons.R


class MyEditText : androidx.appcompat.widget.AppCompatEditText {
    var mContext : Context? = null

    constructor(context: Context) : super(context){
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        mContext = context

    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle){
        mContext = context
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        val drawableRight = mContext?.resources?.getDrawable(R.drawable.ic_cross_vector_black)
        super.setCompoundDrawables(null, null, drawableRight, null)
    }

//    setOnTouchListener(View.OnTouchListener { v, event ->
//
//        val DRAWABLE_RIGHT = 2
//        if (event.action == MotionEvent.ACTION_UP) {
//            if (event.rawX >= right - compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
//                clear()
//                return@OnTouchListener true
//            }
//        } else {
//            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
//        }
//        false
//    })
}

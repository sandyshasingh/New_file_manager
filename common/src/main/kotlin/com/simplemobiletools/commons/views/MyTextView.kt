package com.simplemobiletools.commons.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class MyTextView : androidx.appcompat.widget.AppCompatTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

}

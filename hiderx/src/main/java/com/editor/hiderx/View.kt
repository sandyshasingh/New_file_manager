package com.editor.hiderx

import android.view.View

fun View.doVisible() {
    if (!isVisible())
        visibility = View.VISIBLE
}

fun View.doGone() {
    if (isVisible())
        visibility = View.GONE
}


fun View.isVisible() : Boolean
{
    return visibility == View.VISIBLE
}

fun View.doInvisibleIf(boolean: Boolean)
{
    if(boolean)
        doInvisible()
    else
        doVisible()
}

fun View.doInvisible() {
    if (isVisible())
        visibility = View.INVISIBLE
}
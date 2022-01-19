package com.simplemobiletools.commons

import android.view.View
import androidx.core.view.isVisible

fun View.doInVisible()
{
    if(isVisible())
    {
        visibility = View.INVISIBLE
    }
}

fun View.doVisible()
{
    if(!isVisible())
    {
        visibility = View.VISIBLE
    }
}

fun View.doGone()
{
    if(isVisible())
    {
        visibility = View.GONE
    }
}


fun View.isVisible() : Boolean
{
    return this.visibility == View.VISIBLE
}
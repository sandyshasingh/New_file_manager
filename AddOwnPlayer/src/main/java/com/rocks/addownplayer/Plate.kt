package com.rocks.addownplayer

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.util.*
class Plate() {
    companion object {
        fun getColor(palette: Palette?, isDark: Boolean): Int? {
            if (palette != null) {
                when {

                    palette.darkVibrantSwatch != null && isDark -> {
                        return palette.darkVibrantSwatch?.rgb
                    }
                    palette.darkMutedSwatch != null  && isDark -> {
                        return palette.darkMutedSwatch?.rgb
                    }
                    palette.lightVibrantSwatch != null  && isDark.not()-> {
                        return palette.lightVibrantSwatch?.rgb
                    }
                    palette.lightMutedSwatch != null && isDark.not() -> {
                        return palette.lightMutedSwatch?.rgb
                    }
                    palette.swatches.isNotEmpty() -> {
                        return Collections.max(palette.swatches,SwatchComparator.instance).rgb
                    }
                }
            }
            return null
        }
    }
}
class SwatchComparator : Comparator<Swatch?> {
    companion object {
        private var sInstance: SwatchComparator? = null
        val instance: SwatchComparator?
            get() {
                if (sInstance == null) {
                    sInstance = SwatchComparator()
                }
                return sInstance
            }
    }
    override fun compare(o1: Swatch?, o2: Swatch?): Int {
        return o1?.population?.minus(o2?.population!!)!!
    }
}

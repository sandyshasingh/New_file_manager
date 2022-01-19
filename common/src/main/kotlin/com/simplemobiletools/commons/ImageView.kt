package com.simplemobiletools.commons

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File


fun ImageView.loadUriForPhotoViewer(string: String?){
    val requestOptions = RequestOptions().placeholder(R.color.black)
    Glide.with(this.context).asBitmap().optionalCenterCrop()
        .load(Uri.fromFile(File(string!!))).thumbnail(0.04f).apply(requestOptions)
        .into(this)
}
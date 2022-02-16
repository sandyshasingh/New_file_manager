package com.editor.hiderx

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

fun ImageView.loadUriFromPath(string: String?){
    val requestOptions = RequestOptions().placeholder(R.drawable.place_holder)
    Glide.with(this.context).asBitmap().optionalCenterCrop()
        .load(Uri.fromFile(File(string!!))).thumbnail(0.04f).apply(requestOptions)
        .into(this)
}

fun ImageView.loadUriForPhotoViewer(string: String?){
    val requestOptions = RequestOptions().placeholder(R.color.black)
    Glide.with(this.context).asBitmap().optionalCenterCrop()
        .load(Uri.fromFile(File(string!!))).thumbnail(0.04f).apply(requestOptions)
        .into(this)
}

fun ImageView.loadUri(uri: Uri?){
    val requestOptions = RequestOptions().placeholder(R.drawable.place_holder)
    Glide.with(this.context).asBitmap().optionalCenterCrop()
        .load(uri).thumbnail(0.04f).apply(requestOptions)
        .into(this)
}

/*fun ImageView.loadUri(uri: Uri){
    val requestOptions = RequestOptions().placeholder(R.drawable.ic_share_dflt)
    Glide.with(this.context).asBitmap().optionalCenterCrop()
        .load(uri).thumbnail(0.04f).apply(requestOptions)
        .into(this)
}*/

fun ImageView.loadUriForAudio(uri: Uri?){
    val requestOptions = RequestOptions().placeholder(R.drawable.place_holder)
    Glide.with(this.context).asBitmap().optionalCenterCrop()
        .load(uri).thumbnail(0.04f).apply(requestOptions)
        .into(this)
}


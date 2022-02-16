package com.simplemobiletools.camera.dialogs

import android.graphics.Camera
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.editor.hiderx.HiderUtils
import com.editor.hiderx.MySize
import com.editor.hiderx.R
import com.editor.hiderx.activity.CameraActivity
import com.simplemobiletools.commons.dialogs.RadioItem

import kotlinx.android.synthetic.main.dialog_change_resolution.view.*

class ChangeResolutionDialog(
    val activity: CameraActivity,
    val isFrontCamera: Boolean,
    val photoResolutions: ArrayList<MySize>,
    val videoResolutions: ArrayList<MySize>,
    val openVideoResolutions: Boolean,
    val callback: () -> Unit
) {
    private var dialog: AlertDialog

    init {
        val view =
            LayoutInflater.from(activity).inflate(R.layout.dialog_change_resolution, null).apply {
                setupPhotoResolutionPicker(this)
                setupVideoResolutionPicker(this)
            }

        dialog = AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setOnDismissListener { callback() }
            .create().apply {
                activity.setupDialogStuff(
                    view,
                    this,
                    if (isFrontCamera) R.string.front_camera else R.string.back_camera
                ) {
                    if (openVideoResolutions) {
                        view.change_resolution_video_holder.performClick()
                    }
                }
            }
    }

    private fun setupPhotoResolutionPicker(view: View) {
        val items = getFormattedResolutions(photoResolutions)
        var selectionIndex = if (isFrontCamera) HiderUtils.getIntSharedPreference(
            activity,
            HiderUtils.FRONT_PHOTO_RES_INDEX_KEY
        ) else HiderUtils.getIntSharedPreference(activity, HiderUtils.BACK_PHOTO_RES_INDEX_KEY)
        selectionIndex = Math.max(selectionIndex, 0)

        view.change_resolution_photo_holder.setOnClickListener {
            RadioGroupDialog(activity, items, selectionIndex) {
                selectionIndex = it as Int
                view.change_resolution_photo.text = items[selectionIndex].title
                if (isFrontCamera) {
                    HiderUtils.setIntSharedPreference(
                        activity,
                        HiderUtils.FRONT_PHOTO_RES_INDEX_KEY,
                        it
                    )
                } else {
                    HiderUtils.setIntSharedPreference(
                        activity,
                        HiderUtils.BACK_PHOTO_RES_INDEX_KEY,
                        it
                    )
                }
                dialog.dismiss()
            }
        }
        view.change_resolution_photo.text = items.getOrNull(selectionIndex)?.title
    }

    private fun setupVideoResolutionPicker(view: View) {
        val items = getFormattedResolutions(videoResolutions)
        var selectionIndex = if (isFrontCamera) HiderUtils.getIntSharedPreference(
            activity,
            HiderUtils.FRONT_VIDEO_RES_INDEX_KEY
        ) else HiderUtils.getIntSharedPreference(activity, HiderUtils.BACK_VIDEO_RES_INDEX_KEY)

        view.change_resolution_video_holder.setOnClickListener {
            RadioGroupDialog(activity, items, selectionIndex) {
                selectionIndex = it as Int
                view.change_resolution_video.text = items[selectionIndex].title
                if (isFrontCamera) {
                    HiderUtils.setIntSharedPreference(activity,HiderUtils.FRONT_VIDEO_RES_INDEX_KEY,it)
                } else {
                    HiderUtils.setIntSharedPreference(activity,HiderUtils.BACK_VIDEO_RES_INDEX_KEY,it)
                }
                dialog.dismiss()
            }
        }
        view.change_resolution_video.text = items.getOrNull(selectionIndex)?.title
    }

    private fun getFormattedResolutions(resolutions: List<MySize>): ArrayList<RadioItem> {
        val items = ArrayList<RadioItem>(resolutions.size)
        val sorted = resolutions.sortedByDescending { it.width * it.height }
        sorted.forEachIndexed { index, size ->
            val megapixels = String.format("%.1f", (size.width * size.height.toFloat()) / 1000000)
            val aspectRatio = size.getAspectRatio(activity)
            items.add(
                RadioItem(
                    index,
                    "${size.width} x ${size.height}  ($megapixels MP,  $aspectRatio)"
                )
            )
        }
        return items
    }
}

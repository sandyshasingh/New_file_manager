package com.simplemobiletools.filemanager.pro.dialogs

import android.annotation.SuppressLint
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.extensions.config
import kotlinx.android.synthetic.main.dialog_compress_as.view.*
import kotlinx.android.synthetic.main.dialog_create_new.view.*

@SuppressLint("ClickableViewAccessibility")
class CompressAsDialog(val activity: BaseSimpleActivity, val path: String, val callback: (destination: String) -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_compress_as, null)

    init {
        val filename = path.getFilenameFromPath()
        val indexOfDot = if (filename.contains('.') && !activity.getIsPathDirectory(path)) filename.lastIndexOf(".") else filename.length
        val baseFilename = filename.substring(0, indexOfDot)
        var realPath = path.getParentPath()

        view.apply {
            file_name.setText(baseFilename)
            file_path.text = activity.humanizePath(realPath)
            file_path.setOnClickListener {
                FilePickerDialog(activity, realPath,"Compress", "Cancel",false, activity.config.shouldShowHidden, true, true) {
                    file_path.text = activity.humanizePath(it)
                    realPath = it
                }
            }
            file_name.setOnTouchListener(View.OnTouchListener { v, event ->
                val DRAWABLE_RIGHT = 2
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= file_name.right - file_name.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        // your action here
                        view.file_name?.text?.clear()
                        return@OnTouchListener true
                    }
                } else {
                    view.file_name.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                }
                false
            })
        }

        AlertDialog.Builder(activity, R.style.MyDialogTheme)
                .setPositiveButton("Compress", null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.compress_as) {
                        showKeyboard(view.file_name)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                            val name = view.file_name.value
                            when {
                                name.isEmpty() -> activity.toast(R.string.empty_name)
                                name.isAValidFilename() -> {
                                    val newPath = "$realPath/$name.zip"
                                    if (activity.getDoesFilePathExist(newPath)) {
                                        activity.toast(R.string.name_taken)
                                        return@OnClickListener
                                    }

                                    dismiss()
                                    callback(newPath)
                                }
                                else -> activity.toast(R.string.invalid_name)
                            }
                        })
                    }
                }
    }
}

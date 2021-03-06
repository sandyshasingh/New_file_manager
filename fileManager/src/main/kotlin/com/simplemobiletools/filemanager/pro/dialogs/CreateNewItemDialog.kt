package com.simplemobiletools.filemanager.pro.dialogs

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.filemanager.pro.R
import com.simplemobiletools.filemanager.pro.helpers.RootHelpers
import kotlinx.android.synthetic.main.dialog_create_new.view.*
import java.io.File
import java.io.IOException


@SuppressLint("ClickableViewAccessibility")
class CreateNewItemDialog(val activity: BaseSimpleActivity,
                          var positiveButtonText: String,
                          var negativeButtonText: String,
                          val path: String, val callback: (success: Boolean) -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_create_new, null)

    init {
        AlertDialog.Builder(activity, R.style.MyDialogTheme)

                .setPositiveButton(positiveButtonText, null)
                .setNegativeButton(negativeButtonText, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.create_new) {

                        view.item_name.setOnTouchListener(View.OnTouchListener { v, event ->

                            val DRAWABLE_RIGHT = 2
                            if (event.action == MotionEvent.ACTION_UP) {
                                if (event.rawX >= view.item_name.right - view.item_name.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                                    // your action here
                                    view.item_name.text.clear()
                                    return@OnTouchListener true
                                }
                            } else {
                                view.item_name.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            }
                            false
                        })

                        showKeyboard(view.item_name)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                            val name = view.item_name.value
                            when {
                                name.isEmpty() -> {
                                    activity.toast(R.string.empty_name)
                                }
                                name.isAValidFilename() -> {
                                    val newPath = "$path/$name"
                                    if (activity.getDoesFilePathExist(newPath)) {
                                        activity.toast(R.string.name_taken)
                                        return@OnClickListener
                                    }

                                    success(this)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        createDirectory(newPath, this) {
                                            callback(it)
                                        }
                                    }, 1500)
                                }
                                else -> {
                                    activity.toast(R.string.invalid_name)
                                }
                            }
                        })
                    }
                }
    }

    private fun createDirectory(path: String, alertDialog: AlertDialog, callback: (Boolean) -> Unit) {
        when {
            activity.needsStupidWritePermissions(path) -> activity.handleSAFDialog(path) {
                if (!it) {
                    return@handleSAFDialog
                }

                val documentFile = activity.getDocumentFile(path.getParentPath())
                if (documentFile == null) {
                    val error = String.format(activity.getString(R.string.could_not_create_folder), path)
                    activity.showErrorToast(error)
                    callback(false)
                    return@handleSAFDialog
                }
                /* Handler(Looper.getMainLooper()).postDelayed({
                    // documentFile.createDirectory(path.getFilenameFromPath())
                 }, 3000)*/

                documentFile.createDirectory(path.getFilenameFromPath())


                success(alertDialog)
                callback(true)

            }
            path.startsWith(activity.internalStoragePath, true) -> {
                if (File(path).mkdirs()) {
                    success(alertDialog)
                    callback(true)

                }
            }
            else -> {
                RootHelpers(activity).createFileFolder(path, false) {
                    if (it) {
                        success(alertDialog)
                        callback(true)

                    } else {
                        callback(false)
                    }
                }
            }
        }
    }

    private fun createFile(path: String, alertDialog: AlertDialog, callback: (Boolean) -> Unit) {
        try {
            when {
                activity.needsStupidWritePermissions(path) -> {
                    activity.handleSAFDialog(path) {
                        if (!it) {
                            return@handleSAFDialog
                        }

                        val documentFile = activity.getDocumentFile(path.getParentPath())
                        if (documentFile == null) {
                            val error = String.format(activity.getString(R.string.could_not_create_file), path)
                            activity.showErrorToast(error)
                            callback(false)
                            return@handleSAFDialog
                        }
                        documentFile.createFile(path.getMimeType(), path.getFilenameFromPath())
                        success(alertDialog)
                        callback(true)

                    }
                }
                path.startsWith(activity.internalStoragePath, true) -> {
                    if (File(path).createNewFile()) {
                        success(alertDialog)
                        callback(true)

                    }
                }
                else -> {
                    RootHelpers(activity).createFileFolder(path, true) {
                        if (it) {
                            success(alertDialog)
                            callback(true)

                        } else {
                            callback(false)
                        }
                    }
                }
            }
        } catch (exception: IOException) {
            activity.showErrorToast(exception)
            callback(false)
        }
    }

    private fun success(alertDialog: AlertDialog) {
        alertDialog.dismiss()
    }
}

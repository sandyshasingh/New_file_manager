package com.simplemobiletools.commons.dialogs

import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_create_new_folder.*
import kotlinx.android.synthetic.main.dialog_create_new_folder.view.*
import java.io.File

class CreateNewFolderDialog(val activity: BaseSimpleActivity,
                             positiveButtonText : String,
                             negativeButtonText : String,
                            val path: String, val callback: (path: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_create_new_folder, null)
        view.folder_path.text = "${activity.humanizePath(path).trimEnd('/')}/"

       AlertDialog.Builder(activity,R.style.MyDialogTheme)
                .setPositiveButton(positiveButtonText, null)
                .setNegativeButton(negativeButtonText, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.create_new_folder) {

                        view.folder_name?.setOnTouchListener(View.OnTouchListener { v, event ->
                            val DRAWABLE_RIGHT = 2
                            if (event.action == MotionEvent.ACTION_UP) {
                                if (event.rawX >= folder_name.right - folder_name.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                                    // your action here
                                    view.folder_name?.text?.clear()
                                    return@OnTouchListener true
                                }
                            } else {
                                view.folder_name?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            }
                            false
                        })

                        showKeyboard(view.folder_name)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
                            val name = view.folder_name.value
                            when {
                                name.isEmpty() -> activity.toast(R.string.empty_name)
                                name.isAValidFilename() -> {
                                    val file = File(path, name)
                                    if (file.exists()) {
                                        activity.toast(R.string.name_taken)
                                        return@OnClickListener
                                    }

                                    createFolder("$path/$name", this)
                                }
                                else -> activity.toast(R.string.invalid_name)
                            }
                        })
                    }
                }

    }

    private fun createFolder(path: String, alertDialog: AlertDialog) {
        try {
            when {
                activity.needsStupidWritePermissions(path) -> activity.handleSAFDialog(path) {
                    if (it) {
                        try {
                            val documentFile = activity.getDocumentFile(path.getParentPath())
                            val newDir = documentFile?.createDirectory(path.getFilenameFromPath()) ?: activity.getDocumentFile(path)
                            if (newDir != null) {
                                sendSuccess(alertDialog, path)
                            } else {
                                activity.toast(R.string.unknown_error_occurred)
                            }
                        } catch (e: SecurityException) {
                            activity.showErrorToast(e)
                        }
                    }
                }
                File(path).mkdirs() -> sendSuccess(alertDialog, path)
                else -> activity.toast(R.string.unknown_error_occurred)
            }
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, path: String) {
        callback(path.trimEnd('/'))
        alertDialog.dismiss()
    }
}

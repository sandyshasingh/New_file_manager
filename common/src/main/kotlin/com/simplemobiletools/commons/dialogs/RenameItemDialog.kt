package com.simplemobiletools.commons.dialogs

import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_rename_item.view.*
import kotlinx.android.synthetic.main.tab_rename_simple.view.*
import java.util.*

class RenameItemDialog(val activity: BaseSimpleActivity, val path: String, val callback: (newPath: String) -> Unit) {
    init {
        var ignoreClicks = false
        val fullName = path.getFilenameFromPath()
        val dotAt = fullName.lastIndexOf(".")
        var name = fullName

        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_item, null).apply {
            if (dotAt > 0 && !activity.getIsPathDirectory(path)) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                rename_item_extension.setText(extension)
            } else {
                rename_item_extension_label.beGone()
                rename_item_extension.beGone()
            }

            rename_item_name.setText(name)
            rename_item_path.text = "${activity.humanizePath(path.getParentPath()).trimEnd('/')}/"
        }

      AlertDialog.Builder(activity,R.style.MyDialogTheme)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this,R.string.rename_new ) {

                        view.rename_item_name?.setOnTouchListener(View.OnTouchListener { v, event ->

                            val DRAWABLE_RIGHT = 2
                            if (event.action == MotionEvent.ACTION_UP) {
                                if (event.rawX >= view.rename_item_name.right - view.rename_item_name.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                                    // your action here
                                    view.rename_item_name?.text?.clear()
                                    return@OnTouchListener true
                                }
                            } else {
                                view.rename_item_name?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            }
                            false
                        })

                        showKeyboard(view.rename_item_name)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (ignoreClicks) {
                                return@setOnClickListener
                            }

                            var newName = view.rename_item_name.value
                            val newExtension = view.rename_item_extension.value

                            if (newName.isEmpty()) {
                                activity.toast(R.string.empty_name)
                                return@setOnClickListener
                            }

                            if (!newName.isAValidFilename()) {
                                activity.toast(R.string.invalid_name)
                                return@setOnClickListener
                            }

                            val updatedPaths = ArrayList<String>()
                            updatedPaths.add(path)
                            if (!newExtension.isEmpty()) {
                                newName += ".$newExtension"
                            }

                            if (!activity.getDoesFilePathExist(path)) {
                                activity.toast(String.format(activity.getString(R.string.source_file_doesnt_exist), path))
                                return@setOnClickListener
                            }

                            val newPath = "${path.getParentPath()}/$newName"
                            if (activity.getDoesFilePathExist(newPath)) {
                                activity.toast(R.string.name_taken)
                                return@setOnClickListener
                            }

                            updatedPaths.add(newPath)
                            ignoreClicks = true
                            activity.renameFile(path, newPath) {
                                ignoreClicks = false
                                if (it) {
                                    callback(newPath)
                                    dismiss()
                                } else {
                                    activity.toast(R.string.unknown_error_occurred)
                                }
                            }
                        }
                    }
                }

    }

}

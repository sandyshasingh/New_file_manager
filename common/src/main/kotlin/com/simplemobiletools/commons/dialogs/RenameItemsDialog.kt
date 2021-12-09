package com.simplemobiletools.commons.dialogs

import android.text.InputType
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import kotlinx.android.synthetic.main.dialog_rename_items.*
import kotlinx.android.synthetic.main.dialog_rename_items.view.*
import java.util.*

class RenameItemsDialog(val activity: BaseSimpleActivity, val paths: ArrayList<String>, val callback: () -> Unit) {
    init {
        var ignoreClicks = false
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_items, null)

        AlertDialog.Builder(activity,R.style.MyDialogTheme)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)


                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.rename) {

                        view.rename_items_value?.setOnTouchListener(View.OnTouchListener { v, event ->

                            val DRAWABLE_RIGHT = 2
                            if (event.action == MotionEvent.ACTION_UP) {
                                if (event.rawX >= view.rename_items_value.right - view.rename_items_value.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                                    // your action here
                                    view.rename_items_value?.text?.clear()
                                    return@OnTouchListener true
                                }
                            } else {
                                view.rename_items_value?.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                            }
                            false
                        })

                        showKeyboard(view.rename_items_value)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            if (ignoreClicks) {
                                return@setOnClickListener
                            }

                            val valueToAdd = view.rename_items_value.text.toString()
                            val append = view.rename_items_radio_group.checkedRadioButtonId == rename_items_radio_append.id

                            if (valueToAdd.isEmpty()) {
                                callback()
                                dismiss()
                                return@setOnClickListener
                            }

                            if (!valueToAdd.isAValidFilename()) {
                                activity.toast(R.string.invalid_name)
                                return@setOnClickListener
                            }

                            val validPaths = paths.filter { activity.getDoesFilePathExist(it) }
                            val sdFilePath = validPaths.firstOrNull { activity.isPathOnSD(it) } ?: validPaths.firstOrNull()
                            if (sdFilePath == null) {
                                activity.toast(R.string.unknown_error_occurred)
                                dismiss()
                                return@setOnClickListener
                            }

                            activity.handleSAFDialog(sdFilePath) {
                                if (!it) {
                                    return@handleSAFDialog
                                }

                                ignoreClicks = true
                                var pathsCnt = validPaths.size
                                for (path in validPaths) {
                                    val fullName = path.getFilenameFromPath()
                                    var dotAt = fullName.lastIndexOf(".")
                                    if (dotAt == -1) {
                                        dotAt = fullName.length
                                    }

                                    val name = fullName.substring(0, dotAt)
                                    val extension = if (fullName.contains(".")) ".${fullName.getFilenameExtension()}" else ""

                                    val newName = if (append) {
                                        "$name$valueToAdd$extension"
                                    } else {
                                        "$valueToAdd$fullName"
                                    }

                                    val newPath = "${path.getParentPath()}/$newName"

                                    if (activity.getDoesFilePathExist(newPath)) {
                                        continue
                                    }

                                    activity.renameFile(path, newPath) {
                                        if (it) {
                                            pathsCnt--
                                            if (pathsCnt == 0) {
                                                callback()
                                                dismiss()
                                            }
                                        } else {
                                            ignoreClicks = false
                                            activity.toast(R.string.unknown_error_occurred)
                                            dismiss()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
    }
}
